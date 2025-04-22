import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonials.component.html',
  styleUrl: './testimonials.component.scss'
})
export class TestimonialsComponent {
  testimonials = [
    {
      name: 'Nguyễn Văn A',
      avatar: 'assets/images/avatars/user-1.jpg',
      rating: 5,
      comment: 'Hải sản tươi ngon, giao hàng nhanh chóng và đóng gói rất cẩn thận. Nhân viên tư vấn nhiệt tình. Sẽ ủng hộ shop dài dài!'
    },
    {
      name: 'Trần Thị B',
      avatar: 'assets/images/avatars/user-2.jpg',
      rating: 5,
      comment: 'Tôm mua ở đây to và tươi lắm. Giao hàng đúng giờ, đóng gói bằng đá lạnh nên khi nhận hàng vẫn rất tươi. Rất hài lòng!'
    },
    {
      name: 'Lê Văn C',
      avatar: 'assets/images/avatars/user-3.jpg',
      rating: 4.5,
      comment: 'Mua cua ở đây cực kỳ chất lượng, gạch nhiều, thịt ngọt. Đặc biệt là dịch vụ chăm sóc khách hàng rất tốt.'
    }
  ];
}
