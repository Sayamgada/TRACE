export type NotificationType =
  | "TRANSACTION_FLAGGED"
  | "TRANSACTION_BLOCKED"
  | "FRAUD_CASE";

export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  transactionId: number | null;
  fraudCaseId: number | null;
  read: boolean;
  createdAt: string;
  readAt: string | null;
}

export interface NotificationPage {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
