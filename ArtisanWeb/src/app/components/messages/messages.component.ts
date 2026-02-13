// src/app/components/messages/messages.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MessageService } from '../../core/services/message.service';
import { ConversationDto } from '../../core/models/message.model';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {
  conversations: ConversationDto[] = [];
  loading: boolean = true;
  error: string | null = null;

  constructor(
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.loading = true;
    this.error = null;

    this.messageService.getConversations().subscribe({
      next: (response) => {
        this.conversations = response.conversations;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading conversations:', err);
        this.error = 'Не удалось загрузить диалоги';
        this.loading = false;
      }
    });
  }

  openConversation(otherUserId: number): void {
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
        month: 'short' 
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
}