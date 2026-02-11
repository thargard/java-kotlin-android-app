// src/app/components/producer-rating/producer-rating.component.ts

import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatingService } from '../../core/services/rating.service';
import { RatingStats } from '../../core/models/rating.model';
import { StarRatingComponent } from '../star-rating/star-rating.component';

@Component({
  selector: 'app-producer-rating',
  standalone: true,
  imports: [CommonModule, StarRatingComponent],
  templateUrl: './producer-rating.component.html',
  styleUrls: ['./producer-rating.component.css']
})
export class ProducerRatingComponent implements OnInit {
  @Input() producerId!: number;
  @Input() showDetails: boolean = true;
  
  ratingStats: RatingStats | null = null;
  loading: boolean = true;
  error: string | null = null;

  constructor(private ratingService: RatingService) {}

  ngOnInit(): void {
    this.loadRatingStats();
  }

  loadRatingStats(): void {
    this.loading = true;
    this.error = null;

    this.ratingService.getProducerRatingStats(this.producerId).subscribe({
      next: (stats) => {
        this.ratingStats = stats;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading rating stats:', err);
        this.error = 'Не удалось загрузить рейтинг';
        this.loading = false;
      }
    });
  }

  getStarPercentage(stars: number): number {
    if (!this.ratingStats || this.ratingStats.totalRatings === 0) {
      return 0;
    }
    
    const count = this.getStarCount(stars);
    return (count / this.ratingStats.totalRatings) * 100;
  }

  getStarCount(stars: number): number {
    if (!this.ratingStats) return 0;
    
    switch (stars) {
      case 5: return this.ratingStats.fiveStars;
      case 4: return this.ratingStats.fourStars;
      case 3: return this.ratingStats.threeStars;
      case 2: return this.ratingStats.twoStars;
      case 1: return this.ratingStats.oneStar;
      default: return 0;
    }
  }

  getRatingWord(): string {
    if (!this.ratingStats) return 'оценок';
    
    const count = this.ratingStats.totalRatings;
    const lastDigit = count % 10;
    const lastTwoDigits = count % 100;

    if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
      return 'оценок';
    }

    if (lastDigit === 1) {
      return 'оценка';
    }

    if (lastDigit >= 2 && lastDigit <= 4) {
      return 'оценки';
    }

    return 'оценок';
  }
}
