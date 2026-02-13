// src/app/components/chat/chat.component.ts

import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MessageService } from '../../core/services/message.service';
import { AuthService } from '../../core/services/auth.service';
import { MessageDto } from '../../core/models/message.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  otherUserId!: number;
  otherUserName: string = '';
  messages: MessageDto[] = [];
  newMessage: string = '';
  loading: boolean = true;
  sending: boolean = false;
  error: string | null = null;
  currentUserId: number | null = null;
  private shouldScrollToBottom: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.getCurrentUser()?.id || null;
    
    this.route.params.subscribe(params => {
      this.otherUserId = +params['userId'];
      console.log('Loaded chat with userId:', this.otherUserId);
      if (this.otherUserId) {
        this.loadConversation();
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  loadConversation(): void {
    this.loading = true;
    this.error = null;

    this.messageService.getConversation(this.otherUserId).subscribe({
      next: (response) => {
        this.messages = response.messages;
        if (this.messages.length > 0) {
          // Получаем имя собеседника из первого сообщения
          const firstMessage = this.messages[0];
          this.otherUserName = firstMessage.senderId === this.otherUserId 
            ? firstMessage.senderName 
            : firstMessage.receiverName;
        } else {
          this.otherUserName = `User ${this.otherUserId}`;
        }
        this.loading = false;
        this.shouldScrollToBottom = true;
        
        // Отмечаем все сообщения как прочитанные
        this.markConversationAsRead();
      },
      error: (err) => {
        console.error('Error loading conversation:', err);
        this.error = 'Не удалось загрузить переписку';
        this.loading = false;
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || this.sending) {
      return;
    }

    this.sending = true;
    const messageContent = this.newMessage.trim();
    this.newMessage = '';

    this.messageService.sendMessage({
      receiverId: this.otherUserId,
      content: messageContent
    }).subscribe({
      next: (message) => {
        this.messages.push(message);
        this.sending = false;
        this.shouldScrollToBottom = true;
      },
      error: (err) => {
        console.error('Error sending message:', err);
        this.error = 'Не удалось отправить сообщение';
        this.newMessage = messageContent; // Восстанавливаем сообщение
        this.sending = false;
      }
    });
  }

  markConversationAsRead(): void {
    this.messageService.markConversationAsRead(this.otherUserId).subscribe({
      next: () => {
        // Обновляем статус прочитанности локально
        this.messages.forEach(msg => {
          if (msg.receiverId === this.currentUserId) {
            msg.isRead = true;
          }
        });
      },
      error: (err) => {
        console.error('Error marking conversation as read:', err);
      }
    });
  }

  scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = 
        this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  goBack(): void {
    this.router.navigate(['/messages']);
  }

  isMyMessage(message: MessageDto): boolean {
    return message.senderId === this.currentUserId;
  }

  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('ru-RU', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  formatDate(timestamp: string): string {
    const date = new Date(timestamp);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return 'Сегодня';
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Вчера';
    } else {
      return date.toLocaleDateString('ru-RU', { 
        day: 'numeric', 
        month: 'long', 
        year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined 
      });
    }
  }

  shouldShowDateSeparator(index: number): boolean {
    if (index === 0) return true;
    
    const currentDate = new Date(this.messages[index].createdAt).toDateString();
    const previousDate = new Date(this.messages[index - 1].createdAt).toDateString();
    
    return currentDate !== previousDate;
  }

  onEntryKey(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    keyboardEvent.preventDefault();
  
    if (!keyboardEvent.shiftKey) {
      this.sendMessage();
    }
  }
}