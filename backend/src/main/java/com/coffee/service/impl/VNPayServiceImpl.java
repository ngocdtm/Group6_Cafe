package com.coffee.service.impl;

import com.coffee.config.VNPayConfig;
import com.coffee.entity.*;
import com.coffee.enums.OrderStatus;
import com.coffee.enums.OrderType;
import com.coffee.repository.*;
import com.coffee.security.JwtRequestFilter;
import com.coffee.service.InventoryService;
import com.coffee.service.ShoppingCartService;
import com.coffee.service.VNPayService;
import com.coffee.utils.CafeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventorySnapshotRepository inventorySnapshotRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    public String createPayment(Payment paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("Creating payment with data: {}", paymentDTO);

        // Validate input
        if (paymentDTO.getAmount() < 1000) {
            throw new IllegalArgumentException("Amount must be greater than 1000 VND");
        }
        log.info("Received shipping address: '{}'", paymentDTO.getShippingAddress());

        Bill bill = createInitialBill(paymentDTO);
        createBillItems(bill, paymentDTO.getOrderInfo()); // Add this line to create bill items

        try {
            billRepository.save(bill);
        } catch (Exception e) {
            log.error("Error saving bill: ", e);
            throw new RuntimeException("Error saving bill", e);
        }

        // Continue with existing VNPAY payment URL generation code...
        return generatePaymentUrl(paymentDTO, request);
    }

    private Bill createInitialBill(Payment paymentDTO) {
        Bill bill = new Bill();
        bill.setUuid(paymentDTO.getOrderId());
        bill.setTotal(paymentDTO.getTotal());
        bill.setOrderStatus(OrderStatus.PENDING);
        bill.setOrderDate(LocalDateTime.now());
        bill.setOrderType(OrderType.ONLINE);
        bill.setPaymentMethod("VNPAY");
        bill.setCustomerName(paymentDTO.getCustomerName());
        bill.setCustomerPhone(paymentDTO.getCustomerPhone());

        if (paymentDTO.getShippingAddress() != null && !paymentDTO.getShippingAddress().trim().isEmpty()) {
            bill.setShippingAddress(paymentDTO.getShippingAddress().trim());
        } else {
            throw new IllegalArgumentException("Shipping address is required for online orders");
        }

        processCouponAndDiscount(bill, paymentDTO);
        processUserInformation(bill);

        return bill;
    }

    private void createBillItems(Bill bill, String orderInfo) {
        try {
            JSONArray jsonArray = CafeUtils.getJsonArrayFromString(orderInfo);
            List<BillItem> billItems = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Product product = productRepository.findById(jsonObject.getInt("id"))
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                BillItem billItem = BillItem.createFromProduct(
                        product,
                        jsonObject.getInt("quantity"),
                        jsonObject.getInt("price")
                );
                billItem.setBill(bill);
                billItems.add(billItem);
            }

            bill.setBillItems(billItems);
        } catch (JSONException e) {
            throw new RuntimeException("Error processing order items", e);
        }
    }

    private String generatePaymentUrl(Payment paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(paymentDTO.getAmount() * 100L));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", paymentDTO.getOrderId());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + paymentDTO.getOrderId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        if (paymentDTO.getBankCode() != null && !paymentDTO.getBankCode().isEmpty()) {
            vnp_Params.put("vnp_BankCode", paymentDTO.getBankCode());
        }

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    private void processCouponAndDiscount(Bill bill, Payment paymentDTO) {
        if (paymentDTO.getCouponCode() != null && !paymentDTO.getCouponCode().isEmpty()) {
            bill.setCouponCode(paymentDTO.getCouponCode());

            // Validate discount value
            if (paymentDTO.getDiscount() != null && paymentDTO.getDiscount() >= 0 &&
                    paymentDTO.getDiscount() <= paymentDTO.getTotal()) {
                bill.setDiscount(paymentDTO.getDiscount());
                bill.setTotalAfterDiscount(paymentDTO.getTotal() - paymentDTO.getDiscount());
            } else {
                throw new IllegalArgumentException("Invalid discount amount");
            }
        } else {
            bill.setDiscount(0);
            bill.setTotalAfterDiscount(paymentDTO.getTotal());
        }
    }

    private void processUserInformation(Bill bill) {
        String userEmail = jwtRequestFilter.getCurrentUser();
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail);
            if (user != null) {
                bill.setUser(user);
                bill.setCreatedByUser(userEmail);

                // Only override customer info if not provided
                if (bill.getCustomerName() == null || bill.getCustomerName().trim().isEmpty()) {
                    bill.setCustomerName(user.getName());
                }
                if (bill.getCustomerPhone() == null || bill.getCustomerPhone().trim().isEmpty()) {
                    bill.setCustomerPhone(user.getPhoneNumber());
                }
            }
        }
    }

    private Map<String, String> collectRequestParameters(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();

        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        return fields;
    }

    private String processFailedPayment(Bill bill, String reason) {
        bill.setOrderStatus(OrderStatus.FAILED);
        bill.setLastUpdatedDate(LocalDateTime.now());
        bill.setFailureReason(reason);
        billRepository.save(bill);
        return "Payment Failed - " + reason;
    }

    @Override
    @Transactional
    public String paymentCallback(HttpServletRequest request) {
        try {
            Map<String, String> fields = collectRequestParameters(request);
            String vnp_SecureHash = fields.get("vnp_SecureHash");

            if (vnp_SecureHash == null) {
                log.error("Missing secure hash");
                return "Payment Failed - Missing security hash";
            }

            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String orderId = fields.get("vnp_TxnRef");
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String amount = fields.get("vnp_Amount");
            String bankCode = fields.get("vnp_BankCode");
            String payDate = fields.get("vnp_PayDate");

            String signValue = VNPayConfig.hashAllFields(fields);

            if (signValue.equals(vnp_SecureHash)) {
                return processVerifiedPayment(orderId, vnp_ResponseCode, amount, bankCode, payDate);
            } else {
                log.error("Invalid signature. Expected: {}, Got: {}", signValue, vnp_SecureHash);
                return "Payment Failed - Invalid signature";
            }
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            return "Payment Failed - System error: " + e.getMessage();
        }
    }

    @Transactional
    private String processVerifiedPayment(String orderId, String responseCode, String amount,
                                          String bankCode, String payDate) {
        if ("00".equals(responseCode)) {
            Bill bill = billRepository.findByUuid(orderId);
            if (bill != null) {
                long vnpayAmount = Long.parseLong(amount) / 100;
                if (vnpayAmount == bill.getTotalAfterDiscount()) {
                    return processSuccessfulPayment(bill, bankCode, payDate);
                } else {
                    return processFailedPayment(bill, "Amount mismatch");
                }
            }
            return "Payment Failed - Bill not found";
        } else {
            Bill bill = billRepository.findByUuid(orderId);
            if (bill != null) {
                return processFailedPayment(bill, getResponseDescription(responseCode));
            }
            return "Payment Failed - " + getResponseDescription(responseCode);
        }
    }

    @Transactional
    private String processSuccessfulPayment(Bill bill, String bankCode, String payDate) {
        try {
            // Update bill status
            bill.setOrderStatus(OrderStatus.PENDING);
            bill.setLastUpdatedDate(LocalDateTime.now());
            bill.setBankCode(bankCode);
            bill.setPayDate(payDate);

            // Update inventory for each item
            for (BillItem billItem : bill.getBillItems()) {
                updateInventoryForItem(billItem, bill.getUuid());
            }

            billRepository.save(bill);
            // Clear the user's shopping cart after successful order
            shoppingCartService.clearCart();
            return "Payment Success";
        } catch (Exception e) {
            log.error("Error processing successful payment for bill {}", bill.getUuid(), e);
            bill.setOrderStatus(OrderStatus.FAILED);
            bill.setFailureReason("Error updating inventory: " + e.getMessage());
            billRepository.save(bill);
            return "Payment Failed - Inventory Update Error";
        }
    }

    private void updateInventoryForItem(BillItem billItem, String orderUuid) {
        try {
            Integer productId = billItem.getOriginalProductId();
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();
                int quantity = billItem.getQuantity();

                // Remove stock and create transaction record
                inventoryService.removeStock(productId, quantity, "Order #" + orderUuid);

                // Create inventory snapshot
                InventorySnapshot snapshot = new InventorySnapshot();
                snapshot.setProduct(inventory.getProduct());
                snapshot.setQuantity(inventory.getQuantity());
                snapshot.setSnapshotDate(LocalDate.now());
                snapshot.setCreatedAt(LocalDateTime.now());
                inventorySnapshotRepository.save(snapshot);
            } else {
                log.warn("Product with ID {} not found while updating inventory for Order #{}",
                        productId, orderUuid);
                throw new RuntimeException("Product inventory not found");
            }
        } catch (Exception ex) {
            log.error("Error updating inventory for product: {}", billItem.getProductName(), ex);
            throw new RuntimeException("Failed to update inventory", ex);
        }
    }

    private String getResponseDescription(String responseCode) {
        Map<String, String> responseDescriptions = new HashMap<>();
        responseDescriptions.put("00", "Giao dịch thành công");
        responseDescriptions.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)");
        responseDescriptions.put("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng");
        responseDescriptions.put("10", "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        responseDescriptions.put("11", "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch");
        responseDescriptions.put("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa");
        responseDescriptions.put("13", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)");
        responseDescriptions.put("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch");
        responseDescriptions.put("51", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch");
        responseDescriptions.put("65", "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày");
        responseDescriptions.put("75", "Ngân hàng thanh toán đang bảo trì");
        responseDescriptions.put("79", "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định");
        responseDescriptions.put("99", "Các lỗi khác");

        return responseDescriptions.getOrDefault(responseCode, "Unknown error");
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP:" + e.getMessage();
        }
        return ipAddress;
    }
}