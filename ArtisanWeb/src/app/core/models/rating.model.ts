export interface Rating {
  id: number;
  customerId: number;
  customerName: string;
  producerId: number;
  producerName: string;
  ratingValue: number;
  createdAt: string;
  updatedAt?: string;
}

export interface RatingRequest {
  producerId: number;
  ratingValue: number;
}

export interface RatingStats {
  producerId: number;
  averageRating: number;
  totalRatings: number;
  fiveStars: number;
  fourStars: number;
  threeStars: number;
  twoStars: number;
  oneStar: number;
}