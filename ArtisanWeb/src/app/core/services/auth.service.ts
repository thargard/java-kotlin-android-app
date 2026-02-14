import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, User } from '../models/auth.model';

/**
 * Сервис для аутентификации пользователей
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly TOKEN_KEY = 'authToken';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private readonly USER_KEY = 'currentUser';

  private currentUserSubject = new BehaviorSubject<User | null>(
    this.getCurrentUser()
  );
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Вход пользователя в систему
   * @param username имя пользователя
   * @param password пароль
   */
  login(username: string, password: string): Observable<LoginResponse> {
    const loginRequest: LoginRequest = { login: username, password };

    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, loginRequest)
      .pipe(
        tap((response) => {
          // Сохраняем JWT токен
          if (response.token) {
            this.setToken(response.token);
          }

          // Сохраняем refresh token если есть
          if (response.refreshToken) {
            this.setRefreshToken(response.refreshToken);
          }

          // Сохраняем данные пользователя
          if (response.user) {
            this.setCurrentUser(response.user);
            this.currentUserSubject.next(response.user);
          }
        })
      );
  }

  /**
   * Вход пользователя в систему через Google OAuth
   */
  googleLogin(idToken: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${environment.apiUrl}/auth/google`,
        { token: idToken },
        { headers: { 'Content-Type': 'application/json' } }
      )
      .pipe(
        tap((response) => {
          console.log(response);
          // Сохраняем JWT токен
          if (response.token) {
            this.setToken(response.token);
          }

          // Сохраняем refresh token если есть
          if (response.refreshToken) {
            this.setRefreshToken(response.refreshToken);
          }

          // Сохраняем данные пользователя
          if (response.user) {
            this.setCurrentUser(response.user);
            this.currentUserSubject.next(response.user);
          }
        })
      );
  }

  /**
   * Регистрация нового пользователя
   * @param registrationData данные регистрации
   */
  register(registrationData: {
    login: string;
    email: string;
    password: string;
    fullName: string;
    role: string;
  }): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${environment.apiUrl}/auth/register`,
        registrationData
      )
      .pipe(
        tap((response) => {
          console.log('Registration response', response);
          // Сохраняем JWT токен
          if (response.token) {
            this.setToken(response.token);
          }

          // Сохраняем refresh token если есть
          if (response.refreshToken) {
            this.setRefreshToken(response.refreshToken);
          }

          // Сохраняем данные пользователя
          if (response.user) {
            this.setCurrentUser(response.user);
            this.currentUserSubject.next(response.user);
          }
        })
      );
  }

  /**
   * Получить список всех пользователей (GET /api/auth).
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/auth`);
  }

  /**
   * Выход пользователя из системы
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Получить JWT токен
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Сохранить JWT токен
   */
  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Получить refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Сохранить refresh token
   */
  private setRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  /**
   * Проверить, авторизован ли пользователь
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    // Можно добавить проверку срока действия токена
    // return !this.isTokenExpired(token);
    return true;
  }

  /**
   * Загрузить профиль текущего пользователя с сервера (GET /api/auth/me).
   * После успешной загрузки обновляет currentUser в localStorage и в currentUser$.
   */
  fetchCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => {
        this.setCurrentUser(user);
        this.currentUserSubject.next(user);
      })
    );
  }

  /**
   * Обновить профиль текущего пользователя (PATCH /api/auth/me).
   * После успешного обновления обновляет currentUser в localStorage и в currentUser$.
   */
  updateProfile(profile: {
    fullName?: string;
    login?: string;
    email?: string;
  }): Observable<User> {
    return this.http.patch<User>(`${environment.apiUrl}/auth/me`, profile).pipe(
      tap((user) => {
        this.setCurrentUser(user);
        this.currentUserSubject.next(user);
      })
    );
  }

  /**
   * Загрузить аватар текущего пользователя (POST /api/users/me/avatar).
   * Ожидает ответ { avatarUrl } и обновляет currentUser.
   */
  uploadAvatar(file: File): Observable<{ avatarUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http
      .post<{ avatarUrl: string }>(
        `${environment.apiUrl}/users/me/avatar`,
        formData
      )
      .pipe(
        tap((response) => {
          if (!response?.avatarUrl) return;
          const current = this.getCurrentUser();
          if (!current) return;
          const next = { ...current, avatarUrl: response.avatarUrl };
          this.setCurrentUser(next);
          this.currentUserSubject.next(next);
        })
      );
  }

  /**
   * Получить текущего пользователя
   */
  getCurrentUser(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Return current user id, falling back to token payload if needed.
   */
  getCurrentUserId(): number | null {
    const user = this.getCurrentUser();
    if (user?.id != null) return user.id;

    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = atob(normalized);
      const data = JSON.parse(json);
      const id = data?.id;
      return typeof id === 'number' ? id : Number.isFinite(+id) ? +id : null;
    } catch {
      return null;
    }
  }

  /**
   * Сохранить текущего пользователя
   */
  private setCurrentUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Проверить, истек ли срок действия токена
   * @param token JWT токен
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp;
      return Math.floor(new Date().getTime() / 1000) >= expiry;
    } catch (e) {
      return true;
    }
  }
}
