import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-blog-posts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './blog-posts.component.html',
  styleUrl: './blog-posts.component.scss'
})
export class BlogPostsComponent {
  blogPosts = [
    {
      id: 1,
      title: 'Cách chọn hải sản tươi ngon',
      image: 'assets/images/blog/post-1.jpg',
      excerpt: 'Những bí quyết giúp bạn chọn được hải sản tươi ngon, đảm bảo chất lượng và an toàn thực phẩm.',
      date: '10/04/2025'
    },
    {
      id: 2,
      title: '5 món ngon từ cua biển',
      image: 'assets/images/blog/post-2.jpg',
      excerpt: 'Khám phá 5 cách chế biến cua biển thơm ngon, bổ dưỡng mà không làm mất đi hương vị tự nhiên của cua.',
      date: '05/04/2025'
    },
    {
      id: 3,
      title: 'Bảo quản hải sản đúng cách',
      image: 'assets/images/blog/post-3.jpg',
      excerpt: 'Hướng dẫn chi tiết cách bảo quản hải sản tại nhà để giữ được độ tươi ngon và dinh dưỡng.',
      date: '01/04/2025'
    }
  ];
}
