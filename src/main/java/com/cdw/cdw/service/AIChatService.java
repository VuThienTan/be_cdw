package com.cdw.cdw.service;

import com.cdw.cdw.domain.entity.ChatMessage;
import com.cdw.cdw.domain.enums.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class AIChatService {

    private final Random random = new Random();

    @Autowired
    private ChatHistoryService chatHistoryService;

    public String processMessage(String userMessage, String username, String sessionId) {
        String message = userMessage.toLowerCase();
        String response;

        // Xử lý tin nhắn và tạo phản hồi
        response = generateResponse(message);

        // Lưu tin nhắn của người dùng
        ChatMessage userChatMessage = new ChatMessage();
        userChatMessage.setSender(username);
        userChatMessage.setContent(userMessage);
        userChatMessage.setType(MessageType.CHAT);
        chatHistoryService.saveMessage(userChatMessage, sessionId);

        // Lưu phản hồi của bot
        ChatMessage botChatMessage = new ChatMessage();
        botChatMessage.setSender("AI Assistant");
        botChatMessage.setContent(response);
        botChatMessage.setType(MessageType.BOT);
        chatHistoryService.saveMessage(botChatMessage, sessionId);

        return response;
    }

    private String generateResponse(String message) {
        // Chào hỏi
        if (message.contains("hello") || message.contains("hi") || message.contains("xin chào") || message.contains("chào")) {
            return getRandomResponse(greetings);
        }

        // Thông tin về menu
        else if (message.contains("menu") || message.contains("món ăn") || message.contains("đồ ăn") || message.contains("thực đơn")) {
            return getRandomResponse(menuResponses);
        }

        // Đặt bàn
        else if (message.contains("đặt bàn") || message.contains("đặt chỗ") || message.contains("reservation")) {
            return getRandomResponse(reservationResponses);
        }

        // Giờ mở cửa
        else if (message.contains("giờ") || message.contains("mở cửa") || message.contains("đóng cửa") || message.contains("thời gian")) {
            return getRandomResponse(openingHoursResponses);
        }

        // Khuyến mãi
        else if (message.contains("khuyến mãi") || message.contains("giảm giá") || message.contains("ưu đãi") || message.contains("voucher")) {
            return getRandomResponse(promotionResponses);
        }

        // Thanh toán
        else if (message.contains("thanh toán") || message.contains("payment") || message.contains("trả tiền") || message.contains("visa") || message.contains("mastercard")) {
            return getRandomResponse(paymentResponses);
        }

        // Giao hàng
        else if (message.contains("giao hàng") || message.contains("delivery") || message.contains("vận chuyển") || message.contains("ship")) {
            return getRandomResponse(deliveryResponses);
        }

        // Hỏi về món đặc biệt
        else if (message.contains("đặc biệt") || message.contains("nổi tiếng") || message.contains("signature") || message.contains("nên ăn")) {
            return getRandomResponse(specialDishResponses);
        }

        // Trợ giúp
        else if (message.contains("help") || message.contains("trợ giúp") || message.contains("hướng dẫn")) {
            return "Tôi có thể giúp bạn với: thông tin về menu, đặt bàn, giờ mở cửa, khuyến mãi, phương thức thanh toán, giao hàng, và gợi ý món ăn đặc biệt. Bạn cần hỗ trợ gì?";
        }

        // Câu trả lời mặc định
        else {
            return "Tôi chỉ là bot AI trả lời được một số câu hỏi liên quan. Những vấn đề tôi không thể giải đáp vui lòng liên hệ với admin thông qua số điện thoại: 0898388564 hoặc email: 21130149@st.hcmuaf.edu.vn";
        }
    }

    private String getRandomResponse(List<String> responses) {
        return responses.get(random.nextInt(responses.size()));
    }

    // Danh sách các câu trả lời
    private final List<String> greetings = Arrays.asList(
            "Xin chào! Tôi là trợ lý ảo của nhà hàng. Tôi có thể giúp gì cho bạn?",
            "Chào mừng bạn đến với nhà hàng của chúng tôi! Bạn muốn tìm hiểu về món ăn nào?",
            "Xin chào quý khách! Hôm nay bạn muốn thưởng thức món gì?",
            "Chào bạn! Tôi có thể giới thiệu menu hoặc giúp bạn đặt bàn. Bạn cần gì?"
    );

    // Các danh sách khác giữ nguyên...
    private final List<String> menuResponses = Arrays.asList(
            "Menu của chúng tôi có nhiều món đặc sắc như: Bò lúc lắc, Cá hồi nướng, Gỏi cuốn tôm thịt, và nhiều món khác. Bạn muốn xem chi tiết món nào?",
            "Nhà hàng chúng tôi phục vụ đa dạng các món Á - Âu. Bạn thích ẩm thực nào?",
            "Thực đơn hôm nay có món mới là Cua sốt tiêu đen và Bò Wellington. Bạn có muốn đặt không?",
            "Chúng tôi có menu đặc biệt cho bữa trưa với giá ưu đãi. Bạn muốn xem chi tiết không?"
    );

    private final List<String> reservationResponses = Arrays.asList(
            "Để đặt bàn, bạn vui lòng cho biết ngày, giờ và số lượng khách. Chúng tôi sẽ kiểm tra và xác nhận lại với bạn.",
            "Bạn có thể đặt bàn qua website hoặc gọi số 0123.456.789. Bạn muốn đặt bàn cho mấy người và vào thời gian nào?",
            "Chúng tôi khuyến khích đặt bàn trước 24 giờ để đảm bảo có chỗ tốt nhất. Bạn dự định đến vào ngày nào?",
            "Để có trải nghiệm tốt nhất, bạn nên đặt bàn trước. Bạn muốn đặt cho sự kiện đặc biệt không?"
    );

    private final List<String> openingHoursResponses = Arrays.asList(
            "Nhà hàng mở cửa từ 10:00 đến 22:00 các ngày trong tuần, cuối tuần từ 9:00 đến 23:00.",
            "Chúng tôi phục vụ từ 10 giờ sáng đến 10 giờ tối hàng ngày. Bạn muốn đến lúc mấy giờ?",
            "Nhà hàng hoạt động tất cả các ngày trong tuần, kể cả ngày lễ, từ 10:00 đến 22:00.",
            "Bếp đóng cửa lúc 21:30, vì vậy bạn nên đặt món trước thời gian này nhé."
    );

    private final List<String> promotionResponses = Arrays.asList(
            "Hiện tại chúng tôi có chương trình giảm 20% cho hóa đơn trên 500.000đ vào thứ Ba và thứ Tư.",
            "Khách hàng mới sẽ được tặng món tráng miệng khi đặt bàn online lần đầu.",
            "Chúng tôi đang có ưu đãi mua 1 tặng 1 cho các loại cocktail vào Happy Hour (17:00-19:00) hàng ngày.",
            "Nhân dịp khai trương, chúng tôi giảm 15% tổng hóa đơn cho tất cả khách hàng đến ngày 30 tháng này."
    );

    private final List<String> paymentResponses = Arrays.asList(
            "Chúng tôi chấp nhận thanh toán bằng tiền mặt, thẻ tín dụng/ghi nợ và các ví điện tử như Momo, ZaloPay.",
            "Bạn có thể thanh toán trực tuyến khi đặt hàng hoặc trả tiền khi nhận hàng (COD).",
            "Để đảm bảo an toàn, tất cả giao dịch thẻ đều được mã hóa và bảo mật theo tiêu chuẩn quốc tế.",
            "Chúng tôi có tính năng chia hóa đơn nếu bạn muốn thanh toán riêng trong nhóm."
    );

    private final List<String> deliveryResponses = Arrays.asList(
            "Chúng tôi giao hàng miễn phí trong bán kính 5km cho đơn hàng từ 200.000đ.",
            "Thời gian giao hàng dự kiến từ 30-45 phút tùy khoảng cách và thời điểm trong ngày.",
            "Bạn có thể theo dõi đơn hàng realtime qua ứng dụng của chúng tôi sau khi đặt hàng.",
            "Chúng tôi đảm bảo giao hàng đúng giờ hoặc hoàn tiền 50% giá trị đơn hàng."
    );

    private final List<String> specialDishResponses = Arrays.asList(
            "Món đặc trưng của nhà hàng là Bò Wellington và Cá hồi áp chảo sốt chanh dây, được đầu bếp 5 sao chế biến.",
            "Khách hàng rất yêu thích món Sườn cừu nướng và Pasta hải sản của chúng tôi. Bạn nên thử!",
            "Signature dish của chúng tôi là Bò Wagyu A5 nướng với muối biển và Risotto nấm truffle.",
            "Tôi khuyên bạn nên thử set menu đặc biệt của bếp trưởng, bao gồm 5 món từ khai vị đến tráng miệng với giá 650.000đ/người."
    );
}
