import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-why-choose-us',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './why-choose-us.component.html',
  styleUrl: './why-choose-us.component.scss'
})
export class WhyChooseUsComponent {
  features = [
    {
      icon: 'check-circle',
      title: 'Hải sản sạch 100%',
      description: 'Nguồn gốc rõ ràng, đảm bảo vệ sinh an toàn thực phẩm'
    },
    {
      icon: 'bolt',
      title: 'Giao hàng nhanh 2h',
      description: 'Giao hàng nhanh chóng trong vòng 2h tại nội thành'
    },
    {
      icon: 'snowflake',
      title: 'Đóng gói chuẩn lạnh',
      description: 'Bảo quản sản phẩm tươi ngon trong suốt quá trình vận chuyển'
    },
    {
      icon: 'headset',
      title: 'Hỗ trợ 24/7',
      description: 'Đội ngũ tư vấn viên luôn sẵn sàng hỗ trợ bạn mọi lúc'
    }
  ];
}
