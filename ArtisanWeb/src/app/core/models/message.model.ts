export interface MessageDto {
  id: number;
  senderId: number;
  senderName?: string;
  receiverId: number;
  receiverName?: string;
  content: string;
  threadId: number | null;
  productId: number | null;
  createdAt: string;
  isRead: boolean;
}

export interface StartConversationResponse {
  id: number;
  threadId: number | null;
  message?: string;
  [key: string]: unknown;
}
