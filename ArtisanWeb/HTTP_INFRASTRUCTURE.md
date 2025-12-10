# HTTP Infrastructure для работы с Java сервером

Создана полная инфраструктура для работы с Java сервером по HTTP.

## Структура проекта

```
src/
├── environments/
│   ├── environment.ts           # Конфигурация для разработки
│   └── environment.prod.ts      # Конфигурация для production
├── app/
│   └── core/
│       ├── models/
│       │   ├── api-response.model.ts      # Типы для API ответов
│       │   └── error-response.model.ts    # Типы для ошибок
│       ├── services/
│       │   ├── base-http.service.ts       # Базовый HTTP сервис
│       │   ├── user.service.ts            # Пример сервиса
│       │   └── loading.service.ts         # Сервис индикатора загрузки
│       └── interceptors/
│           ├── auth.interceptor.ts        # Перехватчик для авторизации
│           ├── error.interceptor.ts       # Обработка ошибок
│           ├── loading.interceptor.ts     # Индикатор загрузки
│           └── logging.interceptor.ts     # Логирование запросов
```

## Основные компоненты

### 1. Environment Configuration

- `environment.ts` - URL API для разработки (по умолчанию http://localhost:8080/api)
- `environment.prod.ts` - URL API для production

### 2. Models

- `ApiResponse<T>` - обертка для всех API ответов
- `PaginatedResponse<T>` - для пагинированных данных
- `ErrorResponse` - для обработки ошибок

### 3. Services

- `BaseHttpService` - базовый класс с методами GET, POST, PUT, PATCH, DELETE
- `UserService` - пример сервиса (можно использовать как шаблон)
- `LoadingService` - управление индикатором загрузки

### 4. Interceptors

- `authInterceptor` - добавляет Bearer токен к запросам
- `errorInterceptor` - обрабатывает HTTP ошибки и перенаправляет на /login при 401
- `loadingInterceptor` - показывает/скрывает индикатор загрузки
- `loggingInterceptor` - логирует все запросы и ответы в консоль

## Использование

### Создание нового сервиса

```typescript
import { Injectable } from "@angular/core";
import { BaseHttpService } from "./base-http.service";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class MyService extends BaseHttpService {
  private endpoint = "/my-endpoint";

  getData(): Observable<any> {
    return this.get(this.endpoint);
  }

  createData(data: any): Observable<any> {
    return this.post(this.endpoint, data);
  }
}
```

### Использование сервиса в компоненте

```typescript
export class MyComponent {
  constructor(private userService: UserService) {}

  loadUsers() {
    this.userService.getUsers({ page: 0, size: 10 }).subscribe({
      next: (response) => console.log(response),
      error: (error) => console.error(error),
    });
  }
}
```

## Настройка

1. Измените `apiUrl` в файлах environment для вашего сервера
2. Настройте токен авторизации в `auth.interceptor.ts`
3. При необходимости отключите interceptors в `app.config.ts`

## Готово к использованию! 🚀
