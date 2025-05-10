import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, retry, tap } from 'rxjs/operators';

export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  discount?: number;
  rating: number;
  reviewCount: number;
  image: string;
  dateAdded: string | Date;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'https://api.ocean-fresh.com/products'; // Thay bằng URL API thực tế của bạn
  private cachedProducts: Product[] | null = null;

  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    // Sử dụng cache để tránh gọi API nhiều lần
    if (this.cachedProducts) {
      console.log('Trả về dữ liệu từ cache');
      return of(this.cachedProducts);
    }

    return this.http.get<Product[]>(this.apiUrl).pipe(
      retry(2), // Thử lại 2 lần nếu có lỗi kết nối
      tap(products => {
        console.log('Dữ liệu sản phẩm từ API:', products);
        this.cachedProducts = products; // Lưu vào cache
      }),
      catchError(this.handleError)
    );
  }

  // Xử lý lỗi từ API
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Đã xảy ra lỗi khi tải dữ liệu sản phẩm';
    
    if (error.error instanceof ErrorEvent) {
      // Lỗi phía client
      errorMessage = `Lỗi: ${error.error.message}`;
    } else {
      // Lỗi phía server
      errorMessage = `Mã lỗi: ${error.status}, Thông báo: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
