import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";

import type { TokenRefreshResponse } from "../types/auth";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshPromise: Promise<string> | null = null;

const performTokenRefresh = async (): Promise<string> => {
  const refreshToken = localStorage.getItem("trace_refresh_token");

  if (!refreshToken) {
    throw new Error("No refresh token available");
  }

  // Separate axios request so the refresh call does not
  // pass through the protected API interceptor.
  const response = await axios.post<TokenRefreshResponse>(
    `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
    {
      refreshToken,
    },
    {
      headers: {
        "Content-Type": "application/json",
      },
    },
  );

  localStorage.setItem("trace_access_token", response.data.accessToken);
  localStorage.setItem("trace_refresh_token", response.data.refreshToken);

  return response.data.accessToken;
};

apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem("trace_access_token");

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url?.includes("/api/auth/login") ||
      originalRequest.url?.includes("/api/auth/refresh") ||
      originalRequest.url?.includes("/api/auth/logout")
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = performTokenRefresh().finally(() => {
          refreshPromise = null;
        });
      }

      const newAccessToken = await refreshPromise;

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      localStorage.removeItem("trace_access_token");
      localStorage.removeItem("trace_refresh_token");

      window.location.href = "/login";

      return Promise.reject(refreshError);
    }
  },
);

export default apiClient;
