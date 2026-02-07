import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ProductService } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';

const CATEGORIES = [
  { value: 'Деревянные изделия', labelKey: 'products.categoryWood' },
  { value: 'Керамика', labelKey: 'products.categoryCeramics' },
  { value: 'Текстиль', labelKey: 'products.categoryTextile' },
  { value: 'Стекло', labelKey: 'products.categoryGlass' },
  { value: 'Кожаные изделия', labelKey: 'products.categoryLeather' },
  { value: 'Свечи', labelKey: 'products.categoryCandles' },
];

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, FormsModule],
  templateUrl: './product-create.component.html',
  styleUrl: './product-create.component.css',
})
export class ProductCreateComponent implements OnInit {
  name = '';
  description = '';
  price: number | null = null;
  category = '';
  imageUrl = '';
  submitting = false;
  error: string | null = null;

  readonly categories = CATEGORIES;

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
  }

  submit(): void {
    if (!this.name.trim()) {
      this.error = 'products.createNameRequired';
      return;
    }
    if (this.price == null || this.price < 0) {
      this.error = 'products.createPriceRequired';
      return;
    }
    if (!this.category.trim()) {
      this.error = 'products.createCategoryRequired';
      return;
    }
    this.error = null;
    this.submitting = true;
    this.productService
      .createProduct({
        name: this.name.trim(),
        description: this.description.trim() || undefined,
        price: Number(this.price),
        category: this.category.trim(),
        imageUrl: this.imageUrl.trim() || undefined,
      })
      .subscribe({
        next: (product) => {
          this.submitting = false;
          this.router.navigate(['/products', product.id]);
        },
        error: (err) => {
          this.submitting = false;
          this.error =
            err?.error?.error || 'products.createError';
        },
      });
  }
}
