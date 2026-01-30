import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { OrderDto, OrderStatus } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit, OnDestroy {
  login: string | null = null;
  role: string | null = null;
  private sub: Subscription | null = null;

  orders: OrderDto[] = [];
  loadingOrders = false;
  ordersError: string | null = null;

  search = '';
  status: OrderStatus | '' = '';
  sort = 'createdAt,desc';

  readonly statuses: OrderStatus[] = [
    'PENDING',
    'CONFIRMED',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
  ];

  readonly sortOptions: Array<{ labelKey: string; value: string }> = [
    { labelKey: 'orders.sortNewest', value: 'createdAt,desc' },
    { labelKey: 'orders.sortOldest', value: 'createdAt,asc' },
    { labelKey: 'orders.sortIdDesc', value: 'id,desc' },
    { labelKey: 'orders.sortIdAsc', value: 'id,asc' },
    { labelKey: 'orders.sortStatusAsc', value: 'status,asc' },
    { labelKey: 'orders.sortStatusDesc', value: 'status,desc' },
  ];

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.sub = this.authService.currentUser$.subscribe((user) => {
      if (typeof user === 'string') {
        try {
          user = JSON.parse(user);
        } catch (e) {
          user = null;
        }
      }
      if (user) {
        this.login = user.login || null;
        this.role = user.role || null;
      } else {
        this.login = null;
        this.role = null;
      }
    });

    this.loadOrders();
  }

  loadOrders(): void {
    this.loadingOrders = true;
    this.ordersError = null;

    this.orderService
      .getOrders({
        search: this.search?.trim() || undefined,
        status: this.status || undefined,
        sort: this.sort || undefined,
      })
      .subscribe({
        next: (orders) => {
          this.orders = orders ?? [];
          this.loadingOrders = false;
        },
        error: (err) => {
          this.orders = [];
          this.loadingOrders = false;
          this.ordersError =
            err?.message ||
            this.translate.instant('orders.errorLoadFailed');
        },
      });
  }

  clearFilters(): void {
    this.search = '';
    this.status = '';
    this.sort = 'createdAt,desc';
    this.loadOrders();
  }

  trackByOrderId(_: number, order: OrderDto): number {
    return order.id;
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

  statusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case 'PENDING':
        return 'bg-gray-100 text-gray-700 border-gray-200';
      case 'CONFIRMED':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'IN_PROGRESS':
        return 'bg-amber-50 text-amber-800 border-amber-200';
      case 'COMPLETED':
        return 'bg-green-50 text-green-700 border-green-200';
      case 'CANCELLED':
        return 'bg-red-50 text-red-700 border-red-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  }

  userLabel(order: OrderDto): string {
    const u = order.user;
    if (!u) return this.translate.instant('orders.emptyValue');
    return u.login || u.fullName || `#${u.id}`;
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
