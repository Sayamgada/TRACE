import { Bell, Search, Menu } from "lucide-react";

interface TopbarProps {
  onMenuClick?: () => void;
}

export function Topbar({ onMenuClick }: TopbarProps) {
  return (
    <header className="topbar">
      <div className="topbar-left">
        <button
          className="mobile-menu-button"
          onClick={onMenuClick}
          aria-label="Open navigation"
        >
          <Menu size={21} />
        </button>

        <div className="search-box">
          <Search size={18} />
          <input type="search" placeholder="Search transactions, accounts..." />
        </div>
      </div>

      <div className="topbar-actions">
        <button className="icon-button" aria-label="Notifications">
          <Bell size={20} />
          <span className="notification-dot" />
        </button>

        <div className="topbar-user">
          <div className="user-avatar small">TC</div>

          <div className="topbar-user-info">
            <strong>TRACE Customer</strong>
            <span>Customer</span>
          </div>
        </div>
      </div>
    </header>
  );
}
