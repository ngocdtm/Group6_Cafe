package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.*;
import com.coffee.enums.OrderStatus;
import com.coffee.enums.OrderType;
import com.coffee.repository.*;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.BillService;
import com.coffee.service.InventoryService;
import com.coffee.service.ShoppingCartService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.BillWrapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    InventorySnapshotRepository inventorySnapshotRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public ResponseEntity<String> generateOfflineBill(Map<String, Object> requestMap) {
        log.info("Inside Generate Offline Bill");
        try {
            if (validateOfflineBillRequest(requestMap)) {
                String fileName = CafeUtils.getUUID();
                requestMap.put("uuid", fileName);
                Bill bill = createBillFromRequest(requestMap, OrderType.IN_STORE);

                // Cập nhật số lượng hàng tồn kho
                updateInventoryForOrder(bill);

                generatePdf(bill, fileName);
                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Required data not found!", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> processOnlineOrder(Map<String, Object> requestMap) {
        log.info("Inside Process Online Order");
        try {
            if (validateOnlineOrderRequest(requestMap)) {
                String fileName = CafeUtils.getUUID();
                requestMap.put("uuid", fileName);
                Bill bill = createBillFromRequest(requestMap, OrderType.ONLINE);

                // Cập nhật số lượng hàng tồn kho
                updateInventoryForOrder(bill);

                // Clear the user's shopping cart after successful order
                shoppingCartService.clearCart();

                // Generate PDF for order confirmation
                generatePdf(bill, fileName);

                // Could add email notification here

                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Required data not found!", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void updateInventoryForOrder(Bill bill) {
        for (BillItem billItem : bill.getBillItems()) {
            try {
                Integer productId = billItem.getOriginalProductId();
                Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();
                    int quantity = billItem.getQuantity();
                    inventoryService.removeStock(productId, quantity, "Order #" + bill.getUuid());

                    // Create new InventorySnapshot
                    InventorySnapshot snapshot = new InventorySnapshot();
                    snapshot.setProduct(inventory.getProduct());
                    snapshot.setQuantity(inventory.getQuantity());
                    snapshot.setSnapshotDate(LocalDate.now());
                    snapshot.setCreatedAt(LocalDateTime.now());
                    inventorySnapshotRepository.save(snapshot);
                } else {
                    // Log warning nếu product không tồn tại (có thể đã bị xóa hoàn toàn)
                    log.warn("Product with ID {} not found while updating inventory for Order #{}",
                            productId, bill.getUuid());
                }

            } catch (Exception ex) {
                log.error("Error updating inventory for product: {}", billItem.getProductName(), ex);
            }
        }
    }

    private Bill createBillFromRequest(Map<String, Object> requestMap, OrderType orderType) throws JSONException {
        Bill bill = new Bill();
        bill.setUuid((String) requestMap.get("uuid"));
        bill.setCustomerName((String) requestMap.get("customerName"));
        bill.setCustomerPhone((String) requestMap.get("customerPhone"));
        bill.setShippingAddress((String) requestMap.get("shippingAddress"));
        bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
        bill.setTotal((Integer) requestMap.get("total"));
        bill.setOrderType(orderType);

        // Set order status based on order type
        if (orderType == OrderType.IN_STORE) {
            bill.setOrderStatus(OrderStatus.COMPLETED);
        } else {
            bill.setOrderStatus(OrderStatus.PENDING);
        }

        bill.setOrderDate(LocalDateTime.now());
        bill.setLastUpdatedDate(LocalDateTime.now());

        // Set user if available (required for online orders)
        String userEmail = jwtRequestFilter.getCurrentUser();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail);
            bill.setCreatedByUser(userEmail);
            if (orderType == OrderType.ONLINE){
                bill.setUser(user);
            }
        }

        // Handle discount and coupon with null checks
        String couponCode = (String) requestMap.get("couponCode");
        if (couponCode != null && !couponCode.isEmpty()) {
            bill.setCouponCode(couponCode);

            // Get discount and totalAfterDiscount from request with default values
            Integer discount = (Integer) requestMap.getOrDefault("discount", 0);
            Integer totalAfterDiscount = (Integer) requestMap.getOrDefault("totalAfterDiscount", bill.getTotal());

            bill.setDiscount(discount);
            bill.setTotalAfterDiscount(totalAfterDiscount);
        } else {
            // If no coupon, set default values
            bill.setDiscount(0);
            bill.setTotalAfterDiscount(bill.getTotal());
        }

        // Process bill items
        List<BillItem> billItems = new ArrayList<>();
        JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Product product = productRepository.findById(jsonObject.getInt("id"))
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Create bill item with snapshot of current product state
            BillItem billItem = BillItem.createFromProduct(
                    product,
                    jsonObject.getInt("quantity"),
                    jsonObject.getInt("price")
            );
            billItem.setBill(bill);
            billItems.add(billItem);
        }

        bill.setBillItems(billItems);
        return billRepository.save(bill);
    }

    @Override
    public ResponseEntity<String> updateOrderStatus(Integer id, OrderStatus status) {
        try {
            Bill bill = billRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));
            bill.setOrderStatus(status);
            bill.setLastUpdatedDate(LocalDateTime.now());
            billRepository.save(bill);

            // Here you could add notifications based on status change

            return CafeUtils.getResponseEntity("Order status updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateOfflineBillRequest(Map<String, Object> requestMap) {
        return requestMap.containsKey("customerName") &&
                requestMap.containsKey("customerPhone") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("total") &&
                !requestMap.get("productDetails").toString().isEmpty() && // Ensure productDetails is not empty
                (Integer) requestMap.get("total") > 0; // Ensure total is greater than 0
    }

    private boolean validateOnlineOrderRequest(Map<String, Object> requestMap) {
        return requestMap.containsKey("customerName") &&
                requestMap.containsKey("customerPhone") &&
                requestMap.containsKey("shippingAddress") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("total");
    }

    // Updated PDF generation method
    private void generatePdf(Bill bill, String fileName) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION + "/" + fileName + ".pdf"));
        document.open();
        setRectangleInPdf(document);

        Paragraph title = new Paragraph("Cafe Management System", getFont("Header"));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        String orderType = bill.getOrderType() == OrderType.ONLINE ? "Online Order" : "In-Store Purchase";
        Paragraph orderInfo = new Paragraph("Order Type: " + orderType + "\n" +
                "Order Date: " + bill.getFormattedOrderDate() + "\n" +
                "Order Status: " + bill.getOrderStatus() + "\n\n", getFont("Data"));
        document.add(orderInfo);

        // Customer Information
        String customerInfo = "Customer Information:\n" +
                "Name: " + bill.getCustomerName() + "\n" +
                "Phone: " + bill.getCustomerPhone() + "\n" +
                (bill.getShippingAddress() != null ? "Shipping Address: " + bill.getShippingAddress() + "\n" : "") +
                "Payment Method: " + bill.getPaymentMethod() + "\n\n";

        document.add(new Paragraph(customerInfo, getFont("Data")));

        // Products table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        addTableHeader(table);

        for (BillItem item : bill.getBillItems()) {
            addRow(table, item);
        }
        document.add(table);

        // Total calculations
        Paragraph totals = new Paragraph(
                "Subtotal: " + bill.getTotal() + "\n" +
                        "Discount: " + bill.getDiscount() + "\n" +
                        "Total Amount: " + bill.getTotalAfterDiscount() + "\n\n" +
                        "Thank you for your purchase!", getFont("Data"));
        document.add(totals);
        document.close();
    }

    private void addRow(PdfPTable table, BillItem item) {
        log.info("Inside addRow");
        table.addCell(item.getProductName());
        table.addCell(item.getProductCategory());
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

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf");
        try {
            if (validateGetPdfRequest(requestMap)) {
                String uuid = (String) requestMap.get("uuid");
                String filePath = CafeConstants.STORE_LOCATION + "/" + uuid + ".pdf";

                // Check if file exists
                File file = new File(filePath);
                if (!file.exists()) {
                    log.info("PDF file not found. Attempting to regenerate...");
                    // Find the bill by UUID
                    Bill bill = billRepository.findByUuid(uuid);
                    if (bill == null) {
                        log.error("Bill not found with UUID: {}", uuid);
                        return new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND);
                    }

                    try {
                        // Regenerate the PDF
                        generatePdf(bill, uuid);
                        log.info("PDF regenerated successfully");


                        // Check if the file was created successfully
                        if (!new File(filePath).exists()) {
                            log.error("Failed to regenerate PDF file");
                            return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    } catch (Exception e) {
                        log.error("Error regenerating PDF: {}", e.getMessage());
                        return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

                // Read PDF file into byte array
                byte[] pdfBytes = readPdfFile(filePath);

                // Set response headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("filename", uuid + ".pdf");
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            }
            return new ResponseEntity<>(new byte[0], HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error in getPdf: {}", ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateGetPdfRequest(Map<String, Object> requestMap) {
        return requestMap.containsKey("uuid") &&
                requestMap.get("uuid") != null &&
                !requestMap.get("uuid").toString().trim().isEmpty();
    }

    private byte[] readPdfFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return IOUtils.toByteArray(fileInputStream);
        }
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
    public ResponseEntity<Map<String, Object>> applyCoupon(Map<String, Object> requestMap) {
        try {
            String couponCode = (String) requestMap.get("couponCode");
            Integer totalAmount = (Integer) requestMap.get("total");


            if (couponCode == null || totalAmount == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid request parameters"));
            }

            // Validate coupon
            Coupon coupon = couponRepository.findByCode(couponCode);
            if (coupon == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid coupon code"));
            }

            // Check coupon expiration
            if (couponIsExpired(coupon)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Coupon is expired"));
            }

            // Calculate discount
            Integer discountAmount = (totalAmount * coupon.getDiscount().intValue()) / 100;
            Integer totalAfterDiscount = totalAmount - discountAmount;

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("total", totalAmount);
            response.put("totalAfterDiscount", totalAfterDiscount);
            response.put("discountAmount", discountAmount);
            response.put("discount", coupon.getDiscount());
            response.put("couponCode", couponCode);
            response.put("message", "Coupon applied successfully");

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CafeConstants.SOMETHING_WENT_WRONG));
        }
    }

    private boolean couponIsExpired(Coupon coupon) {
        LocalDate currentDate = LocalDate.now();
        LocalDate expirationDate = coupon.getExpirationDate();
        return expirationDate != null && currentDate.isAfter(expirationDate);
    }
}