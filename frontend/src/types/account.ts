export type AccountStatus = "ACTIVE" | "FROZEN" | "SUSPENDED" | "CLOSED";

export interface Account {
  id: number;
  accountNumber: string;
  balance: number;
  currency: string;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
}
