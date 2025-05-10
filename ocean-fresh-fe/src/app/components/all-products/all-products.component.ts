import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProductService, Product } from '../services/product.service';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { HttpClientModule } from '@angular/common/http';

// Using the Product interface from ProductService

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
  imports: [CommonModule, FormsModule, ReactiveFormsModule, HttpClientModule]
})
export class AllProductsComponent implements OnInit, OnDestroy {
  // Products data
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
  filterForm: FormGroup;    // Pagination
  currentPage = 1;
  itemsPerPage = 12; // Thiết lập cho phù hợp với limit API mặc định
  totalPages = 1;
  
  // Sorting
  currentSort = 'newest';
  
  // Loading & error states
  isLoading = false;
  error: string | null = null;
  
  // For use in template
  Math = Math;
  
  // Subscription management
  private subscriptions = new Subscription();
  
  constructor(
    private fb: FormBuilder,
    private productService: ProductService
  ) {
    this.filterForm = this.fb.group({
      category: [''],
      priceRange: ['']
    });
  }
    ngOnInit(): void {
    this.loadProducts();
    
    // Subscribe to form changes
    this.subscriptions.add(
      this.filterForm.valueChanges.subscribe(() => {
        this.applyFiltersAndSort();
      })
    );
  }
  
  ngOnDestroy(): void {
    // Unsubscribe to avoid memory leaks
    this.subscriptions.unsubscribe();
  }  loadProducts(): void {
    this.isLoading = true;
    this.error = null;
    
    console.log('Loading page:', this.currentPage, 'with items per page:', this.itemsPerPage);
    
    // Chuyển đổi từ currentPage sang page index (page 1 -> index 0)
    const pageIndex = this.currentPage - 1;
    
    // Hiển thị URL sẽ được gọi để debug
    console.log(`API URL will be: http://localhost:8088/api/v1/products?page=${pageIndex}&limit=${this.itemsPerPage}`);
    
    this.subscriptions.add(
      this.productService.getProducts(pageIndex, this.itemsPerPage)
        .pipe(
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe({
          next: (data) => {
            console.log('Products received:', data.products);
            console.log('Total pages:', data.totalPages);
            this.products = data.products;
            this.totalPages = data.totalPages;
            // Không cần phân trang lại vì chúng ta đang dùng phân trang server-side
            this.pagedProducts = this.products;
            // Áp dụng filter và sort 
            this.applyFiltersAndSort();
          },
          error: (err) => {
            this.error = 'Không thể tải dữ liệu sản phẩm';
            console.error('Error loading products:', err);
          }
        })
    );
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
    
    // Nếu chúng ta sử dụng bộ lọc, lúc này chúng ta cần xử lý phân trang phía client
    // vì server không biết về bộ lọc của chúng ta
    this.pagedProducts = this.filteredProducts;
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
        this.filteredProducts.sort((a, b) => {
          const dateA = a.dateAdded instanceof Date ? a.dateAdded.getTime() : 0;
          const dateB = b.dateAdded instanceof Date ? b.dateAdded.getTime() : 0;
          return dateB - dateA;
        });
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
  }  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages || page === this.currentPage) {
      return; // Không làm gì nếu trang không hợp lệ hoặc đang ở trang đó
    }
    
    console.log('Navigating to page:', page);
    this.currentPage = page;
    
    // Tải lại sản phẩm với trang mới từ server
    this.loadProducts();
  }
  
  updatePagedProducts(): void {
    // Đối với phân trang server-side, chúng ta không cần cắt dữ liệu
    // vì server đã trả về đúng dữ liệu cho trang hiện tại
    this.pagedProducts = this.filteredProducts;
  }
  
  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }

  refreshProducts(): void {
    // Clear service cache
    this.productService.clearCache();
    // Reload products
    this.loadProducts();
  }
}