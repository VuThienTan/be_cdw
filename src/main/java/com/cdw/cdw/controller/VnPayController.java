package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.enums.OrderStatus;
import com.cdw.cdw.repository.OrdersRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final OrdersRepository ordersRepository;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.paymentUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @PostMapping("/create_payment_url")
    public ResponseEntity<String> createPaymentUrl(
            @RequestParam("orderId") Long orderId,
            @RequestParam("amount") Long amount,
            HttpServletRequest request) {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang #" + orderId;
        String orderType = "other";
        String vnp_TxnRef = orderId.toString();
        String vnp_IpAddr = getIpAddress(request);
        String vnp_Locale = "vn";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // Nhân 100 vì VNPay tính bằng 100 đồng

        // Thời gian giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        // Sắp xếp tham số theo thứ tự a-z
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // Tạo chữ ký
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String paymentUrl = vnp_PayUrl + "?" + query;
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/payment_callback")
    public ApiResponse<String> paymentCallback(
            @RequestParam Map<String, String> requestParams) {

        String vnp_ResponseCode = requestParams.get("vnp_ResponseCode");
        String vnp_TxnRef = requestParams.get("vnp_TxnRef");

        if ("00".equals(vnp_ResponseCode)) {
            // Thanh toán thành công, cập nhật trạng thái đơn hàng
            Long orderId = Long.parseLong(vnp_TxnRef);
            Orders order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Cập nhật trạng thái đơn hàng
            order.setStatus(OrderStatus.CONFIRMED);
            ordersRepository.save(order);

            return ApiResponse.<String>builder()
                    .result("Thanh toán thành công")
                    .build();
        } else {
            // Thanh toán thất bại
            return ApiResponse.<String>builder()
                    .result("Thanh toán thất bại")
                    .build();
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
