import { useEffect, useMemo, useState } from "react";
import {
  ArrowDownLeft,
  ArrowUpRight,
  CreditCard,
  Plus,
  Wallet,
  RefreshCw,
} from "lucide-react";

import { getMyAccounts } from "../../api/accountApi";
import { getMyTransactions } from "../../api/transactionApi";

import type { Account } from "../../types/account";
import type { Transaction } from "../../types/transaction";

export default function DashboardPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = async () => {
    try {
      setLoading(true);
      setError("");

      const [accountData, transactionData] = await Promise.all([
        getMyAccounts(),
        getMyTransactions(0, 20),
      ]);

      setAccounts(accountData);
      setTransactions(transactionData.content);
    } catch (err) {
      console.error("Failed to load dashboard", err);
      setError("We couldn't load your account information. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDashboard();
  }, []);

  const activeAccounts = useMemo(
    () => accounts.filter((account) => account.status === "ACTIVE"),
    [accounts],
  );

  const primaryAccount = activeAccounts[0] ?? accounts[0];

  const primaryCurrency = primaryAccount?.currency ?? "INR";

  const totalBalance = useMemo(() => {
    if (!primaryAccount) {
      return 0;
    }

    return accounts
      .filter((account) => account.currency === primaryCurrency)
      .reduce((total, account) => total + account.balance, 0);
  }, [accounts, primaryAccount, primaryCurrency]);

  const availableBalance = totalBalance;

  const accountIds = useMemo(
    () => new Set(accounts.map((account) => account.id)),
    [accounts],
  );

  const currentMonthTransactions = useMemo(() => {
    const now = new Date();

    return transactions.filter((transaction) => {
      const date = new Date(transaction.createdAt);

      return (
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth()
      );
    });
  }, [transactions]);

  const moneySent = useMemo(
    () =>
      currentMonthTransactions
        .filter((transaction) => accountIds.has(transaction.senderAccountId))
        .reduce((total, transaction) => total + transaction.amount, 0),
    [currentMonthTransactions, accountIds],
  );

  const moneyReceived = useMemo(
    () =>
      currentMonthTransactions
        .filter((transaction) => accountIds.has(transaction.receiverAccountId))
        .reduce((total, transaction) => total + transaction.amount, 0),
    [currentMonthTransactions, accountIds],
  );

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="dashboard-loading">
          <RefreshCw size={22} className="loading-spinner" />
          <span>Loading your TRACE dashboard...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-page">
        <div className="dashboard-error">
          <div>
            <strong>Unable to load dashboard</strong>
            <p>{error}</p>
          </div>

          <button
            className="secondary-button"
            onClick={() => void loadDashboard()}
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <section className="page-heading">
        <div>
          <div className="eyebrow">Overview</div>
          <h1>Good morning, welcome back.</h1>
          <p>Here's what's happening across your TRACE accounts today.</p>
        </div>

        <button className="primary-button">
          <Plus size={18} />
          New transfer
        </button>
      </section>

      <section className="stats-grid">
        <div className="stat-card primary-stat">
          <div className="stat-card-top">
            <div className="stat-icon">
              <Wallet size={20} />
            </div>

            <span className="stat-label">Total balance</span>
          </div>

          <div className="stat-value">
            {formatMoney(totalBalance, primaryCurrency)}
          </div>

          <div className="stat-footer">
            <span>
              {activeAccounts.length} active{" "}
              {activeAccounts.length === 1 ? "account" : "accounts"}
            </span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon">
              <CreditCard size={20} />
            </div>

            <span className="stat-label">Available balance</span>
          </div>

          <div className="stat-value">
            {formatMoney(availableBalance, primaryCurrency)}
          </div>

          <div className="stat-footer">
            <span>
              {activeAccounts.length} active{" "}
              {activeAccounts.length === 1 ? "account" : "accounts"}
            </span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon">
              <ArrowUpRight size={20} />
            </div>

            <span className="stat-label">Money sent</span>
          </div>

          <div className="stat-value">
            {formatMoney(moneySent, primaryCurrency)}
          </div>

          <div className="stat-footer">
            <span>
              {
                currentMonthTransactions.filter((transaction) =>
                  accountIds.has(transaction.senderAccountId),
                ).length
              }{" "}
              transactions this month
            </span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon">
              <ArrowDownLeft size={20} />
            </div>

            <span className="stat-label">Money received</span>
          </div>

          <div className="stat-value">
            {formatMoney(moneyReceived, primaryCurrency)}
          </div>

          <div className="stat-footer">
            <span className="positive">
              {
                currentMonthTransactions.filter((transaction) =>
                  accountIds.has(transaction.receiverAccountId),
                ).length
              }{" "}
              received this month
            </span>
          </div>
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="panel-header">
            <div>
              <h2>Recent transactions</h2>
              <p>Your latest account activity</p>
            </div>

            <button className="text-button">View all</button>
          </div>

          <div className="transaction-list">
            {transactions.length === 0 ? (
              <div className="empty-state">
                <Wallet size={24} />
                <strong>No transactions yet</strong>
                <span>Your recent account activity will appear here.</span>
              </div>
            ) : (
              transactions.slice(0, 5).map((transaction) => {
                const received = accountIds.has(transaction.receiverAccountId);

                const sent = accountIds.has(transaction.senderAccountId);

                return (
                  <Transaction
                    key={transaction.id}
                    title={getTransactionTitle(transaction, received, sent)}
                    subtitle={`${formatDate(transaction.createdAt)} · ${transaction.status}`}
                    amount={`${received ? "+" : "-"}${formatMoney(
                      transaction.amount,
                      transaction.currency,
                    )}`}
                    positive={received}
                    initials={getInitials(transaction.transactionType)}
                  />
                );
              })
            )}
          </div>
        </div>

        <div className="panel">
          <div className="panel-header">
            <div>
              <h2>Primary account</h2>
              <p>Your main spending account</p>
            </div>

            {primaryAccount && (
              <span
                className={`status-badge ${
                  primaryAccount.status === "ACTIVE" ? "success" : ""
                }`}
              >
                {formatStatus(primaryAccount.status)}
              </span>
            )}
          </div>

          {primaryAccount ? (
            <>
              <div className="account-card">
                <div className="account-card-top">
                  <span>TRACE</span>
                  <CreditCard size={24} />
                </div>

                <div className="account-number">
                  {maskAccountNumber(primaryAccount.accountNumber)}
                </div>

                <div className="account-card-bottom">
                  <div>
                    <span>Available balance</span>
                    <strong>
                      {formatMoney(
                        primaryAccount.balance,
                        primaryAccount.currency,
                      )}
                    </strong>
                  </div>

                  <div>
                    <span>Currency</span>
                    <strong>{primaryAccount.currency}</strong>
                  </div>
                </div>
              </div>

              <button className="secondary-button full-width">
                View account details
              </button>
            </>
          ) : (
            <div className="empty-state account-empty-state">
              <Wallet size={24} />
              <strong>No accounts found</strong>
              <span>Create an account to start using TRACE.</span>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}

interface TransactionProps {
  title: string;
  subtitle: string;
  amount: string;
  initials: string;
  positive?: boolean;
}

function Transaction({
  title,
  subtitle,
  amount,
  initials,
  positive,
}: TransactionProps) {
  return (
    <div className="transaction-row">
      <div className="transaction-icon">{initials}</div>

      <div className="transaction-info">
        <strong>{title}</strong>
        <span>{subtitle}</span>
      </div>

      <div className={`transaction-amount ${positive ? "positive" : ""}`}>
        {amount}
      </div>
    </div>
  );
}

function formatMoney(amount: number, currency: string) {
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      minimumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${currency} ${amount.toFixed(2)}`;
  }
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(value));
}

function maskAccountNumber(accountNumber: string) {
  if (!accountNumber) {
    return "•••• ••••";
  }

  const lastFour = accountNumber.slice(-4);

  return `•••• •••• •••• ${lastFour}`;
}

function formatStatus(status: string) {
  return status
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function getInitials(transactionType: string) {
  const words = transactionType
    .toLowerCase()
    .replace(/_/g, " ")
    .split(" ")
    .filter(Boolean);

  if (words.length === 0) {
    return "T";
  }

  if (words.length === 1) {
    return words[0][0].toUpperCase();
  }

  return `${words[0][0]}${words[1][0]}`.toUpperCase();
}

function getTransactionTitle(
  transaction: Transaction,
  received: boolean,
  sent: boolean,
) {
  const type = transaction.transactionType.toLowerCase().replace(/_/g, " ");

  if (received) {
    return `Received ${type}`;
  }

  if (sent) {
    return `Transfer ${type}`;
  }

  return type.replace(/\b\w/g, (letter) => letter.toUpperCase());
}
