import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';

export interface PaginatedProducts {
  content: Product[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface CartResponse {
  message: string;
  cartItemId?: number;
  productId: number;
  quantity?: number;
  totalPrice?: number;
}

export interface BuyResponse {
  message: string;
  orderId: number;
  productId: number;
  productName: string;
  price: number;
  sellerId: number;
  sellerName: string;
  orderStatus: string;
  createdAt: string;
}

export interface ContactResponse {
  message: string;
  threadId: number;
  productId: number;
  sellerId: number;
  sellerName: string;
  messageId: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private baseUrl = environment.apiUrl + '/products';

  constructor(private http: HttpClient) {}

  /**
   * Get all products with optional filtering
   */
  getProducts(filters?: {
    page?: number;
    size?: number;
    category?: string;
    search?: string;
    sellerId?: number;
    availableOnly?: boolean;
  }): Observable<PaginatedProducts> {
    let params = new HttpParams();
    if (filters?.page !== undefined) params = params.set('page', filters.page);
    if (filters?.size !== undefined) params = params.set('size', filters.size);
    if (filters?.category) params = params.set('category', filters.category);
    if (filters?.search) params = params.set('search', filters.search);
    if (filters?.sellerId) params = params.set('sellerId', filters.sellerId.toString());
    if (filters?.availableOnly) params = params.set('availableOnly', 'true');
    return this.http.get<PaginatedProducts>(this.baseUrl, { params });
  }

  /**
   * Get product by ID
   */
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  /**
   * Create a new product
   */
  createProduct(product: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, product);
  }

  /**
   * Update product
   */
  updateProduct(id: number, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, product);
  }

  /**
   * Delete product
   */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /**
   * Add product to cart
   */
  addToCart(productId: number, quantity?: number): Observable<CartResponse> {
    const body = quantity ? { quantity } : {};
    return this.http.post<CartResponse>(`${this.baseUrl}/${productId}/cart`, body);
  }

  /**
   * Purchase product directly
   */
  buyNow(productId: number): Observable<BuyResponse> {
    return this.http.post<BuyResponse>(`${this.baseUrl}/${productId}/buy`, {});
  }

  /**
   * Contact seller (start chat)
   */
  contactSeller(productId: number, message?: string): Observable<ContactResponse> {
    const body = message ? { message } : {};
    return this.http.post<ContactResponse>(`${this.baseUrl}/${productId}/contact`, body);
  }
}
