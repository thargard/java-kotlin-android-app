import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  PaginatedResponse,
  PaginationParams,
} from '../models/api-response.model';

/**
 * Базовый HTTP сервис для работы с API
 */
@Injectable({
  providedIn: 'root',
})
export class BaseHttpService {
  protected apiUrl = environment.apiUrl;

  constructor(protected http: HttpClient) {}

  /**
   * GET запрос
   */
  protected get<T>(
    endpoint: string,
    params?: any,
    options?: { headers?: HttpHeaders }
  ): Observable<T> {
    const httpParams = this.buildParams(params);
    return this.http.get<T>(`${this.apiUrl}${endpoint}`, {
      params: httpParams,
      ...options,
    });
  }

  /**
   * POST запрос
   */
  protected post<T>(
    endpoint: string,
    body: any,
    options?: { headers?: HttpHeaders }
  ): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}${endpoint}`, body, options);
  }

  /**
   * PUT запрос
   */
  protected put<T>(
    endpoint: string,
    body: any,
    options?: { headers?: HttpHeaders }
  ): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}${endpoint}`, body, options);
  }

  /**
   * PATCH запрос
   */
  protected patch<T>(
    endpoint: string,
    body: any,
    options?: { headers?: HttpHeaders }
  ): Observable<T> {
    return this.http.patch<T>(`${this.apiUrl}${endpoint}`, body, options);
  }

  /**
   * DELETE запрос
   */
  protected delete<T>(
    endpoint: string,
    options?: { headers?: HttpHeaders }
  ): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}${endpoint}`, options);
  }

  /**
   * Построение HttpParams из объекта
   */
  private buildParams(params?: any): HttpParams {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach((key) => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key].toString());
        }
      });
    }
    return httpParams;
  }
}
