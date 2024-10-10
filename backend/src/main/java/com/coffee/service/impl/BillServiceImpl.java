
package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.Bill;
import com.coffee.entity.Category;
import com.coffee.entity.Coupon;
import com.coffee.entity.Product;
import com.coffee.repository.BillRepository;
import com.coffee.repository.CouponRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.BillService;
import com.coffee.utils.CafeUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillRepository billRepository;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Autowired
    CouponRepository couponRepository;

    @Override
    public ResponseEntity<String> generateBill(Map<String, Object> requestMap) {
        log.info("Inside Generate Report");
        try{
            String code = (String) requestMap.get("code");
            Integer total = (Integer) requestMap.get("total");

            // Apply discount if coupon is provided
            if (code != null && !code.isEmpty()) {
                ResponseEntity<Map<String, Object>> discountResponse = applyCoupon(Map.of("code", code, "total", total));
                if (discountResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> discountInfo = discountResponse.getBody();
                    requestMap.put("total", discountInfo.get("total"));
                    requestMap.put("totalAfterDiscount", discountInfo.get("totalAfterDiscount"));
                    requestMap.put("discount", discountInfo.get("discount"));
                }
            }



            String fileName;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey("isGenerate") && Boolean.TRUE.equals(!(Boolean) requestMap.get("isGenerate"))){
                    fileName = (String) requestMap.get("uuid");
                }else{
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

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for(int i = 0; i < jsonArray.length(); i++){
                    addRow(table, CafeUtils.getMapFromJSon(jsonArray.getString(i)));
                }
                doc.add(table);

                Paragraph footer = new Paragraph("Total Amount: " + requestMap.get("total") + "\n"
                        + "Thank You For Visiting!!", getFont("Data"));
                doc.add(footer);
                doc.close();
                return new ResponseEntity<>("{\"uuid\":\"" + fileName +"\"}", HttpStatus.OK);
            }else {
                return CafeUtils.getResponseEntity("Required data not found!", HttpStatus.BAD_REQUEST);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRow(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRow");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(String.valueOf(data.get("price")));
        table.addCell(String.valueOf(data.get("total")));
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
        try{
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setPhoneNumber((String) requestMap.get("phoneNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.valueOf((String) requestMap.get("total")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtRequestFilter.getCurrentUser());
            billRepository.save(bill);
        }catch(Exception ex){
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
    public ResponseEntity<List<Bill>> getBills() {
        log.info(("Inside get bills"));
        try{
            List<Bill> billList;
            if(jwtRequestFilter.isAdmin()){
                billList = billRepository.getAllBills();
            }else{
                billList = billRepository.getBillByUserName(jwtRequestFilter.getCurrentUser());
            }
            return new ResponseEntity<>(billList, HttpStatus.OK);
        }catch(Exception ex){
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

            Integer discountAmount = (totalAmount * coupon.getDiscount().intValue()) / 100;
            Integer totalAfterDiscount = totalAmount - discountAmount;

            Map<String, Object> response = new HashMap<>();
            response.put("total", totalAmount);
            response.put("totalAfterDiscount", totalAfterDiscount);
            response.put("discountAmount", discountAmount);
            response.put("discount", coupon.getDiscount());

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




