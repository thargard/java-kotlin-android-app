import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { OrderDto, OrderStatus } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-announcements',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './announcements.component.html',
  styleUrl: './announcements.component.css',
})
export class AnnouncementsComponent implements OnInit {
  orders: OrderDto[] = [];
  loading = false;
  error: string | null = null;

  status: OrderStatus | '' = 'PENDING';
  search = '';

  readonly statuses: OrderStatus[] = [
    'PENDING',
    'CONFIRMED',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
  ];

  constructor(
    private orderService: OrderService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.orderService
      .getOrders({
        search: this.search?.trim() || undefined,
        status: this.status || undefined,
      })
      .subscribe({
        next: (orders) => {
          this.orders = orders ?? [];
          this.loading = false;
        },
        error: (err) => {
          this.orders = [];
          this.loading = false;
          this.error =
            err?.message ||
            this.translate.instant('announcements.errorLoadFailed');
        },
      });
  }

  clear(): void {
    this.search = '';
    this.status = 'PENDING';
    this.load();
  }

  statusLabel(status: OrderStatus): string {
    const keyMap: Record<OrderStatus, string> = {
      PENDING: 'orders.statusPending',
      CONFIRMED: 'orders.statusConfirmed',
      IN_PROGRESS: 'orders.statusInProgress',
      COMPLETED: 'orders.statusCompleted',
      CANCELLED: 'orders.statusCancelled',
    };
    return this.translate.instant(keyMap[status] || status);
  }
}

