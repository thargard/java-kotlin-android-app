export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface OrderUserRef {
  id: number;
  login?: string;
  fullName?: string;
  email?: string;
  role?: string;
}

export interface OrderDto {
  id: number;
  user?: OrderUserRef | null;
  description?: string | null;
  status: OrderStatus;
  createdAt: string; // ISO-8601 from backend (Instant)
}

export interface GetOrdersParams {
  userId?: number;
  status?: OrderStatus;
  search?: string;
  createdAfter?: string; // ISO-8601
  createdBefore?: string; // ISO-8601
  sort?: string; // e.g. "createdAt,desc"
}
