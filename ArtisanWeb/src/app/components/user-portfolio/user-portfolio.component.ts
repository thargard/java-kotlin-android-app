import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { OrderService } from '../../core/services/order.service';
import { User } from '../../core/models/auth.model';
import { OrderDto, OrderStatus } from '../../core/models/order.model';

@Component({
  selector: 'app-user-portfolio',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './user-portfolio.component.html',
  styleUrl: './user-portfolio.component.css',
})
export class UserPortfolioComponent implements OnInit {
  user: User | null = null;
  loadingUser = false;
  userError: string | null = null;

  orders: OrderDto[] = [];
  loadingOrders = false;
  ordersError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private orderService: OrderService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const userId = idParam ? Number(idParam) : NaN;
    if (!userId || Number.isNaN(userId)) {
      this.userError = this.translate.instant('users.invalidId');
      return;
    }
    this.loadUser(userId);
    this.loadOrders(userId);
  }

  private loadUser(userId: number): void {
    this.loadingUser = true;
    this.userError = null;
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        this.user = users.find((u) => u.id === userId) ?? null;
        if (!this.user) {
          this.userError = this.translate.instant('users.notFound');
        }
        this.loadingUser = false;
      },
      error: (err) => {
        this.loadingUser = false;
        this.userError =
          err?.message || this.translate.instant('users.loadError');
      },
    });
  }

  private loadOrders(userId: number): void {
    this.loadingOrders = true;
    this.ordersError = null;
    this.orderService.getOrders({ userId }).subscribe({
      next: (orders) => {
        this.orders = orders ?? [];
        this.loadingOrders = false;
      },
      error: (err) => {
        this.orders = [];
        this.loadingOrders = false;
        this.ordersError =
          err?.message || this.translate.instant('orders.errorLoadFailed');
      },
    });
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
}

