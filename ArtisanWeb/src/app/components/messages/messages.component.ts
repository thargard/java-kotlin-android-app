import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { MessageService } from '../../core/services/message.service';
import { MessageDto } from '../../core/models/message.model';

export interface ConversationSummary {
  threadId: number;
  otherPartyName: string;
  otherPartyId: number | null;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
}

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.css',
})
export class MessagesComponent implements OnInit {
  conversations: ConversationSummary[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadThreads();
  }

  openChat(threadId: number): void {
    this.router.navigate(['/chat', threadId]);
  }

  loadThreads(): void {
    this.loading = true;
    this.error = null;
    this.messageService.getThreads().subscribe({
      next: (res) => {
        this.conversations = this.buildConversations(res.threads || {});
        this.loading = false;
      },
      error: () => {
        this.error = 'messages.loadError';
        this.loading = false;
      },
    });
  }

  private buildConversations(threads: Record<string, MessageDto[]>): ConversationSummary[] {
    const currentUserId = this.authService.getCurrentUser()?.id;
    if (currentUserId == null) return [];
    const unknownLabel = this.translate.instant('messages.unknownUser');

    const result: ConversationSummary[] = [];
    for (const [threadIdStr, messages] of Object.entries(threads)) {
      const threadId = +threadIdStr;
      if (Number.isNaN(threadId) || !messages?.length) continue;

      const sorted = [...messages].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      const last = sorted[0] as MessageDto & Record<string, unknown>;
      const isOtherSender = (last.receiverId ?? (last as any).receiver_id) === currentUserId;
      const otherPartyId = isOtherSender
        ? (last.senderId ?? (last as any).sender_id ?? (last as any).sender?.id)
        : (last.receiverId ?? (last as any).receiver_id ?? (last as any).receiver?.id);
      const otherPartyName = this.getOtherPartyName(last, currentUserId, unknownLabel);
      const unreadCount = messages.filter(
        (m) => m.receiverId === currentUserId && !m.isRead
      ).length;

      result.push({
        threadId,
        otherPartyName,
        otherPartyId: otherPartyId ?? null,
        lastMessage: last.content || '',
        lastMessageAt: last.createdAt,
        unreadCount,
      });
    }
    result.sort(
      (a, b) => new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime()
    );
    return result;
  }

  private getOtherPartyName(msg: MessageDto & Record<string, unknown>, currentUserId: number, unknownLabel: string): string {
    const isOtherSender = (msg.receiverId ?? (msg as any).receiver_id) === currentUserId;
    const name = isOtherSender
      ? (msg.senderName ?? (msg as any).sender_name ?? (msg as any).sender?.fullName ?? (msg as any).sender?.login)
      : (msg.receiverName ?? (msg as any).receiver_name ?? (msg as any).receiver?.fullName ?? (msg as any).receiver?.login);
    const id = isOtherSender ? (msg.senderId ?? (msg as any).sender_id ?? (msg as any).sender?.id) : (msg.receiverId ?? (msg as any).receiver_id ?? (msg as any).receiver?.id);
    if (name != null && String(name).trim() !== '') return String(name).trim();
    if (id != null) return `ID ${id}`;
    return unknownLabel;
  }
}
