import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface CartItemDto {
  id: number;
  productId: number;
  productName: string;
  productImageUrl?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface CartResponse {
  items: CartItemDto[];
  totalItems: number;
  totalPrice: number;
}

export interface CheckoutResponse {
  orderIds: number[];
  totalPrice: number;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private baseUrl = environment.apiUrl + '/cart';

  constructor(private http: HttpClient) {}

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.baseUrl);
  }

  checkout(): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.baseUrl}/checkout`, {});
  }

  removeItem(productId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${productId}`);
  }

  updateQuantity(productId: number, quantity: number): Observable<CartItemDto | { message: string }> {
    return this.http.put<CartItemDto | { message: string }>(`${this.baseUrl}/${productId}`, { quantity });
  }

  getCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/count`);
  }
}
