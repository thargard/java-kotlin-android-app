// src/app/core/services/message.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  MessageDto, 
  ConversationDto, 
  SendMessageRequest, 
  ConversationResponse,
  ConversationsResponse,
  UnreadCountResponse
} from '../models/message.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private apiUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  /**
   * Получить список всех диалогов пользователя
   * GET /api/messages/conversations
   */
  getConversations(): Observable<ConversationsResponse> {
    return this.http.get<ConversationsResponse>(`${this.apiUrl}/conversations`);
  }

  /**
   * Получить переписку с конкретным пользователем
   * GET /api/messages/conversation/{otherUserId}
   */
  getConversation(otherUserId: number): Observable<ConversationResponse> {
    return this.http.get<ConversationResponse>(`${this.apiUrl}/conversation/${otherUserId}`);
  }

  /**
   * Отправить сообщение пользователю
   * POST /api/messages/send
   */
  sendMessage(request: SendMessageRequest): Observable<MessageDto> {
    return this.http.post<MessageDto>(`${this.apiUrl}/send`, request);
  }

  /**
   * Отметить сообщение как прочитанное
   * PATCH /api/messages/{messageId}/read
   */
  markAsRead(messageId: number): Observable<{ message: string }> {
    return this.http.patch<{ message: string }>(`${this.apiUrl}/${messageId}/read`, {});
  }

  /**
   * Отметить весь диалог как прочитанный
   * POST /api/messages/conversation/{otherUserId}/read
   */
  markConversationAsRead(otherUserId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/conversation/${otherUserId}/read`, {});
  }

  /**
   * Получить количество непрочитанных сообщений
   * GET /api/messages/unread/count
   */
  getUnreadCount(): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(`${this.apiUrl}/unread/count`);
  }

  /**
   * Получить количество непрочитанных сообщений от конкретного пользователя
   * GET /api/messages/unread/count/{otherUserId}
   */
  getUnreadCountFromUser(otherUserId: number): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(`${this.apiUrl}/unread/count/${otherUserId}`);
  }
}