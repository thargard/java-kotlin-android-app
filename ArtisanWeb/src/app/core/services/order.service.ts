import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from './base-http.service';
import { GetOrdersParams, OrderDto } from '../models/order.model';

@Injectable({
  providedIn: 'root',
})
export class OrderService extends BaseHttpService {
  private readonly endpoint = '/orders';

  /**
   * Получить список заказов (с фильтрами/поиском/сортировкой).
   * Backend: GET /api/orders?userId=&status=&search=&createdAfter=&createdBefore=&sort=
   */
  getOrders(params?: GetOrdersParams): Observable<OrderDto[]> {
    return this.get<OrderDto[]>(this.endpoint, params);
  }
}
