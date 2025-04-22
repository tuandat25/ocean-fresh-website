import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.scss'
})
export class ChatbotComponent {
  isOpen = false;
  userMessage = '';
  messages: {type: string, text: string, time: string}[] = [];

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.messages.length === 0) {
      this.addBotMessage('Xin chào! Tôi là trợ lý ảo của Ocean Fresh. Tôi có thể giúp gì cho bạn?');
    }
  }

  sendMessage() {
    if (this.userMessage.trim() !== '') {
      // Thêm tin nhắn của người dùng
      this.addUserMessage(this.userMessage);
      
      // Lưu tin nhắn vào biến tạm để xử lý
      const message = this.userMessage;
      this.userMessage = '';
      
      // Mô phỏng phản hồi từ chatbot sau 1 giây
      setTimeout(() => {
        this.handleBotResponse(message);
      }, 1000);
    }
  }

  addUserMessage(text: string) {
    this.messages.push({
      type: 'user',
      text: text,
      time: this.getCurrentTime()
    });
  }

  addBotMessage(text: string) {
    this.messages.push({
      type: 'bot',
      text: text,
      time: this.getCurrentTime()
    });
  }

  handleBotResponse(message: string) {
    // Đây là logic đơn giản để mô phỏng chatbot
    message = message.toLowerCase();
    
    if (message.includes('giá') || message.includes('bảng giá')) {
      this.addBotMessage('Bạn có thể xem bảng giá đầy đủ của chúng tôi trong mục Sản phẩm. Hoặc bạn muốn tìm hiểu về sản phẩm cụ thể nào?');
    } else if (message.includes('giao hàng') || message.includes('ship') || message.includes('vận chuyển')) {
      this.addBotMessage('Ocean Fresh giao hàng nhanh trong vòng 2h đối với khu vực nội thành và 24h cho các tỉnh lân cận. Phí ship sẽ được tính dựa trên khoảng cách.');
    } else if (message.includes('tôm') || message.includes('cua') || message.includes('cá') || message.includes('mực')) {
      this.addBotMessage('Chúng tôi có nhiều loại hải sản tươi sống với giá cả hợp lý. Bạn có thể xem chi tiết trong mục Sản phẩm hoặc để lại số điện thoại để nhân viên tư vấn gọi lại cho bạn.');
    } else {
      this.addBotMessage('Cảm ơn bạn đã liên hệ với Ocean Fresh. Vui lòng cho biết thêm thông tin để tôi có thể hỗ trợ bạn tốt hơn, hoặc để lại số điện thoại để được tư vấn trực tiếp.');
    }
  }

  getCurrentTime(): string {
    const now = new Date();
    return now.getHours() + ':' + (now.getMinutes() < 10 ? '0' : '') + now.getMinutes();
  }
}
