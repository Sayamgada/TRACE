import {
  ArrowLeftRight,
  Bell,
  ChevronDown,
  CreditCard,
  LayoutDashboard,
  LogOut,
  Settings,
  ShieldCheck,
  WalletCards,
} from "lucide-react";
import { NavLink } from "react-router-dom";

interface SidebarProps {
  onLogout: () => void;
}

const navigation = [
  {
    label: "Dashboard",
    to: "/dashboard",
    icon: LayoutDashboard,
  },
  {
    label: "Accounts",
    to: "/accounts",
    icon: WalletCards,
  },
  {
    label: "Transfers",
    to: "/transfers",
    icon: ArrowLeftRight,
  },
  {
    label: "Transactions",
    to: "/transactions",
    icon: CreditCard,
  },
];

const secondaryNavigation = [
  {
    label: "Notifications",
    to: "/notifications",
    icon: Bell,
  },
  {
    label: "Security",
    to: "/security",
    icon: ShieldCheck,
  },
  {
    label: "Settings",
    to: "/settings",
    icon: Settings,
  },
];

export function Sidebar({ onLogout }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-mark">T</div>

        <div>
          <div className="brand-name">TRACE</div>
          <div className="brand-subtitle">Financial Intelligence</div>
        </div>
      </div>

      <div className="sidebar-section">
        <div className="sidebar-label">Workspace</div>

        <nav className="sidebar-nav">
          {navigation.map(({ label, to, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active" : ""}`
              }
            >
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="sidebar-section">
        <div className="sidebar-label">Account</div>

        <nav className="sidebar-nav">
          {secondaryNavigation.map(({ label, to, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active" : ""}`
              }
            >
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="sidebar-footer">
        <div className="user-mini">
          <div className="user-avatar">TC</div>

          <div className="user-mini-info">
            <strong>TRACE Customer</strong>
            <span>Personal account</span>
          </div>

          <ChevronDown size={16} />
        </div>

        <button className="logout-button" onClick={onLogout}>
          <LogOut size={17} />
          Sign out
        </button>
      </div>
    </aside>
  );
}
