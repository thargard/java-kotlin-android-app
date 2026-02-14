// src/app/components/messages/messages.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MessageService } from '../../core/services/message.service';
import { ConversationDto } from '../../core/models/message.model';
import { MessageSocketService } from '../../core/services/message-socket.service';
import { AuthService } from '../../core/services/auth.service';
import { MessageBadgeService } from '../../core/services/message-badge.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css'],
})
export class MessagesComponent implements OnInit, OnDestroy {
  conversations: ConversationDto[] = [];
  loading: boolean = true;
  error: string | null = null;
  private socketSub?: Subscription;

  constructor(
    private messageService: MessageService,
    private router: Router,
    private messageSocket: MessageSocketService,
    private authService: AuthService,
    private messageBadge: MessageBadgeService,
  ) {}

  ngOnInit(): void {
    this.loadConversations();
    this.messageSocket.connect(this.authService.getCurrentUserId());
    this.socketSub = this.messageSocket.message$.subscribe((msg) => {
      const me = this.authService.getCurrentUserId();
      if (!me) return;
      const otherId = msg.senderId === me ? msg.receiverId : msg.senderId;
      if (!otherId) return;
      const otherName = msg.senderId === me ? msg.receiverName : msg.senderName;

      const existing = this.conversations.find(
        (c) => c.otherUserId === otherId,
      );
        if (existing) {
          existing.lastMessage = msg.content || '';
          existing.lastMessageAt = msg.createdAt || existing.lastMessageAt;
          existing.isLastMessageFromMe = msg.senderId === me;
          if (msg.receiverId === me) {
            existing.unreadCount = (existing.unreadCount || 0) + 1;
          }
        } else {
        this.conversations.unshift({
          otherUserId: otherId,
          otherUserName: otherName || `User ${otherId}`,
          lastMessage: msg.content || '',
          lastMessageAt: msg.createdAt || new Date().toISOString(),
          unreadCount: msg.receiverId === me ? 1 : 0,
          isLastMessageFromMe: msg.senderId === me,
        });
        // Global badge count is handled in AppComponent.
      }
      this.sortConversations();
    });
  }

  ngOnDestroy(): void {
    this.socketSub?.unsubscribe();
  }

  loadConversations(): void {
    this.loading = true;
    this.error = null;

    this.messageService.getConversations().subscribe({
      next: (response) => {
        this.conversations = response.conversations;
        this.messageBadge.setTotal(this.getTotalUnreadCount());
        this.sortConversations();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading conversations:', err);
        this.error = 'Не удалось загрузить диалоги';
        this.loading = false;
      },
    });
  }

  openConversation(otherUserId: number): void {
    const convo = this.conversations.find((c) => c.otherUserId === otherUserId);
    if (convo && convo.unreadCount > 0) {
      this.messageBadge.decrement(convo.unreadCount);
      convo.unreadCount = 0;
    }
    this.router.navigate(['/chat', otherUserId]);
  }

  getTimeAgo(timestamp: string): string {
    const now = new Date();
    const messageTime = new Date(timestamp);
    const diffInMs = now.getTime() - messageTime.getTime();
    const diffInMinutes = Math.floor(diffInMs / 60000);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInMinutes < 1) {
      return 'только что';
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes} мин назад`;
    } else if (diffInHours < 24) {
      return `${diffInHours} ч назад`;
    } else if (diffInDays === 1) {
      return 'вчера';
    } else if (diffInDays < 7) {
      return `${diffInDays} дн назад`;
    } else {
      return messageTime.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'short',
      });
    }
  }

  truncateMessage(message: string, maxLength: number = 50): string {
    if (message.length <= maxLength) {
      return message;
    }
    return message.substring(0, maxLength) + '...';
  }

  getTotalUnreadCount(): number {
    return this.conversations.reduce((sum, conv) => sum + conv.unreadCount, 0);
  }

  private sortConversations(): void {
    this.conversations.sort((a, b) => {
      const at = new Date(a.lastMessageAt || 0).getTime();
      const bt = new Date(b.lastMessageAt || 0).getTime();
      return bt - at;
    });
  }
}
