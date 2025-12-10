import { HttpInterceptorFn } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { inject } from '@angular/core';
import { LoadingService } from '../services/loading.service';

/**
 * Interceptor для отображения индикатора загрузки
 */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);

  // Увеличиваем счетчик активных запросов
  loadingService.show();

  return next(req).pipe(
    finalize(() => {
      // Уменьшаем счетчик после завершения запроса
      loadingService.hide();
    })
  );
};
