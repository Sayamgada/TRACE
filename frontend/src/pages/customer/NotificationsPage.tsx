import { useEffect, useState } from "react";
import {
  AlertTriangle,
  Bell,
  CheckCircle2,
  RefreshCw,
  ShieldAlert,
} from "lucide-react";

import {
  getMyNotifications,
  markNotificationAsRead,
} from "../../api/notificationApi";
import type { Notification } from "../../types/notification";

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function getNotificationIcon(type: Notification["type"]) {
  switch (type) {
    case "TRANSACTION_FLAGGED":
      return <AlertTriangle size={18} />;

    case "TRANSACTION_BLOCKED":
      return <ShieldAlert size={18} />;

    case "FRAUD_CASE":
      return <ShieldAlert size={18} />;

    default:
      return <Bell size={18} />;
  }
}

function getNotificationLabel(type: Notification["type"]) {
  switch (type) {
    case "TRANSACTION_FLAGGED":
      return "Transaction flagged";

    case "TRANSACTION_BLOCKED":
      return "Transaction blocked";

    case "FRAUD_CASE":
      return "Fraud case";

    default:
      return "Notification";
  }
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [filter, setFilter] = useState<"all" | "unread">("all");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadNotifications = async (requestedPage = page) => {
    setLoading(true);
    setError("");

    try {
      const response = await getMyNotifications(
        requestedPage,
        20,
        filter === "unread" ? false : undefined,
      );

      setNotifications(response.content);
      setPage(response.number);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch {
      setError("Unable to load your notifications.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setPage(0);
  }, [filter]);

  useEffect(() => {
    void loadNotifications(page);
  }, [page, filter]);

  const handleMarkAsRead = async (notification: Notification) => {
    if (notification.read) {
      return;
    }

    try {
      const updated = await markNotificationAsRead(notification.id);

      setNotifications((current) =>
        current.map((item) => (item.id === updated.id ? updated : item)),
      );
    } catch {
      setError("Unable to mark the notification as read.");
    }
  };

  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">CUSTOMER ALERTS</div>
          <h1>Notifications</h1>
          <p>Stay informed about activity on your TRACE account.</p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => void loadNotifications(page)}
          disabled={loading}
        >
          <RefreshCw size={15} className={loading ? "loading-spinner" : ""} />
          Refresh
        </button>
      </div>

      <section className="panel notifications-panel">
        <div className="notifications-toolbar">
          <div>
            <h2>Alerts</h2>
            <span>
              {totalElements} notification
              {totalElements === 1 ? "" : "s"}
            </span>
          </div>

          <div className="notification-filters">
            <button
              type="button"
              className={filter === "all" ? "active" : ""}
              onClick={() => setFilter("all")}
            >
              All
            </button>

            <button
              type="button"
              className={filter === "unread" ? "active" : ""}
              onClick={() => setFilter("unread")}
            >
              Unread
            </button>
          </div>
        </div>

        {loading ? (
          <div className="dashboard-loading notifications-loading">
            <RefreshCw size={19} className="loading-spinner" />
            <span>Loading notifications...</span>
          </div>
        ) : error ? (
          <div className="dashboard-error">
            <span>{error}</span>

            <button
              type="button"
              className="secondary-button"
              onClick={() => void loadNotifications(page)}
            >
              Try again
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="accounts-empty">
            <CheckCircle2 size={28} />
            <strong>
              {filter === "unread"
                ? "No unread notifications"
                : "No notifications"}
            </strong>
            <span>New transaction and fraud alerts will appear here.</span>
          </div>
        ) : (
          <div className="notifications-list">
            {notifications.map((notification) => (
              <article
                key={notification.id}
                className={`notification-item ${
                  notification.read ? "" : "notification-unread"
                }`}
              >
                <div className="notification-icon">
                  {getNotificationIcon(notification.type)}
                </div>

                <div className="notification-content">
                  <div className="notification-title-row">
                    <div>
                      <span className="notification-type">
                        {getNotificationLabel(notification.type)}
                      </span>

                      <h3>{notification.title}</h3>
                    </div>

                    {!notification.read && (
                      <span className="notification-unread-dot" />
                    )}
                  </div>

                  <p>{notification.message}</p>

                  <div className="notification-meta">
                    <span>{formatDate(notification.createdAt)}</span>

                    {notification.transactionId && (
                      <span>Transaction #{notification.transactionId}</span>
                    )}

                    {notification.fraudCaseId && (
                      <span>Fraud case #{notification.fraudCaseId}</span>
                    )}
                  </div>
                </div>

                {!notification.read && (
                  <button
                    type="button"
                    className="notification-read-button"
                    onClick={() => void handleMarkAsRead(notification)}
                  >
                    Mark as read
                  </button>
                )}
              </article>
            ))}
          </div>
        )}

        {!loading && !error && notifications.length > 0 && totalPages > 1 && (
          <div className="transactions-pagination">
            <span>
              Page {page + 1} of {totalPages}
            </span>

            <div>
              <button
                type="button"
                className="secondary-button"
                disabled={page === 0}
                onClick={() => setPage((current) => current - 1)}
              >
                Previous
              </button>

              <button
                type="button"
                className="secondary-button"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((current) => current + 1)}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
