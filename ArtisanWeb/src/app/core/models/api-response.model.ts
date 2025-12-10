/**
 * Базовая модель для API ответа
 */
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  timestamp?: string;
}

/**
 * Модель для пагинированного ответа
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Параметры запроса с пагинацией
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}
