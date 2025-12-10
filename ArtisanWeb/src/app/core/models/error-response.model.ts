/**
 * Модель для обработки ошибок от сервера
 */
export interface ErrorResponse {
  status: number;
  message: string;
  error?: string;
  path?: string;
  timestamp?: string;
  details?: any;
}
