import apiClient from "./client";
import type { Transaction, TransactionPage } from "../types/transaction";

export interface CreateTransactionRequest {
  senderAccountId: number;
  receiverAccountId: number;
  amount: number;
  currency: string;
  transactionType: string;
}

export const createTransaction = async (
  request: CreateTransactionRequest,
): Promise<Transaction> => {
  const response = await apiClient.post<Transaction>(
    "/api/transactions",
    request,
  );

  return response.data;
};

export const getMyTransactions = async (
  page = 0,
  size = 20,
): Promise<TransactionPage> => {
  const response = await apiClient.get<TransactionPage>("/api/transactions", {
    params: {
      page,
      size,
      sort: "createdAt,desc",
    },
  });

  return response.data;
};
