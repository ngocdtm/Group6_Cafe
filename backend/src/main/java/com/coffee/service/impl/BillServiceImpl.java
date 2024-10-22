
package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Bill;
import com.coffee.entity.BillItem;
import com.coffee.entity.Coupon;
import com.coffee.repository.BillRepository;
import com.coffee.repository.CouponRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.BillService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.BillWrapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Autowired
    CouponRepository couponRepository;

    @Override
    public ResponseEntity<String> generateBill(Map<String, Object> requestMap) {
        log.info("Inside Generate Report");
        try {
            String fileName;
            if (validateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && Boolean.TRUE.equals(!(Boolean) requestMap.get("isGenerate"))) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }

                String data = "Name: " + requestMap.get("name") + "\n" + "Contact Number: " + requestMap.get("phoneNumber") +
                        "\n" + "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod");

                Document doc = new Document();
                PdfWriter.getInstance(doc, new FileOutputStream(CafeConstants.STORE_LOCATION + "/" + fileName + ".pdf"));
                doc.open();
                setRectangleInPdf(doc);

                Paragraph chunk = new Paragraph("Cafe Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                doc.add(chunk);

                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                doc.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                Bill bill = billRepository.findByUuid(fileName);
                for (BillItem item : bill.getBillItems()) {
                    addRow(table, item);
                }
                doc.add(table);

                Paragraph footer = new Paragraph("Total Before Discount Amount: " + bill.getTotal() + "\n" +
                        "Discount: " + bill.getDiscount() + "\n" +
                        "Total After Discount Amount: " + bill.getTotalAfterDiscount() + "\n" +
                        "Thank You For Visiting!!", getFont("Data"));
                doc.add(footer);
                doc.close();
                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity("Required data not found!", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRow(PdfPTable table, BillItem item) {
        log.info("Inside addRow");
        table.addCell(item.getProduct().getName());
        table.addCell(item.getProduct().getCategory().getName());
        table.addCell(String.valueOf(item.getQuantity()));
        table.addCell(String.valueOf(item.getPrice()));
        table.addCell(String.valueOf(item.getQuantity() * item.getPrice()));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside table header");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorder(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void setRectangleInPdf(Document doc) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rect = new Rectangle(577, 825, 18, 15);
        rect.enableBorderSide(1);
        rect.enableBorderSide(2);
        rect.enableBorderSide(4);
        rect.enableBorderSide(8);
        rect.setBorderColor(BaseColor.BLACK);
        rect.setBorderWidth(1);
        doc.add(rect);
    }

    private Font getFont(String type){
        log.info("Inside getFont");
        switch ( type ){
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setPhoneNumber((String) requestMap.get("phoneNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal((Integer) requestMap.get("total"));
            // Set totalAfterDiscount to total if no discount applied
            Integer totalAfterDiscount = (Integer) requestMap.get("totalAfterDiscount");
            if (totalAfterDiscount == null || totalAfterDiscount.equals(bill.getTotal())) {
                bill.setTotalAfterDiscount(bill.getTotal());
            } else {
                bill.setTotalAfterDiscount(totalAfterDiscount);
            }

            bill.setCreatedBy(jwtRequestFilter.getCurrentUser());
            bill.setStatus("PENDING"); // Set initial status
            bill.setOrderDate(LocalDateTime.now());

            // Set discount to 0 if no coupon applied
            Integer discount = (Integer) requestMap.get("discount");
            bill.setDiscount(discount != null ? discount : 0);

            // Set coupon code if available
            String couponCode = (String) requestMap.get("code");
            if (couponCode != null && !couponCode.isEmpty()) {
                bill.setCode(couponCode);
            }

            List<BillItem> billItems = new ArrayList<>();
            JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                BillItem billItem = new BillItem();
                billItem.setBill(bill);
                billItem.setProduct(productRepository.findById(jsonObject.getInt("id")).orElseThrow());
                billItem.setQuantity(jsonObject.getInt("quantity"));
                billItem.setPrice(jsonObject.getInt("price"));
                billItems.add(billItem);
            }

            bill.setBillItems(billItems);
            billRepository.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("phoneNumber")&&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("total");
    }

    @Override
    public ResponseEntity<List<BillWrapper>> getBills() {
        log.info("Inside get bills");
        try {
            List<Bill> billList;
            if (jwtRequestFilter.isAdmin()) {
                billList = billRepository.getAllBills();
            } else {
                billList = billRepository.getBillByUserName(jwtRequestFilter.getCurrentUser());
            }
            List<BillWrapper> billDTOs = billList.stream().map(BillWrapper::fromBill).collect(Collectors.toList());
            return new ResponseEntity<>(billDTOs, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf: requestMap {}", requestMap);
        try{
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)){
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = CafeConstants.STORE_LOCATION + "/" +(String)requestMap.get("uuid") + ".pdf";
            if(Boolean.TRUE.equals(CafeUtils.isFileExist(filePath))){
                byteArray = getByteArray(filePath);
            }else{
                requestMap.put("isGenerated", false);
                generateBill(requestMap);
            }
            return new ResponseEntity<>(byteArray, HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filePath) throws Exception {
        File initialFile = new File(filePath);
        InputStream inputStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        inputStream.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            Optional<Bill> optional = billRepository.findById(id);
            if(optional.isPresent()) {
                billRepository.deleteById(id);
                return CafeUtils.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
            }else{
                return CafeUtils.getResponseEntity("Bill with id does not exist", HttpStatus.OK);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<Map<String, Object>>  applyCoupon(Map<String, Object> requestMap) {
        try {
            String couponCode = (String) requestMap.get("code");
            Integer totalAmount = (Integer) requestMap.get("total");

            Coupon coupon = couponRepository.findByCode(couponCode);
            if (coupon == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid coupon code"));
            }

            if (couponIsExpired(coupon)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Coupon is expired"));
            }

            // Check if coupon has been used before
            if (billRepository.existsByCouponCode(couponCode)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Coupon has already been used"));
            }

            Integer discountAmount = (totalAmount * coupon.getDiscount().intValue()) / 100;
            Integer totalAfterDiscount = totalAmount - discountAmount;

            Map<String, Object> response = new HashMap<>();
            response.put("total", totalAmount);
            response.put("totalAfterDiscount", totalAfterDiscount);
            response.put("discountAmount", discountAmount);
            response.put("discount", coupon.getDiscount());
            response.put("code", couponCode);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", CafeConstants.SOMETHING_WENT_WRONG));
    }


    private boolean couponIsExpired(Coupon coupon) {
        LocalDate currentDate = LocalDate.now();
        LocalDate expirationDate = coupon.getExpirationDate();
        return expirationDate != null && currentDate.isAfter(expirationDate);
    }
}




