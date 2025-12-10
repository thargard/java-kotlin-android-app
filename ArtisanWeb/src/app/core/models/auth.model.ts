/**
 * Модель для данных входа
 */
export interface LoginRequest {
  login: string;
  password: string;
}

/**
 * Модель для ответа при входе
 */
export interface LoginResponse {
  token: string;
  refreshToken?: string;
  expiresIn?: number;
  user?: {
    id: number;
    username: string;
    email?: string;
    roles?: string[];
  };
}

/**
 * Модель данных пользователя
 */
export interface User {
  id: number;
  username: string;
  email?: string;
  roles?: string[];
}
