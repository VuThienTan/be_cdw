package com.cdw.cdw.service;

import com.cdw.cdw.domain.entity.Orders;
import com.cdw.cdw.domain.entity.OrdersItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + token);
        context.setVariable("name", to.split("@")[0]); // Lấy phần trước @ làm tên

        String emailContent = templateEngine.process("password-reset-template", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu");
        helper.setText(emailContent, true);

        mailSender.send(message);
    }

    public void sendAccountActivationEmail(String to, String activationCode, String name) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("activationUrl", frontendUrl + "/activate?code=" + activationCode);
        context.setVariable("name", to.split("@")[0]); // Lấy tên từ email

        String emailContent = templateEngine.process("account-activation-template", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Kích hoạt tài khoản");
        helper.setText(emailContent, true);

        mailSender.send(message);
    }

    public void sendInvoiceEmail(String to, Orders order) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Gán dữ liệu vào Thymeleaf context
        Context context = new Context();
        context.setVariable("customerName", order.getUser().getFullName());
        context.setVariable("orderCode", order.getId());
        context.setVariable("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Tính subtotal (tạm tính)
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrdersItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setVariable("subtotal", subtotal);

        // Ví dụ discount là 0 nếu chưa có voucher
        BigDecimal discount = BigDecimal.ZERO; // Hoặc lấy từ order nếu có
        context.setVariable("discount", discount);

        // Tổng cộng = subtotal - discount
        BigDecimal total = subtotal.subtract(discount);
        context.setVariable("total", total);

        // Chuẩn bị danh sách sản phẩm gửi vào Thymeleaf
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (OrdersItem item : order.getOrderItems()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", item.getMenuItem().getName());          // Tên món
            map.put("quantity", item.getQuantity());                // Số lượng
            map.put("unitPrice", item.getUnitPrice());              // Đơn giá (BigDecimal)
            map.put("totalPrice", item.getTotalPrice());            // Tổng tiền của item (unitPrice * quantity)
            itemList.add(map);
        }
        context.setVariable("items", itemList);

        // Render nội dung email từ template
        String emailContent = templateEngine.process("invoice-email-template", context);

        // Cấu hình email
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Hóa đơn đơn hàng #" + order.getId());
        helper.setText(emailContent, true); // HTML

        mailSender.send(message);
    }
}

