// src/app/core/models/message.model.ts

export interface MessageDto {
  id: number;
  senderId: number;
  senderName: string;
  receiverId: number;
  receiverName: string;
  content: string;
  createdAt: string;
  isRead: boolean;
}

export interface ConversationDto {
  otherUserId: number;
  otherUserName: string;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
  isLastMessageFromMe: boolean;
}

export interface SendMessageRequest {
  receiverId: number;
  content: string;
}

export interface ConversationResponse {
  otherUserId: number;
  messages: MessageDto[];
}

export interface ConversationsResponse {
  conversations: ConversationDto[];
}

export interface UnreadCountResponse {
  unreadCount: number;
}