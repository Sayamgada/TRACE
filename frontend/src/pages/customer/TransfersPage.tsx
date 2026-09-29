import type { FormEvent } from "react";
import { useEffect, useMemo, useState } from "react";
import {
  ArrowRight,
  CheckCircle2,
  CreditCard,
  RefreshCw,
  Send,
  WalletCards,
} from "lucide-react";

import { getMyAccounts } from "../../api/accountApi";
import { createTransaction } from "../../api/transactionApi";
import type { Account } from "../../types/account";

function formatMoney(amount: number, currency: string) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(amount);
}

function maskAccountNumber(accountNumber: string) {
  if (accountNumber.length <= 4) {
    return accountNumber;
  }

  return `•••• •••• ${accountNumber.slice(-4)}`;
}

export default function TransfersPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [senderAccountId, setSenderAccountId] = useState("");
  const [receiverAccountId, setReceiverAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [transactionType, setTransactionType] = useState("TRANSFER");

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadAccounts = async () => {
    setLoading(true);
    setError("");

    try {
      const data = await getMyAccounts();
      setAccounts(data);

      const firstActive = data.find((account) => account.status === "ACTIVE");

      if (firstActive) {
        setSenderAccountId(String(firstActive.id));
      }
    } catch {
      setError("Unable to load your accounts. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadAccounts();
  }, []);

  const senderAccount = useMemo(
    () => accounts.find((account) => String(account.id) === senderAccountId),
    [accounts, senderAccountId],
  );

  const activeAccounts = useMemo(
    () => accounts.filter((account) => account.status === "ACTIVE"),
    [accounts],
  );

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    const numericAmount = Number(amount);

    if (!senderAccountId || !receiverAccountId) {
      setError("Please select both sender and recipient accounts.");
      return;
    }

    if (senderAccountId === receiverAccountId) {
      setError("Sender and recipient accounts must be different.");
      return;
    }

    if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
      setError("Enter a valid transfer amount.");
      return;
    }

    if (senderAccount && numericAmount > Number(senderAccount.balance)) {
      setError("The transfer amount exceeds your available balance.");
      return;
    }

    setSubmitting(true);

    try {
      const transaction = await createTransaction({
        senderAccountId: Number(senderAccountId),
        receiverAccountId: Number(receiverAccountId),
        amount: numericAmount,
        currency: senderAccount?.currency ?? "INR",
        transactionType,
      });

      setSuccess(
        `Transfer submitted successfully. Reference: ${transaction.transactionReference}`,
      );

      setAmount("");

      await loadAccounts();
    } catch (requestError: any) {
      const message =
        requestError?.response?.data?.message ??
        "Unable to complete the transfer. Please try again.";

      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="page-content">
        <div className="dashboard-loading">
          <RefreshCw size={19} className="loading-spinner" />
          <span>Loading your accounts...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">CUSTOMER BANKING</div>
          <h1>Transfer money</h1>
          <p>
            Send money between accounts through the TRACE transaction engine.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => void loadAccounts()}
        >
          <RefreshCw size={15} />
          Refresh
        </button>
      </div>

      <div className="transfer-layout">
        <section className="panel transfer-panel">
          <div className="transfer-panel-header">
            <div className="transfer-icon">
              <Send size={19} />
            </div>

            <div>
              <h2>New transfer</h2>
              <p>Enter the recipient and amount below.</p>
            </div>
          </div>

          {error && (
            <div className="form-message form-message-error">{error}</div>
          )}

          {success && (
            <div className="form-message form-message-success">
              <CheckCircle2 size={17} />
              <span>{success}</span>
            </div>
          )}

          {activeAccounts.length === 0 ? (
            <div className="accounts-empty">
              <WalletCards size={28} />
              <strong>No active accounts available</strong>
              <span>
                You need an active account before you can make a transfer.
              </span>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="transfer-form">
              <div className="form-field">
                <label htmlFor="senderAccount">From account</label>

                <select
                  id="senderAccount"
                  value={senderAccountId}
                  onChange={(event) => setSenderAccountId(event.target.value)}
                  disabled={submitting}
                >
                  {activeAccounts.map((account) => (
                    <option key={account.id} value={account.id}>
                      {maskAccountNumber(account.accountNumber)} —{" "}
                      {formatMoney(Number(account.balance), account.currency)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="transfer-arrow">
                <ArrowRight size={18} />
              </div>

              <div className="form-field">
                <label htmlFor="receiverAccount">Recipient account</label>

                <select
                  id="receiverAccount"
                  value={receiverAccountId}
                  onChange={(event) => setReceiverAccountId(event.target.value)}
                  disabled={submitting}
                >
                  <option value="">Select recipient</option>

                  {activeAccounts
                    .filter((account) => String(account.id) !== senderAccountId)
                    .map((account) => (
                      <option key={account.id} value={account.id}>
                        {maskAccountNumber(account.accountNumber)}
                      </option>
                    ))}
                </select>
              </div>

              <div className="form-field">
                <label htmlFor="amount">Amount</label>

                <div className="amount-input">
                  <span>{senderAccount?.currency ?? "INR"}</span>

                  <input
                    id="amount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    placeholder="0.00"
                    value={amount}
                    onChange={(event) => setAmount(event.target.value)}
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="transactionType">Transaction type</label>

                <select
                  id="transactionType"
                  value={transactionType}
                  onChange={(event) => setTransactionType(event.target.value)}
                  disabled={submitting}
                >
                  <option value="TRANSFER">Transfer</option>
                </select>
              </div>

              <button
                type="submit"
                className="primary-button transfer-submit"
                disabled={submitting}
              >
                {submitting ? (
                  <>
                    <RefreshCw size={16} className="loading-spinner" />
                    Processing...
                  </>
                ) : (
                  <>
                    <Send size={16} />
                    Send transfer
                  </>
                )}
              </button>
            </form>
          )}
        </section>

        <aside className="panel transfer-info-panel">
          <div className="transfer-info-icon">
            <CreditCard size={20} />
          </div>

          <h2>Transfer summary</h2>

          <div className="transfer-summary-row">
            <span>From</span>
            <strong>
              {senderAccount
                ? maskAccountNumber(senderAccount.accountNumber)
                : "—"}
            </strong>
          </div>

          <div className="transfer-summary-row">
            <span>Available balance</span>
            <strong>
              {senderAccount
                ? formatMoney(
                    Number(senderAccount.balance),
                    senderAccount.currency,
                  )
                : "—"}
            </strong>
          </div>

          <div className="transfer-summary-row">
            <span>Transfer amount</span>
            <strong>
              {senderAccount && amount
                ? formatMoney(Number(amount), senderAccount.currency)
                : "—"}
            </strong>
          </div>

          <div className="transfer-security-note">
            <strong>TRACE risk analysis</strong>
            <span>
              Every transfer is validated and evaluated by the backend risk
              engine before its final status is determined.
            </span>
          </div>
        </aside>
      </div>
    </div>
  );
}
