import { useState } from "react";
import type { FormEvent } from "react";
import { Eye, EyeOff, LockKeyhole, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      await login({
        email,
        password,
      });

      navigate("/dashboard", { replace: true });
    } catch {
      setError("Login failed. Please check your email and password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-brand-panel">
        <div className="login-brand">
          <div className="login-brand-mark">T</div>

          <div>
            <div className="login-brand-name">TRACE</div>
            <div className="login-brand-subtitle">
              Transaction Risk Analysis & Compliance Engine
            </div>
          </div>
        </div>

        <div className="login-brand-content">
          <div className="login-security-icon">
            <ShieldCheck size={28} strokeWidth={1.8} />
          </div>

          <p className="login-eyebrow">SECURE FINANCIAL PLATFORM</p>

          <h1>
            Smarter transaction
            <br />
            <span>risk management.</span>
          </h1>

          <p className="login-description">
            Monitor transactions, identify financial risk, and manage compliance
            through a secure centralized platform.
          </p>
        </div>

        <div className="login-brand-footer">
          <span>SECURE</span>
          <span className="login-footer-dot">•</span>
          <span>MONITORED</span>
          <span className="login-footer-dot">•</span>
          <span>COMPLIANT</span>
        </div>
      </section>

      <section className="login-form-panel">
        <div className="login-form-container">
          <div className="login-mobile-brand">
            <div className="login-brand-mark">T</div>
            <span>TRACE</span>
          </div>

          <div className="login-heading">
            <p className="login-form-eyebrow">WELCOME BACK</p>

            <h2>Sign in to your account</h2>

            <p>Enter your credentials to access the TRACE platform.</p>
          </div>

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-field">
              <label htmlFor="email">Email address</label>

              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                autoComplete="email"
                required
              />
            </div>

            <div className="login-field">
              <div className="login-label-row">
                <label htmlFor="password">Password</label>

                <button
                  type="button"
                  className="login-forgot"
                  onClick={() =>
                    setError("Password recovery is not available yet.")
                  }
                >
                  Forgot password?
                </button>
              </div>

              <div className="login-password-wrapper">
                <LockKeyhole size={17} />

                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  required
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((current) => !current)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            {error && (
              <div className="login-error" role="alert">
                <span className="login-error-icon">!</span>
                <span>{error}</span>
              </div>
            )}

            <button type="submit" className="login-submit" disabled={loading}>
              {loading ? (
                <>
                  <span className="login-spinner" />
                  Signing in...
                </>
              ) : (
                "Sign in"
              )}
            </button>
          </form>

          <div className="login-security-note">
            <ShieldCheck size={16} />

            <span>
              Your connection is protected and your credentials are securely
              processed.
            </span>
          </div>

          <div className="login-copyright">
            © {new Date().getFullYear()} TRACE. All rights reserved.
          </div>
        </div>
      </section>
    </main>
  );
}
