import { ShieldCheck, LogOut, LockKeyhole, UserCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../../auth/AuthContext";

export default function SecurityPage() {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">ACCOUNT SECURITY</div>
          <h1>Security</h1>
          <p>Manage your TRACE authentication session.</p>
        </div>
      </div>

      <div className="security-grid">
        <section className="panel security-card">
          <div className="security-card-icon">
            <ShieldCheck size={20} />
          </div>

          <div className="security-card-heading">
            <div>
              <h2>Authentication</h2>
              <p>Your current TRACE authentication status.</p>
            </div>

            <span className="security-status">
              <span />
              {isAuthenticated ? "Authenticated" : "Signed out"}
            </span>
          </div>

          <div className="security-detail-list">
            <div className="security-detail">
              <div className="security-detail-icon">
                <UserCheck size={17} />
              </div>

              <div>
                <strong>Account session</strong>
                <span>
                  {isAuthenticated
                    ? "Your current session is authenticated."
                    : "No active authenticated session."}
                </span>
              </div>
            </div>

            <div className="security-detail">
              <div className="security-detail-icon">
                <LockKeyhole size={17} />
              </div>

              <div>
                <strong>Protected access</strong>
                <span>
                  TRACE uses authenticated API requests to protect customer
                  resources.
                </span>
              </div>
            </div>
          </div>
        </section>

        <section className="panel security-card">
          <div className="security-card-icon">
            <LockKeyhole size={20} />
          </div>

          <h2>Session security</h2>

          <p className="security-description">
            Your authentication tokens are stored locally by the frontend and
            attached to protected API requests.
          </p>

          <div className="security-warning">
            <strong>Keep your session private</strong>
            <span>
              Never share authentication credentials or tokens with another
              person.
            </span>
          </div>

          <button
            type="button"
            className="danger-button"
            onClick={handleLogout}
          >
            <LogOut size={16} />
            Sign out
          </button>
        </section>
      </div>
    </div>
  );
}
