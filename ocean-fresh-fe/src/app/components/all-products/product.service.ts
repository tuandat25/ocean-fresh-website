import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Product {
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
  private apiUrl = 'https://your-api-url.com/products'; // Thay bằng URL thực tế

  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }
}
