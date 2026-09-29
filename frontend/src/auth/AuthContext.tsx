import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import { login as loginRequest } from "../api/authApi";
import type { LoginRequest } from "../types/auth";

interface AuthContextValue {
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    Boolean(localStorage.getItem("trace_access_token")),
  );

  const login = async (request: LoginRequest) => {
    const response = await loginRequest(request);

    localStorage.setItem("trace_access_token", response.accessToken);
    localStorage.setItem("trace_refresh_token", response.refreshToken);

    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem("trace_access_token");
    localStorage.removeItem("trace_refresh_token");

    setIsAuthenticated(false);
  };

  const value = useMemo(
    () => ({
      isAuthenticated,
      login,
      logout,
    }),
    [isAuthenticated],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
