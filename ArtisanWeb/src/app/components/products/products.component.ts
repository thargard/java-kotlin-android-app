import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProductService, PaginatedProducts } from '../../core/services/product.service';
import { Product } from '../../core/models/product.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css',
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  loadingProducts = true;
  productsError = '';
  search = '';
  selectedCategory = 'all';
  
  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;
  
  // Math helper for template
  Math = Math;

  categories = [
    { value: 'all', label: 'products.categoryAll' },
    { value: 'Деревянные изделия', label: 'products.categoryWood' },
    { value: 'Керамика', label: 'products.categoryCeramics' },
    { value: 'Текстиль', label: 'products.categoryTextile' },
    { value: 'Стекло', label: 'products.categoryGlass' },
    { value: 'Кожаные изделия', label: 'products.categoryLeather' },
    { value: 'Свечи', label: 'products.categoryCandles' },
  ]; // делать запрос к API и получать категории

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loadingProducts = true;
    this.productsError = '';
    
    const filters: any = {
      page: this.currentPage,
      size: this.pageSize,
    };
    
    if (this.selectedCategory !== 'all') {
      filters.category = this.selectedCategory;
    }
    
    if (this.search.trim()) {
      filters.search = this.search.trim();
    }

    this.productService.getProducts(filters).subscribe({
      next: (response: PaginatedProducts) => {
        this.products = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loadingProducts = false;
      },
      error: (err) => {
        this.productsError = 'products.errorLoadFailed';
        this.loadingProducts = false;
        console.error('Error loading products:', err);
      },
    });
  }

  applyFilter(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearSearch(): void {
    this.search = '';
    this.selectedCategory = 'all';
    this.applyFilter();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadProducts();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

  trackByProductId(index: number, product: Product): number {
    return product.id;
  }
}
