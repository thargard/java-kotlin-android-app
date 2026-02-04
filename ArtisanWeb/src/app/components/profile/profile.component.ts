import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { OrderService } from '../../core/services/order.service';
import { User } from '../../core/models/auth.model';
import { OrderDto, OrderStatus } from '../../core/models/order.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit, OnDestroy {
  user: User | null = null;
  loadingProfile = false;
  profileError: string | null = null;

  editMode = false;
  editFullName = '';
  editLogin = '';
  editEmail = '';
  savingProfile = false;
  saveProfileError: string | null = null;
  avatarFile: File | null = null;
  avatarPreviewUrl: string | null = null;
  avatarUploading = false;
  avatarUploadError: string | null = null;

  orders: OrderDto[] = [];
  loadingOrders = false;
  ordersError: string | null = null;

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private translate: TranslateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    this.user = this.authService.getCurrentUser();
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.revokeAvatarPreview();
  }

  loadProfile(): void {
    this.loadingProfile = true;
    this.profileError = null;
    this.authService.fetchCurrentUserProfile().subscribe({
      next: (user) => {
        this.user = user;
        this.editFullName = user.fullName ?? '';
        this.editLogin = user.login ?? '';
        this.editEmail = user.email ?? '';
        this.avatarUploadError = null;
        this.loadingProfile = false;
        this.loadOrders();
      },
      error: (err) => {
        this.loadingProfile = false;
        this.profileError =
          err?.message || this.translate.instant('profile.errorLoad');
      },
    });
  }

  loadOrders(): void {
    if (!this.user?.id) return;
    this.loadingOrders = true;
    this.ordersError = null;
    this.orderService.getOrders({ userId: this.user.id }).subscribe({
      next: (list) => {
        this.orders = list ?? [];
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

  startEdit(): void {
    this.editFullName = this.user?.fullName ?? '';
    this.editLogin = this.user?.login ?? '';
    this.editEmail = this.user?.email ?? '';
    this.editMode = true;
    this.saveProfileError = null;
    this.avatarUploadError = null;
  }

  cancelEdit(): void {
    this.editMode = false;
    this.saveProfileError = null;
    this.avatarUploadError = null;
    this.avatarFile = null;
    this.revokeAvatarPreview();
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0] ? input.files[0] : null;
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.avatarUploadError = this.translate.instant('profile.avatarTypeError');
      this.avatarFile = null;
      this.revokeAvatarPreview();
      return;
    }

    this.avatarUploadError = null;
    this.avatarFile = file;
    this.setAvatarPreview(file);
  }

  uploadAvatar(): void {
    if (!this.avatarFile || !this.user) return;
    this.avatarUploading = true;
    this.avatarUploadError = null;
    this.authService.uploadAvatar(this.avatarFile).subscribe({
      next: (response) => {
        this.avatarUploading = false;
        if (response?.avatarUrl) {
          this.user = { ...this.user!, avatarUrl: response.avatarUrl };
          this.avatarPreviewUrl = response.avatarUrl;
        }
        this.avatarFile = null;
      },
      error: (err) => {
        this.avatarUploading = false;
        this.avatarUploadError =
          err?.message || this.translate.instant('profile.avatarUploadError');
      },
    });
  }

  saveProfile(): void {
    this.savingProfile = true;
    this.saveProfileError = null;
    this.authService
      .updateProfile({
        fullName: this.editFullName || undefined,
        login: this.editLogin || undefined,
        email: this.editEmail || undefined,
      })
      .subscribe({
        next: (user) => {
          this.user = user;
          this.editMode = false;
          this.savingProfile = false;
        },
        error: (err) => {
          this.saveProfileError =
            err?.message || this.translate.instant('profile.errorSave');
          this.savingProfile = false;
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

  roleLabel(role: string | undefined): string {
    if (!role) return '—';
    const key = 'profile.role' + role;
    const t = this.translate.instant(key);
    return t !== key ? t : role;
  }
  avatarDisplayUrl(): string | null {
    return this.avatarPreviewUrl || this.user?.avatarUrl || null;
  }

  private setAvatarPreview(file: File): void {
    this.revokeAvatarPreview();
    this.avatarPreviewUrl = URL.createObjectURL(file);
  }

  private revokeAvatarPreview(): void {
    if (this.avatarPreviewUrl && this.avatarPreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.avatarPreviewUrl);
    }
    this.avatarPreviewUrl = null;
  }
}
