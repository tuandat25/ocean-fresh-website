import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, retry, tap, map } from 'rxjs/operators';

export interface ApiResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface Product {
  id: number;
  name: string;
  price: number;
  thumbnail: string | null;
  image?: string; // Để tương thích với UI cũ
  quantity: number;
  soldQuantity: number;
  description: string;
  category?: string; // Để tương thích với UI cũ
  rating?: number; // Để tương thích với UI cũ
  reviewCount?: number; // Để tương thích với UI cũ
  discount?: number; // Để tương thích với UI cũ
  dateAdded?: Date; // Để tương thích với UI cũ
  created_at: string;
  updated_at: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8088/api/v1/products';
  private cachedProducts: Product[] = [];
  private cachedTotalPages = 0;

  constructor(private http: HttpClient) {}  getProducts(page: number = 0, limit: number = 40): Observable<{products: Product[], totalPages: number}> {
    // Không sử dụng cache để luôn tải dữ liệu mới từ API theo trang hiện tại
    
    console.log(`Fetching products - page: ${page}, limit: ${limit}`);
    
    // Đảm bảo các tham số đều là chuỗi và là số không âm
    const safePageIndex = Math.max(0, page);
    const safeLimit = Math.max(1, limit);
    
    let params = new HttpParams()
      .set('page', safePageIndex.toString())
      .set('limit', safeLimit.toString());
    
    // Log URL request để debug
    const requestUrl = `${this.apiUrl}?page=${safePageIndex}&limit=${safeLimit}`;
    console.log('Request URL:', requestUrl);
      
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      retry(1), // Giảm số lần retry để phát hiện lỗi nhanh hơn
      map(response => {
        console.log('Raw API response:', response); // Log raw response to help debug

        // Kiểm tra cấu trúc phản hồi
        let products = [];
        let totalPages = 0;

        if (response && response.data && response.data.products && Array.isArray(response.data.products)) {
          // Cấu trúc API với response.data.products là một mảng
          products = response.data.products;
          totalPages = response.data.totalPages || 0;
        } else if (response && response.content && Array.isArray(response.content)) {
          // Cấu trúc API với response.content là một mảng
          products = response.content;
          totalPages = response.totalPages || 0;
        } else if (Array.isArray(response)) {
          // Nếu response trực tiếp là một mảng
          products = response;
          totalPages = response.length > 0 && response[0].totalPages ? response[0].totalPages : 1;
        } else {
          // Trường hợp không nhận được mảng hợp lệ
          console.warn('API response không có cấu trúc mảng như mong đợi:', response);
          products = [];
          totalPages = 0;
        }
        
        // Map từ backend đến frontend model
        const mappedProducts = products.map((p: any) => this.mapProductFromApi(p));
        
        // Lưu vào cache
        this.cachedProducts = mappedProducts;
        this.cachedTotalPages = totalPages;
        
        return {
          products: mappedProducts,
          totalPages: totalPages
        };
      }),
      tap(data => console.log('Dữ liệu sản phẩm đã xử lý:', data)),
      catchError(this.handleError)
    );
  }  // Mapping từ backend model sang frontend model
  private mapProductFromApi(apiProduct: any): Product {
    console.log('API Product data:', apiProduct); // Log để debug dữ liệu từ API
    
    if (!apiProduct) {
      console.error('Received null or undefined product from API');
      return {
        id: 0,
        name: 'Sản phẩm không hợp lệ',
        price: 0,
        thumbnail: null,
        image: 'assets/images/noimage.jpg',
        quantity: 0,
        soldQuantity: 0,
        description: '',
        category: 'Hải sản',
        rating: 0,
        reviewCount: 0,
        discount: 0,
        created_at: '',
        updated_at: ''
      };
    }
    
    return {
      id: apiProduct.id || 0,
      name: apiProduct.name || 'Chưa có tên',
      price: apiProduct.price || 0,
      thumbnail: apiProduct.thumbnail || null,
      // Map các trường để tương thích với UI cũ
      image: apiProduct.thumbnail || 'assets/images/noimage.jpg', // Sử dụng placeholder khi thumbnail là null
      quantity: apiProduct.quantity || 0,
      soldQuantity: apiProduct.soldQuantity || 0,
      description: apiProduct.description || '',
      category: 'Hải sản', // Placeholder, có thể thay đổi khi API có category
      rating: 4.5, // Placeholder, có thể thay đổi khi API có rating
      reviewCount: apiProduct.soldQuantity || 0, // Tạm dùng soldQuantity làm reviewCount
      discount: 0, // Placeholder, có thể thay đổi khi API có discount
      dateAdded: apiProduct.created_at ? new Date(apiProduct.created_at) : new Date(), // Dùng created_at làm dateAdded
      created_at: apiProduct.created_at || '',
      updated_at: apiProduct.updated_at || ''
    };
  }
  // Xóa cache để load lại dữ liệu mới từ API
  clearCache(): void {
    this.cachedProducts = [];
    this.cachedTotalPages = 0;
  }

  // Xử lý lỗi từ API
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi khi tải dữ liệu sản phẩm';
    
    if (error.error instanceof ErrorEvent) {
      // Lỗi phía client
      errorMessage = `Lỗi: ${error.error.message}`;
    } else if (error.status === 0) {
      // Lỗi kết nối hoặc CORS
      errorMessage = `Không thể kết nối đến máy chủ API. Vui lòng kiểm tra kết nối và CORS.`;
      console.error('Connection error details:', error);
    } else {
      // Lỗi phía server
      errorMessage = `Mã lỗi: ${error.status}, Thông báo: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}