import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { MessageDto } from '../models/message.model';

@Injectable({
  providedIn: 'root',
})
export class MessageSocketService implements OnDestroy {
  private client: Client | null = null;
  private messageSubject = new Subject<MessageDto>();
  message$ = this.messageSubject.asObservable();

  constructor(private authService: AuthService) {}

  connect(userId?: number | null): void {
    if (this.client?.active) return;
    const token = this.authService.getToken();
    if (!token) return;

    const baseUrl = environment.apiUrl.replace(/\/api\/?$/, '');
    const socketUrl = `${baseUrl}/ws`;
    const meId = userId ?? this.authService.getCurrentUser()?.id;

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.client.onConnect = () => {
      this.client?.subscribe('/user/queue/messages', (msg: IMessage) => {
        try {
          const dto = JSON.parse(msg.body) as MessageDto;
          this.messageSubject.next(dto);
        } catch {
          // no-op
        }
      });
      if (meId != null) {
        this.client?.subscribe(`/topic/messages/${meId}`, (msg: IMessage) => {
          try {
            const dto = JSON.parse(msg.body) as MessageDto;
            this.messageSubject.next(dto);
          } catch {
            // no-op
          }
        });
      }
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.messageSubject.complete();
  }
}
