package com.coffee.service.impl;

import com.coffee.config.VNPayConfig;
import com.coffee.entity.Bill;
import com.coffee.entity.Payment;
import com.coffee.enums.OrderStatus;
import com.coffee.repository.BillRepository;
import com.coffee.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    @Autowired
    private BillRepository billRepository;

    @Override
    public String createPayment(Payment paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("Creating payment with data: {}", paymentDTO);

        // Validate input
        if (paymentDTO.getAmount() < 1000) {
            throw new IllegalArgumentException("Amount must be greater than 1000 VND");
        }

        // Tạo Bill record trước khi tạo payment URL
        Bill bill = new Bill();
        bill.setUuid(paymentDTO.getOrderId());
        bill.setTotal(paymentDTO.getAmount());
        bill.setOrderStatus(OrderStatus.PENDING);
        bill.setOrderDate(LocalDateTime.now());
        billRepository.save(bill);

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

        // Sort and build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Build query
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
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

        log.info("Generated payment URL: {}", paymentUrl);
        return paymentUrl;
    }

    @Override
    public String paymentCallback(HttpServletRequest request) {
        try {
            // Collect all request parameters into a map
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            log.info("Received VNPay callback parameters: {}", fields);

            // Get the secure hash from the request
            String vnp_SecureHash = fields.get("vnp_SecureHash");
            if (vnp_SecureHash == null) {
                log.error("Missing secure hash");
                return "Payment Failed - Missing security hash";
            }

            // Remove hash-related fields before validation
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Get important fields for processing
            String orderId = fields.get("vnp_TxnRef");
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String amount = fields.get("vnp_Amount");
            String bankCode = fields.get("vnp_BankCode");
            String payDate = fields.get("vnp_PayDate");

            // Calculate secure hash
            String signValue = VNPayConfig.hashAllFields(fields);
            log.info("Calculated hash: {}", signValue);
            log.info("Received hash: {}", vnp_SecureHash);

            if (signValue.equals(vnp_SecureHash)) {
                // Signature matches, process the payment
                if ("00".equals(vnp_ResponseCode)) {
                    Bill bill = billRepository.findByUuid(orderId);
                    if (bill != null) {
                        long vnpayAmount = Long.parseLong(amount) / 100;
                        if (vnpayAmount == bill.getTotal()) {
                            bill.setOrderStatus(OrderStatus.COMPLETED);
                            bill.setLastUpdatedDate(LocalDateTime.now());
                            bill.setBankCode(bankCode);
                            bill.setPayDate(payDate);
                            billRepository.save(bill);
                            return "Payment Success";
                        } else {
                            bill.setOrderStatus(OrderStatus.FAILED);
                            bill.setLastUpdatedDate(LocalDateTime.now());
                            billRepository.save(bill);
                            return "Payment Failed - Amount mismatch";
                        }
                    }
                    return "Payment Failed - Bill not found";
                } else {
                    // Payment failed according to VNPay
                    Bill bill = billRepository.findByUuid(orderId);
                    if (bill != null) {
                        bill.setOrderStatus(OrderStatus.FAILED);
                        bill.setLastUpdatedDate(LocalDateTime.now());
                        billRepository.save(bill);
                    }
                    return "Payment Failed - " + getResponseDescription(vnp_ResponseCode);
                }
            } else {
                log.error("Invalid signature. Expected: {}, Got: {}", signValue, vnp_SecureHash);
                return "Payment Failed - Invalid signature";
            }
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            return "Payment Failed - System error: " + e.getMessage();
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