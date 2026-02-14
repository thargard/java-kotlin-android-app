import { Injectable, OnDestroy, NgZone } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
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
  private queueSub?: StompSubscription;
  private topicSub?: StompSubscription;
  private subscribedUserId?: number | null = null;
  private messageSubject = new Subject<MessageDto>();
  message$ = this.messageSubject.asObservable();

  constructor(
    private authService: AuthService,
    private ngZone: NgZone,
  ) {}

  connect(userId?: number | null): void {
    const token = this.authService.getToken();
    if (!token) return;

    const meId =
      userId ??
      this.authService.getCurrentUser()?.id ??
      this.getUserIdFromToken(token);

    if (this.client?.active) {
      this.ensureSubscriptions(meId);
      return;
    }

    const baseUrl = environment.apiUrl.replace(/\/api\/?$/, '');
    const wsBase = baseUrl.replace(/^http/, 'ws');
    const socketUrl = `${wsBase}/ws-plain`;

    this.client = new Client({
      // Use native WebSocket endpoint for more reliable real-time delivery.
      brokerURL: socketUrl,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.client.debug = (str) => {
      // Use a single tag to filter WS logs.
      console.log('[WS]', str);
    };

    this.client.onWebSocketError = (evt) => {
      console.error('[WS] socket error', evt);
    };

    this.client.onWebSocketClose = (evt) => {
      console.warn('[WS] socket closed', evt);
    };

    this.client.onStompError = (frame) => {
      console.error('[WS] STOMP error', frame.headers, frame.body);
    };

    this.client.onConnect = () => {
      this.ensureSubscriptions(meId);
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client) {
      this.queueSub?.unsubscribe();
      this.topicSub?.unsubscribe();
      this.queueSub = undefined;
      this.topicSub = undefined;
      this.subscribedUserId = undefined;
      this.client.deactivate();
      this.client = null;
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.messageSubject.complete();
  }

  private ensureSubscriptions(meId?: number | null): void {
    if (!this.client || !this.client.connected) return;

    if (!this.queueSub) {
      this.queueSub = this.client.subscribe(
        '/user/queue/messages',
        (msg: IMessage) => {
          try {
            const dto = JSON.parse(msg.body) as MessageDto;
            // Ensure UI updates by running inside Angular zone.
            this.ngZone.run(() => this.messageSubject.next(dto));
          } catch {
            // no-op
          }
        }
      );
    }

    if (meId == null || this.subscribedUserId === meId) return;

    this.topicSub?.unsubscribe();
    this.topicSub = this.client.subscribe(
      `/topic/messages/${meId}`,
      (msg: IMessage) => {
        try {
          const dto = JSON.parse(msg.body) as MessageDto;
          this.ngZone.run(() => this.messageSubject.next(dto));
        } catch {
          // no-op
        }
      }
    );
    this.subscribedUserId = meId;
  }

  private getUserIdFromToken(token: string): number | null {
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
}
