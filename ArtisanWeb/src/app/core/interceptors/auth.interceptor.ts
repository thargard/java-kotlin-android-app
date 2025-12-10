import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Interceptor для добавления токена авторизации к запросам
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Получаем токен из localStorage или другого хранилища
  const token = localStorage.getItem('authToken');

  // Если токен существует, клонируем запрос и добавляем заголовок Authorization
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req);
};
