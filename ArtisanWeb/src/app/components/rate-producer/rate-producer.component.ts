// src/app/components/rate-producer/rate-producer.component.ts

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatingService } from '../../core/services/rating.service';
import { StarRatingComponent } from '../star-rating/star-rating.component';

@Component({
  selector: 'app-rate-producer',
  standalone: true,
  imports: [CommonModule, StarRatingComponent],
  templateUrl: './rate-producer.component.html',
  styleUrls: ['./rate-producer.component.css']
})
export class RateProducerComponent implements OnInit {
  @Input() producerId!: number;
  @Input() customerId!: number;
  @Output() ratingSubmitted = new EventEmitter<void>();

  currentRating: number = 0;
  existingRating: number = 0;
  loading: boolean = false;
  error: string | null = null;
  success: string | null = null;
  hasRated: boolean = false;

  constructor(private ratingService: RatingService) {}

  ngOnInit(): void {
    this.checkExistingRating();
  }

  checkExistingRating(): void {
    this.ratingService.getRatingByCustomerAndProducer(this.customerId, this.producerId).subscribe({
      next: (rating) => {
        if (rating) {
          this.existingRating = rating.ratingValue;
          this.currentRating = rating.ratingValue;
          this.hasRated = true;
        }
      },
      error: (err) => {
        // Если рейтинг не найден (404), это нормально
        if (err.status !== 404) {
          console.error('Error checking existing rating:', err);
        }
      }
    });
  }

  onRatingChange(rating: number): void {
    this.currentRating = rating;
    this.error = null;
    this.success = null;
  }

  submitRating(): void {
    if (this.currentRating === 0) {
      this.error = 'Пожалуйста, выберите оценку';
      return;
    }

    this.loading = true;
    this.error = null;
    this.success = null;

    const request = {
      producerId: this.producerId,
      ratingValue: this.currentRating
    };

    this.ratingService.createOrUpdateRating(this.customerId, request).subscribe({
      next: () => {
        this.loading = false;
        this.success = this.hasRated ? 'Оценка обновлена!' : 'Оценка сохранена!';
        this.hasRated = true;
        this.existingRating = this.currentRating;
        this.ratingSubmitted.emit();
        
        // Скрыть сообщение об успехе через 3 секунды
        setTimeout(() => {
          this.success = null;
        }, 3000);
      },
      error: (err) => {
        this.loading = false;
        console.error('Error submitting rating:', err);
        this.error = 'Не удалось сохранить оценку. Попробуйте снова.';
      }
    });
  }

  cancelEdit(): void {
    this.currentRating = this.existingRating;
    this.error = null;
    this.success = null;
  }

  get hasChanges(): boolean {
    return this.currentRating !== this.existingRating;
  }
}
