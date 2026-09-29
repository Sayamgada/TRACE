import { Bell, Globe, Palette, Shield } from "lucide-react";

export default function SettingsPage() {
  return (
    <div className="page-content">
      <div className="page-heading">
        <div>
          <div className="eyebrow">PREFERENCES</div>
          <h1>Settings</h1>
          <p>Manage your TRACE application preferences.</p>
        </div>
      </div>

      <div className="settings-grid">
        <section className="panel settings-card">
          <div className="settings-icon">
            <Bell size={20} />
          </div>

          <div>
            <h2>Notifications</h2>
            <p>
              Transaction and fraud-related notifications are delivered through
              your TRACE notification center.
            </p>
          </div>

          <div className="settings-row">
            <div>
              <strong>Notification center</strong>
              <span>Review alerts and account activity.</span>
            </div>

            <span className="settings-value">Enabled</span>
          </div>
        </section>

        <section className="panel settings-card">
          <div className="settings-icon">
            <Globe size={20} />
          </div>

          <div>
            <h2>Regional preferences</h2>
            <p>Application display preferences for your current session.</p>
          </div>

          <div className="settings-row">
            <div>
              <strong>Currency</strong>
              <span>Used when displaying transaction values.</span>
            </div>

            <span className="settings-value">INR</span>
          </div>

          <div className="settings-row">
            <div>
              <strong>Language</strong>
              <span>Interface language.</span>
            </div>

            <span className="settings-value">English</span>
          </div>
        </section>

        <section className="panel settings-card">
          <div className="settings-icon">
            <Palette size={20} />
          </div>

          <div>
            <h2>Appearance</h2>
            <p>Current interface appearance.</p>
          </div>

          <div className="settings-row">
            <div>
              <strong>Theme</strong>
              <span>TRACE application theme.</span>
            </div>

            <span className="settings-value">Light</span>
          </div>
        </section>

        <section className="panel settings-card">
          <div className="settings-icon">
            <Shield size={20} />
          </div>

          <div>
            <h2>Security</h2>
            <p>Authentication and session controls are available separately.</p>
          </div>

          <div className="settings-row">
            <div>
              <strong>Authentication</strong>
              <span>Manage your current authenticated session.</span>
            </div>

            <span className="settings-value">Protected</span>
          </div>
        </section>
      </div>
    </div>
  );
}
