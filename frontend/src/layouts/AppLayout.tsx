import { useState } from "react";
import { Outlet } from "react-router-dom";

import { Sidebar } from "../components/layout/Sidebar";
import { Topbar } from "../components/layout/Topbar";
import { useAuth } from "../auth/AuthContext";

export function AppLayout() {
  const { logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="app-shell">
      <div className={`mobile-sidebar-overlay ${sidebarOpen ? "visible" : ""}`}>
        <div
          className="mobile-overlay-backdrop"
          onClick={() => setSidebarOpen(false)}
        />
      </div>

      <div className={`app-sidebar ${sidebarOpen ? "open" : ""}`}>
        <Sidebar onLogout={logout} />
      </div>

      <div className="app-main">
        <Topbar onMenuClick={() => setSidebarOpen(true)} />

        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
