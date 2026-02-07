export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  sellerId: number;
  sellerName: string;
  category: string;
  createdAt: Date;
  isAvailable: boolean;
}
