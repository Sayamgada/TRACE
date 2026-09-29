import apiClient from "./client";
import type { Notification, NotificationPage } from "../types/notification";

export const getMyNotifications = async (
  page = 0,
  size = 20,
  read?: boolean,
): Promise<NotificationPage> => {
  const response = await apiClient.get<NotificationPage>("/api/notifications", {
    params: {
      page,
      size,
      sort: "createdAt,desc",
      ...(read === undefined ? {} : { read }),
    },
  });

  return response.data;
};

export const getUnreadNotificationCount = async (): Promise<number> => {
  const response = await apiClient.get<number>(
    "/api/notifications/unread/count",
  );

  return response.data;
};

export const markNotificationAsRead = async (
  notificationId: number,
): Promise<Notification> => {
  const response = await apiClient.patch<Notification>(
    `/api/notifications/${notificationId}/read`,
  );

  return response.data;
};
