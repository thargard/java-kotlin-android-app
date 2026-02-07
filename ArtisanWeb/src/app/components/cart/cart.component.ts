import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { CartService, CartItemDto, CartResponse } from '../../core/services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css',
})
export class CartComponent implements OnInit {
  items: CartItemDto[] = [];
  totalPrice = 0;
  loading = true;
  error: string | null = null;
  checkoutLoading = false;
  checkoutSuccess = false;

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.error = null;
    this.cartService.getCart().subscribe({
      next: (res: CartResponse) => {
        this.items = res.items ?? [];
        this.totalPrice = res.totalPrice ?? 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'cart.loadError';
        this.loading = false;
      },
    });
  }

  checkout(): void {
    if (this.items.length === 0) return;
    this.checkoutLoading = true;
    this.error = null;
    this.cartService.checkout().subscribe({
      next: () => {
        this.checkoutLoading = false;
        this.checkoutSuccess = true;
        this.items = [];
        this.totalPrice = 0;
      },
      error: (err) => {
        this.checkoutLoading = false;
        this.error = err?.error?.error || 'cart.checkoutError';
      },
    });
  }
}
