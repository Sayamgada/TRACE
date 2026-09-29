import { useEffect, useState } from "react";
import { ArrowDownLeft, ArrowUpRight, RefreshCw } from "lucide-react";

import { getMyTransactions } from "../../api/transactionApi";
import type { Transaction } from "../../types/transaction";

function formatMoney(amount: number, currency: string) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(amount);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function getStatusClass(status: string) {
  return `transaction-status transaction-status-${status.toLowerCase()}`;
}

function getTransactionLabel(transaction: Transaction) {
  return transaction.transactionType
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadTransactions = async (requestedPage = page) => {
    setLoading(true);
    setError("");

    try {
      const response = await getMyTransactions(requestedPage, 20);

      setTransactions(response.content);
      setPage(response.number);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch {
      setError("Unable to load your transaction history.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadTransactions(page);
  }, [page]);

  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">CUSTOMER BANKING</div>
          <h1>Transactions</h1>
          <p>Review your recent transaction activity.</p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => void loadTransactions(page)}
          disabled={loading}
        >
          <RefreshCw size={15} className={loading ? "loading-spinner" : ""} />
          Refresh
        </button>
      </div>

      <section className="panel transactions-panel">
        <div className="transactions-header">
          <div>
            <h2>Transaction history</h2>
            <p>
              {totalElements} transaction
              {totalElements === 1 ? "" : "s"}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="dashboard-loading transactions-loading">
            <RefreshCw size={19} className="loading-spinner" />
            <span>Loading transactions...</span>
          </div>
        ) : error ? (
          <div className="dashboard-error">
            <span>{error}</span>

            <button
              type="button"
              className="secondary-button"
              onClick={() => void loadTransactions(page)}
            >
              Try again
            </button>
          </div>
        ) : transactions.length === 0 ? (
          <div className="accounts-empty">
            <ArrowUpRight size={28} />
            <strong>No transactions yet</strong>
            <span>
              Your completed and processed transfers will appear here.
            </span>
          </div>
        ) : (
          <>
            <div className="transactions-table-wrapper">
              <table className="transactions-table">
                <thead>
                  <tr>
                    <th>Transaction</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Risk</th>
                    <th>Date</th>
                  </tr>
                </thead>

                <tbody>
                  {transactions.map((transaction) => {
                    const isOutgoing =
                      transaction.transactionType !== "DEPOSIT";

                    return (
                      <tr key={transaction.id}>
                        <td>
                          <div className="transaction-reference">
                            <div className="transaction-direction-icon">
                              {isOutgoing ? (
                                <ArrowUpRight size={15} />
                              ) : (
                                <ArrowDownLeft size={15} />
                              )}
                            </div>

                            <div>
                              <strong>
                                {transaction.transactionReference}
                              </strong>

                              <span>ID #{transaction.id}</span>
                            </div>
                          </div>
                        </td>

                        <td>
                          <span className="transaction-type">
                            {getTransactionLabel(transaction)}
                          </span>
                        </td>

                        <td>
                          <strong
                            className={
                              isOutgoing
                                ? "transaction-amount outgoing"
                                : "transaction-amount incoming"
                            }
                          >
                            {isOutgoing ? "-" : "+"}
                            {formatMoney(
                              Number(transaction.amount),
                              transaction.currency,
                            )}
                          </strong>
                        </td>

                        <td>
                          <span className={getStatusClass(transaction.status)}>
                            {transaction.status.replaceAll("_", " ")}
                          </span>
                        </td>

                        <td>
                          <span className="risk-score">
                            {transaction.riskScore == null
                              ? "—"
                              : `${transaction.riskScore}/100`}
                          </span>
                        </td>

                        <td>
                          <span className="transaction-date">
                            {formatDate(transaction.createdAt)}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="transactions-pagination">
              <span>
                Page {page + 1} of {Math.max(totalPages, 1)}
              </span>

              <div>
                <button
                  type="button"
                  className="secondary-button"
                  disabled={page === 0 || loading}
                  onClick={() => setPage((current) => current - 1)}
                >
                  Previous
                </button>

                <button
                  type="button"
                  className="secondary-button"
                  disabled={page >= totalPages - 1 || loading}
                  onClick={() => setPage((current) => current + 1)}
                >
                  Next
                </button>
              </div>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
