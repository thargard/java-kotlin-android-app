import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root',
})
export class MessageBadgeService {
  private totalSubject = new BehaviorSubject<number>(0);
  total$ = this.totalSubject.asObservable();

  constructor(private messageService: MessageService) {}

  refresh(): void {
    this.messageService.getUnreadCount().subscribe({
      next: (res) => this.setTotal(res.unreadCount ?? 0),
      error: () => {
        // no-op
      },
    });
  }

  setTotal(count: number): void {
    this.totalSubject.next(Math.max(0, count));
  }

  increment(by: number = 1): void {
    this.totalSubject.next(Math.max(0, this.totalSubject.value + by));
  }

  decrement(by: number = 1): void {
    this.totalSubject.next(Math.max(0, this.totalSubject.value - by));
  }
}
