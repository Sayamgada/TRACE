import apiClient from "./client";
import type { Account } from "../types/account";

export const getMyAccounts = async (): Promise<Account[]> => {
  const response = await apiClient.get<Account[]>("/api/accounts");
  return response.data;
};

export const getAccount = async (accountId: number): Promise<Account> => {
  const response = await apiClient.get<Account>(`/api/accounts/${accountId}`);
  return response.data;
};
