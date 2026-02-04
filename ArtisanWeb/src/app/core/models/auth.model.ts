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
    fullName?: string;
    login: string; // username
    email?: string;
    password?: string;
    avatarUrl?: string;
    role: string;
  };
}

/**
 * Модель данных пользователя
 */
export interface User {
  id: number;
  fullName?: string;
  login: string; // username
  email?: string;
  password?: string;
  avatarUrl?: string;
  role: string;
}
