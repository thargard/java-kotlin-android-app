// src/app/core/services/rating.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rating, RatingRequest, RatingStats } from '../models/rating.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RatingService {
  private apiUrl = `${environment.apiUrl}/ratings`;

  constructor(private http: HttpClient) {}

  /**
   * Создать или обновить рейтинг
   */
  createOrUpdateRating(customerId: number, request: RatingRequest): Observable<Rating> {
    const headers = new HttpHeaders({
      'customerId': customerId.toString()
    });
    return this.http.post<Rating>(this.apiUrl, request, { headers });
  }

  /**
   * Получить рейтинг, который заказчик поставил исполнителю
   */
  getRatingByCustomerAndProducer(customerId: number, producerId: number): Observable<Rating> {
    return this.http.get<Rating>(`${this.apiUrl}/customer/${customerId}/producer/${producerId}`);
  }

  /**
   * Получить все рейтинги для исполнителя
   */
  getRatingsByProducer(producerId: number): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${this.apiUrl}/producer/${producerId}`);
  }

  /**
   * Получить статистику рейтинга исполнителя
   */
  getProducerRatingStats(producerId: number): Observable<RatingStats> {
    return this.http.get<RatingStats>(`${this.apiUrl}/producer/${producerId}/stats`);
  }

  /**
   * Проверить, поставил ли заказчик оценку исполнителю
   */
  hasUserRatedProducer(customerId: number, producerId: number): Observable<boolean> {
    const params = new HttpParams()
      .set('customerId', customerId.toString())
      .set('producerId', producerId.toString());
    return this.http.get<boolean>(`${this.apiUrl}/check`, { params });
  }

  /**
   * Удалить рейтинг
   */
  deleteRating(customerId: number, producerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/customer/${customerId}/producer/${producerId}`);
  }
}