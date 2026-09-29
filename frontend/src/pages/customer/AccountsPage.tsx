import { useEffect, useMemo, useState } from "react";
import { ArrowUpRight, CreditCard, RefreshCw, Wallet } from "lucide-react";
import { getMyAccounts } from "../../api/accountApi";
import type { Account } from "../../types/account";

function formatMoney(amount: number, currency: string) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(amount);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(value));
}

function maskAccountNumber(accountNumber: string) {
  if (accountNumber.length <= 4) {
    return accountNumber;
  }

  return `•••• •••• ${accountNumber.slice(-4)}`;
}

function formatStatus(status: Account["status"]) {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

export default function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadAccounts = async () => {
    setLoading(true);
    setError("");

    try {
      const data = await getMyAccounts();
      setAccounts(data);
    } catch {
      setError("Unable to load your accounts. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadAccounts();
  }, []);

  const totalBalance = useMemo(() => {
    return accounts.reduce((total, account) => {
      return total + Number(account.balance);
    }, 0);
  }, [accounts]);

  const activeAccounts = useMemo(
    () => accounts.filter((account) => account.status === "ACTIVE"),
    [accounts],
  );

  const primaryCurrency = accounts[0]?.currency ?? "INR";

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

  if (error) {
    return (
      <div className="page-content">
        <div className="dashboard-error">
          <strong>Unable to load accounts</strong>
          <p>{error}</p>

          <button
            type="button"
            className="primary-button"
            onClick={() => void loadAccounts()}
          >
            <RefreshCw size={15} />
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">CUSTOMER BANKING</div>
          <h1>My accounts</h1>
          <p>View your accounts, balances, and account details.</p>
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

      <section className="account-summary-grid">
        <div className="account-summary-card">
          <div className="account-summary-icon">
            <Wallet size={18} />
          </div>

          <div>
            <span>Total balance</span>
            <strong>{formatMoney(totalBalance, primaryCurrency)}</strong>
          </div>
        </div>

        <div className="account-summary-card">
          <div className="account-summary-icon">
            <CreditCard size={18} />
          </div>

          <div>
            <span>Total accounts</span>
            <strong>{accounts.length}</strong>
          </div>
        </div>

        <div className="account-summary-card">
          <div className="account-summary-icon">
            <ArrowUpRight size={18} />
          </div>

          <div>
            <span>Active accounts</span>
            <strong>{activeAccounts.length}</strong>
          </div>
        </div>
      </section>

      {accounts.length === 0 ? (
        <div className="panel accounts-empty">
          <Wallet size={28} />
          <strong>No accounts found</strong>
          <span>
            There are currently no accounts associated with your profile.
          </span>
        </div>
      ) : (
        <section className="accounts-section">
          <div className="section-heading">
            <div>
              <h2>Your accounts</h2>
              <p>All accounts associated with your customer profile.</p>
            </div>
          </div>

          <div className="accounts-grid">
            {accounts.map((account) => (
              <article className="customer-account-card" key={account.id}>
                <div className="customer-account-card-top">
                  <div className="customer-account-icon">
                    <CreditCard size={19} />
                  </div>

                  <span
                    className={`account-status ${
                      account.status === "ACTIVE"
                        ? "account-status-active"
                        : "account-status-inactive"
                    }`}
                  >
                    {formatStatus(account.status)}
                  </span>
                </div>

                <div className="customer-account-balance">
                  <span>Current balance</span>

                  <strong>
                    {formatMoney(Number(account.balance), account.currency)}
                  </strong>
                </div>

                <div className="customer-account-number">
                  <span>ACCOUNT NUMBER</span>
                  <strong>{maskAccountNumber(account.accountNumber)}</strong>
                </div>

                <div className="customer-account-footer">
                  <div>
                    <span>Currency</span>
                    <strong>{account.currency}</strong>
                  </div>

                  <div>
                    <span>Opened</span>
                    <strong>{formatDate(account.createdAt)}</strong>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
