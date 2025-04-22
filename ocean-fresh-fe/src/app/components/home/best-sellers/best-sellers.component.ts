import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-best-sellers',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './best-sellers.component.html',
  styleUrl: './best-sellers.component.scss'
})
export class BestSellersComponent {
  products = [
    {
      id: 1,
      name: 'Tôm sú tươi sống',
      image: 'assets/images/products/product-1.jpg',
      currentPrice: 289000,
      originalPrice: 340000,
      discount: 15,
      rating: 5,
      reviewCount: 124
    },
    {
      id: 2,
      name: 'Cua hoàng đế Alaska',
      image: 'assets/images/products/product-2.jpg',
      currentPrice: 1250000,
      originalPrice: 1500000,
      discount: 17,
      rating: 4.8,
      reviewCount: 86
    },
    {
      id: 3,
      name: 'Cá hồi Na Uy tươi',
      image: 'assets/images/products/product-3.jpg',
      currentPrice: 349000,
      originalPrice: 390000,
      discount: 10,
      rating: 4.9,
      reviewCount: 204
    },
    {
      id: 4,
      name: 'Mực ống tươi',
      image: 'assets/images/products/product-4.jpg',
      currentPrice: 210000,
      originalPrice: 250000,
      discount: 16,
      rating: 4.7,
      reviewCount: 95
    }
  ];

  formatPrice(price: number): string {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + 'đ';
  }
}
