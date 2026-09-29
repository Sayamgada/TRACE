import apiClient from "./client";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegistrationResponse,
  RefreshTokenRequest,
  TokenRefreshResponse,
} from "../types/auth";

export const login = async (request: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>(
    "/api/auth/login",
    request,
  );

  return response.data;
};

export const register = async (
  request: RegisterRequest,
): Promise<RegistrationResponse> => {
  const response = await apiClient.post<RegistrationResponse>(
    "/api/auth/register",
    request,
  );

  return response.data;
};

export const refreshToken = async (
  request: RefreshTokenRequest,
): Promise<TokenRefreshResponse> => {
  const response = await apiClient.post<TokenRefreshResponse>(
    "/api/auth/refresh",
    request,
  );

  return response.data;
};

export const logout = async (refreshTokenValue: string): Promise<void> => {
  await apiClient.post("/api/auth/logout", {
    refreshToken: refreshTokenValue,
  });
};
