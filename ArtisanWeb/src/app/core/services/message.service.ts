import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from './base-http.service';
import { MessageDto, StartConversationResponse } from '../models/message.model';

@Injectable({
  providedIn: 'root',
})
export class MessageService extends BaseHttpService {
  private readonly endpoint = '/messages';

  /**
   * Список диалогов пользователя (по тредам).
   * GET /api/messages/threads
   */
  getThreads(): Observable<{ threads: Record<string, MessageDto[]> }> {
    return this.get<{ threads: Record<string, MessageDto[]> }>(`${this.endpoint}/threads`);
  }

  /**
   * Отметить сообщение прочитанным.
   * PATCH /api/messages/:messageId/read
   */
  markAsRead(messageId: number): Observable<{ message: string }> {
    return this.patch<{ message: string }>(`${this.endpoint}/${messageId}/read`, {});
  }

  /**
   * Начать диалог с пользователем.
   * POST /api/messages/start
   */
  startConversation(
    receiverId: number,
    content: string,
    productId?: number | null
  ): Observable<StartConversationResponse & { threadId?: number }> {
    const body: Record<string, unknown> = { receiverId, content };
    if (productId != null) body['productId'] = productId;
    return this.post<StartConversationResponse & { threadId?: number }>(
      `${this.endpoint}/start`,
      body
    );
  }

  /**
   * Получить переписку по threadId.
   * GET /api/messages/thread/:threadId
   */
  getConversation(threadId: number): Observable<{ threadId: number; messages: MessageDto[] }> {
    return this.get<{ threadId: number; messages: MessageDto[] }>(
      `${this.endpoint}/thread/${threadId}`
    );
  }

  /**
   * Отправить сообщение в существующий тред.
   * POST /api/messages/thread/:threadId
   */
  sendMessage(
    threadId: number,
    content: string,
    receiverId?: number,
    productId?: number | null
  ): Observable<MessageDto> {
    const body: Record<string, unknown> = { content };
    if (receiverId != null) body['receiverId'] = receiverId;
    if (productId != null) body['productId'] = productId;
    return this.post<MessageDto>(`${this.endpoint}/thread/${threadId}`, body);
  }
}
