export interface Transaction {
  id: number;
  transactionReference: string;
  senderAccountId: number;
  receiverAccountId: number;
  amount: number;
  currency: string;
  transactionType: string;
  status: string;
  riskScore: number | null;
  createdAt: string;
  processedAt: string | null;
}

export interface TransactionPage {
  content: Transaction[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  number: number;
  size: number;
  numberOfElements: number;
  empty: boolean;
}
