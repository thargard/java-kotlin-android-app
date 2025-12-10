import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

/**
 * Interceptor для обработки HTTP ошибок
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = '';

      if (error.error instanceof ErrorEvent) {
        // Клиентская ошибка
        errorMessage = `Ошибка: ${error.error.message}`;
      } else {
        // Серверная ошибка
        switch (error.status) {
          case 400:
            errorMessage = 'Неверный запрос';
            break;
          case 401:
            errorMessage = 'Требуется авторизация';
            // Перенаправление на страницу входа
            router.navigate(['/login']);
            break;
          case 403:
            errorMessage = 'Доступ запрещен';
            break;
          case 404:
            errorMessage = 'Ресурс не найден';
            break;
          case 500:
            errorMessage = 'Внутренняя ошибка сервера';
            break;
          case 503:
            errorMessage = 'Сервис недоступен';
            break;
          default:
            errorMessage =
              error.error?.message || `Ошибка сервера: ${error.status}`;
        }
      }

      console.error('HTTP Error:', errorMessage, error);

      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        error: error.error,
        timestamp: new Date().toISOString(),
      }));
    })
  );
};
