import { HttpInterceptorFn, HttpEventType } from '@angular/common/http';
import { tap } from 'rxjs/operators';

/**
 * Interceptor для логирования HTTP запросов и ответов
 */
export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  const started = Date.now();

  console.log(`🚀 HTTP Request: ${req.method} ${req.url}`);

  if (req.body) {
    console.log('📦 Request Body:', req.body);
  }

  return next(req).pipe(
    tap({
      next: (event) => {
        if (event.type === HttpEventType.Response) {
          const elapsed = Date.now() - started;
          console.log(
            `✅ HTTP Response: ${req.method} ${req.url} - ${event.status} (${elapsed}ms)`
          );
          console.log('📥 Response Body:', event.body);
        }
      },
      error: (error) => {
        const elapsed = Date.now() - started;
        console.error(
          `❌ HTTP Error: ${req.method} ${req.url} - ${error.status} (${elapsed}ms)`
        );
      },
    })
  );
};
