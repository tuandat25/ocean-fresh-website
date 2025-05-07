import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  discount?: number;
  rating: number;
  reviewCount: number;
  image: string;
  dateAdded: Date;
}

interface PriceRange {
  label: string;
  value: string;
  min: number;
  max?: number;
}

interface SortOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-all-products',
  templateUrl: './all-products.component.html',
  styleUrls: ['./all-products.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class AllProductsComponent implements OnInit {
  // Mock data
  products: Product[] = [];
  filteredProducts: Product[] = [];
  pagedProducts: Product[] = [];
  
  // Filter options
  categories: string[] = ['Cá', 'Tôm', 'Cua & Ghẹ', 'Mực & Bạch Tuộc', 'Ốc & Hàu', 'Sò & Nghêu', 'Cá Khô'];
  
  priceRanges: PriceRange[] = [
    { label: 'Dưới 100,000đ', value: 'under-100k', min: 0, max: 100000 },
    { label: '100,000đ - 200,000đ', value: '100k-200k', min: 100000, max: 200000 },
    { label: '200,000đ - 300,000đ', value: '200k-300k', min: 200000, max: 300000 },
    { label: '300,000đ - 500,000đ', value: '300k-500k', min: 300000, max: 500000 },
    { label: 'Trên 500,000đ', value: 'over-500k', min: 500000 }
  ];
  
  // Sort options
  sortOptions: SortOption[] = [
    { label: 'Tên: A-Z', value: 'name-asc' },
    { label: 'Tên: Z-A', value: 'name-desc' },
    { label: 'Giá: Thấp đến cao', value: 'price-asc' },
    { label: 'Giá: Cao đến thấp', value: 'price-desc' },
    { label: 'Mới nhất', value: 'newest' }
  ];
  
  // Reactive form for filters
  filterForm: FormGroup;
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  
  // Sorting
  currentSort = 'newest';
  
  // For use in template
  Math = Math;
  
  constructor(private fb: FormBuilder) {
    this.filterForm = this.fb.group({
      category: [''],
      priceRange: ['']
    });
  }
  
  ngOnInit(): void {
    this.generateMockProducts();
    this.applyFiltersAndSort();
    
    // Subscribe to form changes
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFiltersAndSort();
    });
  }
  
  generateMockProducts(): void {
    // Generate 150 mock products
    const imageBaseUrls = [
      'https://images.unsplash.com/photo-1544551763-77ef2d0cfc6c',
      'https://images.unsplash.com/photo-1534948216015-843149f72be3',
      'https://images.unsplash.com/photo-1578735799461-85ee44851697',
      'https://images.unsplash.com/photo-1565794462772-817c6c8ce7b9',
      'https://images.unsplash.com/photo-1490897343135-a018124f5714'
    ];
    
    for (let i = 1; i <= 150; i++) {
      const categoryIndex = Math.floor(Math.random() * this.categories.length);
      const randomPrice = Math.floor(Math.random() * 800000) + 50000;
      const hasDiscount = Math.random() > 0.7;
      const discountPercentage = hasDiscount ? Math.floor(Math.random() * 30) + 5 : 0;
      const daysAgo = Math.floor(Math.random() * 60);
      const date = new Date();
      date.setDate(date.getDate() - daysAgo);
      
      this.products.push({
        id: i,
        name: `Sản phẩm ${this.categories[categoryIndex]} ${i}`,
        category: this.categories[categoryIndex],
        price: randomPrice,
        discount: hasDiscount ? discountPercentage : undefined,
        rating: 3 + Math.random() * 2,
        reviewCount: Math.floor(Math.random() * 200),
        image: `${imageBaseUrls[Math.floor(Math.random() * imageBaseUrls.length)]}?v=${i}`,
        dateAdded: date
      });
    }
  }
  
  applyFiltersAndSort(): void {
    const { category, priceRange } = this.filterForm.value;
    
    // Apply filters
    this.filteredProducts = this.products.filter(product => {
      // Category filter
      if (category && product.category !== category) {
        return false;
      }
      
      // Price range filter
      if (priceRange) {
        const selectedRange = this.priceRanges.find(range => range.value === priceRange);
        if (selectedRange) {
          if (selectedRange.max && (product.price < selectedRange.min || product.price > selectedRange.max)) {
            return false;
          } else if (!selectedRange.max && product.price < selectedRange.min) {
            return false;
          }
        }
      }
      
      return true;
    });
    
    // Apply sort
    this.sortProducts(this.currentSort);
    
    // Reset to first page when filters change
    this.goToPage(1);
  }
  
  sortProducts(sortType: string): void {
    this.currentSort = sortType;
    
    switch (sortType) {
      case 'name-asc':
        this.filteredProducts.sort((a, b) => a.name.localeCompare(b.name));
        break;
      case 'name-desc':
        this.filteredProducts.sort((a, b) => b.name.localeCompare(a.name));
        break;
      case 'price-asc':
        this.filteredProducts.sort((a, b) => a.price - b.price);
        break;
      case 'price-desc':
        this.filteredProducts.sort((a, b) => b.price - a.price);
        break;
      case 'newest':
        this.filteredProducts.sort((a, b) => b.dateAdded.getTime() - a.dateAdded.getTime());
        break;
    }
    
    this.updatePagedProducts();
  }
  
  selectCategory(category: string): void {
    const currentCategory = this.filterForm.get('category')?.value;
    
    // Toggle selection (if same category is clicked again, deselect it)
    if (currentCategory === category) {
      this.filterForm.patchValue({ category: '' });
    } else {
      this.filterForm.patchValue({ category });
    }
  }
  
  selectPriceRange(range: string): void {
    const currentRange = this.filterForm.get('priceRange')?.value;
    
    // Toggle selection
    if (currentRange === range) {
      this.filterForm.patchValue({ priceRange: '' });
    } else {
      this.filterForm.patchValue({ priceRange: range });
    }
  }
  
  goToPage(page: number): void {
    this.currentPage = page;
    this.updatePagedProducts();
  }
  
  updatePagedProducts(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    this.pagedProducts = this.filteredProducts.slice(startIndex, startIndex + this.itemsPerPage);
    this.totalPages = Math.ceil(this.filteredProducts.length / this.itemsPerPage);
  }
  
  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }
}