// src/app/components/star-rating/star-rating.component.ts

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './star-rating.component.html',
  styleUrls: ['./star-rating.component.css']
})
export class StarRatingComponent {
  @Input() rating: number = 0;
  @Input() totalStars: number = 5;
  @Input() readonly: boolean = false;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Output() ratingChange = new EventEmitter<number>();

  hoveredRating: number = 0;

  get stars(): number[] {
    return Array(this.totalStars).fill(0).map((_, i) => i + 1);
  }

  onStarClick(star: number): void {
    if (!this.readonly) {
      this.rating = star;
      this.ratingChange.emit(this.rating);
    }
  }

  onStarHover(star: number): void {
    if (!this.readonly) {
      this.hoveredRating = star;
    }
  }

  onMouseLeave(): void {
    this.hoveredRating = 0;
  }

  getStarClass(star: number): string {
    const currentRating = this.hoveredRating || this.rating;
    
    if (star <= currentRating) {
      return 'filled';
    } else if (star - 0.5 <= currentRating) {
      return 'half-filled';
    }
    return 'empty';
  }

  getSizeClass(): string {
    return `star-${this.size}`;
  }
}