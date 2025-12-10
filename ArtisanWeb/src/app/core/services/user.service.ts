import { Injectable } from '@angular/core';
import { BaseHttpService } from './base-http.service';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  PaginatedResponse,
  PaginationParams,
} from '../models/api-response.model';

/**
 * Пример сервиса для работы с пользователями
 * Можно использовать как шаблон для других сервисов
 */
@Injectable({
  providedIn: 'root',
})
export class UserService extends BaseHttpService {
  private endpoint = '/users';

  /**
   * Получить список пользователей
   */
  getUsers(params?: PaginationParams): Observable<PaginatedResponse<any>> {
    return this.get<PaginatedResponse<any>>(this.endpoint, params);
  }

  /**
   * Получить пользователя по ID
   */
  getUserById(id: number | string): Observable<ApiResponse<any>> {
    return this.get<ApiResponse<any>>(`${this.endpoint}/${id}`);
  }

  /**
   * Создать нового пользователя
   */
  createUser(user: any): Observable<ApiResponse<any>> {
    return this.post<ApiResponse<any>>(this.endpoint, user);
  }

  /**
   * Обновить пользователя
   */
  updateUser(id: number | string, user: any): Observable<ApiResponse<any>> {
    return this.put<ApiResponse<any>>(`${this.endpoint}/${id}`, user);
  }

  /**
   * Удалить пользователя
   */
  deleteUser(id: number | string): Observable<ApiResponse<void>> {
    return this.delete<ApiResponse<void>>(`${this.endpoint}/${id}`);
  }
}
