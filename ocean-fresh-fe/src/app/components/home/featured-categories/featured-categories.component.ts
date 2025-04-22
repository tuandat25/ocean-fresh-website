import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-featured-categories',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './featured-categories.component.html',
  styleUrl: './featured-categories.component.scss'
})
export class FeaturedCategoriesComponent {
  categories = [
    { id: 'shrimp', name: 'Tôm', image: 'assets/images/categories/shrimp.png' },
    { id: 'crab', name: 'Cua', image: 'assets/images/categories/crab.png' },
    { id: 'fish', name: 'Cá', image: 'assets/images/categories/fish.png' },
    { id: 'squid', name: 'Mực', image: 'assets/images/categories/squid.png' },
    { id: 'dried', name: 'Hải sản khô', image: 'assets/images/categories/dried.png' }
  ];
}
