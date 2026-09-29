import { Navigate, Route, Routes } from "react-router-dom";

import { AppLayout } from "./layouts/AppLayout";
import AccountsPage from "./pages/customer/AccountsPage";
import DashboardPage from "./pages/customer/DashboardPage";
import LoginPage from "./pages/auth/LoginPage";
import ProtectedRoute from "./routes/ProtectedRoute";
import TransfersPage from "./pages/customer/TransfersPage";
import TransactionsPage from "./pages/customer/TransactionsPage";
import NotificationsPage from "./pages/customer/NotificationsPage";
import SecurityPage from "./pages/customer/SecurityPage";
import SettingsPage from "./pages/customer/SettingsPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="accounts" element={<AccountsPage />} />
          <Route path="transfers" element={<TransfersPage />} />
          <Route path="transactions" element={<TransactionsPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="security" element={<SecurityPage />} />
          <Route path="settings" element={<SettingsPage />} />
          
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
