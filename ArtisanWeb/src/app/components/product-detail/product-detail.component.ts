import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css',
})
export class ProductDetailComponent implements OnInit {
  product: Product | undefined;
  loadingProduct = true;
  productError = '';
  actionLoading = '';
  actionMessage = '';
  actionError = '';

  private messageTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProduct(+id);
    } else {
      this.productError = 'products.invalidId';
      this.loadingProduct = false;
    }
  }

  loadProduct(id: number): void {
    this.loadingProduct = true;
    this.productError = '';
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        if (product) {
          this.product = product;
        } else {
          this.productError = 'products.notFound';
        }
        this.loadingProduct = false;
      },
      error: (err) => {
        this.productError = 'products.loadError';
        this.loadingProduct = false;
      },
    });
  }

  addToCart(): void {
    if (!this.product) return;
    this.clearActionFeedback();
    this.actionLoading = 'cart';
    this.productService.addToCart(this.product.id).subscribe({
      next: () => {
        this.actionLoading = '';
        this.showMessage(this.translate.instant('products.addedToCart'));
      },
      error: (err) => {
        this.actionLoading = '';
        this.showError(err?.error?.error || this.translate.instant('products.addToCartError'));
      },
    });
  }

  buyNow(): void {
    if (!this.product) return;
    this.clearActionFeedback();
    this.actionLoading = 'buy';
    this.productService.buyNow(this.product.id).subscribe({
      next: (res) => {
        this.actionLoading = '';
        if (res?.orderId != null) {
          this.router.navigate(['/orders', res.orderId]);
        } else {
          this.showMessage(this.translate.instant('products.buySuccess'));
        }
      },
      error: (err) => {
        this.actionLoading = '';
        this.showError(err?.error?.error || this.translate.instant('products.buyError'));
      },
    });
  }

  contactSeller(): void {
    if (!this.product) return;
    this.clearActionFeedback();
    this.actionLoading = 'contact';
    this.productService.contactSeller(this.product.id).subscribe({
      next: (res) => {
        this.actionLoading = '';
        if (res?.threadId != null) {
          this.router.navigate(['/chat', res.threadId]);
        } else {
          this.showMessage(this.translate.instant('products.contactSuccess'));
        }
      },
      error: (err) => {
        this.actionLoading = '';
        this.showError(err?.error?.error || this.translate.instant('products.contactError'));
      },
    });
  }

  private showMessage(msg: string): void {
    if (this.messageTimeout) clearTimeout(this.messageTimeout);
    this.actionError = '';
    this.actionMessage = msg;
    this.messageTimeout = setTimeout(() => {
      this.actionMessage = '';
      this.messageTimeout = null;
    }, 4000);
  }

  private showError(msg: string): void {
    if (this.messageTimeout) clearTimeout(this.messageTimeout);
    this.messageTimeout = null;
    this.actionMessage = '';
    this.actionError = msg;
  }

  private clearActionFeedback(): void {
    if (this.messageTimeout) clearTimeout(this.messageTimeout);
    this.messageTimeout = null;
    this.actionMessage = '';
    this.actionError = '';
  }
}
