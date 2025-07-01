import { createContext, useContext, useState, useEffect } from "react";
import apiClient from '../config/apiClient';

export const UnreadAlarmContext = createContext();
export const useUnreadAlarm = () => useContext(UnreadAlarmContext);

export function UnreadAlarmProvider({ children }) {
  const [unreadCount, setUnreadCount] = useState(0);

  // 최초 마운트 시 unreadCount 가져오기
  useEffect(() => {
    const baseUrl = import.meta.env.VITE_API_URL;
    apiClient.get(`/alarm/unread`, { withCredentials: true })
      .then(res => setUnreadCount(res.data))
      .catch(() => setUnreadCount(0));
  }, []);

  return (
    <UnreadAlarmContext.Provider value={{ unreadCount, setUnreadCount }}>
      {children}
    </UnreadAlarmContext.Provider>
  );
}