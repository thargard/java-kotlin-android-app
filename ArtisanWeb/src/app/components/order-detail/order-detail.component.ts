import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { OrderService } from '../../core/services/order.service';
import { MessageService } from '../../core/services/message.service';
import { AuthService } from '../../core/services/auth.service';
import { OrderDto, OrderStatus } from '../../core/models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css',
})
export class OrderDetailComponent implements OnInit {
  order: OrderDto | null = null;
  loading = true;
  error: string | null = null;
  chatLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private messageService: MessageService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(+id);
    } else {
      this.error = 'orders.invalidId';
      this.loading = false;
    }
  }

  loadOrder(id: number): void {
    this.loading = true;
    this.error = null;
    this.orderService.getOrderById(id).subscribe({
      next: (order) => {
        this.order = order ?? null;
        if (!this.order) this.error = 'orders.notFound';
        this.loading = false;
      },
      error: () => {
        this.error = 'orders.loadError';
        this.loading = false;
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
    return keyMap[status] || status;
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

  startChat(): void {
    if (!this.order?.user?.id) return;
    const currentUser = this.authService.getCurrentUser();
    if (currentUser?.id === this.order.user.id) {
      return; // не начинаем чат с самим собой
    }
    
    this.chatLoading = true;
    
    // Формируем сообщение
    const content = this.order.description
      ? `Здравствуйте! Вопрос по заказу #${this.order.id}: ${this.order.description.slice(0, 100)}`
      : `Здравствуйте! Вопрос по заказу #${this.order.id}.`;
    
    // Используем новый API - отправляем сообщение напрямую
    this.messageService.sendMessage({
      receiverId: this.order.user.id,
      content: content
    }).subscribe({
      next: (message) => {
        this.chatLoading = false;
        // Переходим в чат с пользователем по его ID
        this.router.navigate(['/chat', this.order!.user!.id]);
      },
      error: (err) => {
        console.error('Error sending message:', err);
        this.chatLoading = false;
      },
    });
  }

  canStartChat(): boolean {
    if (!this.order?.user?.id) return false;
    const current = this.authService.getCurrentUser();
    return current != null && current.id !== this.order.user.id;
  }
}