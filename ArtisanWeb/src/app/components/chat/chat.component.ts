import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { MessageService } from '../../core/services/message.service';
import { MessageDto } from '../../core/models/message.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
})
export class ChatComponent implements OnInit {
  threadId: number | null = null;
  messages: MessageDto[] = [];
  loading = true;
  error: string | null = null;
  newMessage = '';
  sending = false;
  receiverId: number | null = null;
  interlocutorName = '';
  interlocutorId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    const id = this.route.snapshot.paramMap.get('threadId');
    if (id) {
      this.threadId = +id;
      this.loadConversation();
    } else {
      this.error = 'chat.invalidThread';
      this.loading = false;
    }
  }

  loadConversation(): void {
    if (this.threadId == null) return;
    this.loading = true;
    this.error = null;
    this.messageService.getConversation(this.threadId).subscribe({
      next: (res) => {
        this.messages = res.messages ?? [];
        const me = this.authService.getCurrentUser()?.id;
        if (this.messages.length > 0) {
          const last = this.messages[this.messages.length - 1];
          const isOtherSender = last.receiverId === me;
          this.receiverId = isOtherSender ? last.senderId : last.receiverId;
          this.interlocutorName = this.getInterlocutorName(last, me);
          this.interlocutorId = this.receiverId;
          // Mark messages received by current user as read when opening the chat
          this.messages
            .filter((m) => m.receiverId === me && !m.isRead)
            .forEach((m) => this.messageService.markAsRead(m.id).subscribe());
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'chat.loadError';
        this.loading = false;
      },
    });
  }

  isMine(msg: MessageDto): boolean {
    const me = this.authService.getCurrentUser()?.id;
    return me != null && msg.senderId === me;
  }

  private getInterlocutorName(msg: MessageDto, currentUserId: number | undefined): string {
    if (currentUserId == null) return '';
    const name = msg.receiverId === currentUserId ? msg.senderName : msg.receiverName;
    const id = msg.receiverId === currentUserId ? msg.senderId : msg.receiverId;
    if (name != null && String(name).trim() !== '') return String(name).trim();
    if (id != null) return `ID ${id}`;
    return '';
  }

  sendMessage(): void {
    const content = (this.newMessage || '').trim();
    if (!content || this.threadId == null || this.sending) return;
    if (this.receiverId == null && this.messages.length > 0) {
      const me = this.authService.getCurrentUser()?.id;
      const last = this.messages[this.messages.length - 1];
      this.receiverId = last.senderId === me ? last.receiverId : last.senderId;
    }
    this.sending = true;
    this.messageService
      .sendMessage(this.threadId, content, this.receiverId ?? undefined, null)
      .subscribe({
        next: (msg) => {
          this.messages = [...this.messages, msg];
          this.newMessage = '';
          this.sending = false;
        },
        error: () => {
          this.sending = false;
        },
      });
  }
}
