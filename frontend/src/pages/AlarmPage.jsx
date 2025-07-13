import { useState, useEffect } from "react";
import { useUser } from '../contexts/UserContext';
import { useUnreadAlarm } from '../contexts/UnreadAlarmContext';
import TitleBar from "../components/TitleBar";
import apiClient from '../config/apiClient';
import '../styles/Alarm.css';

const Alarm = () => {
  apiClient.defaults.withCredentials = true;
  const user = useUser();
  const [alarms, setAlarms] = useState([]);
  const [loading, setLoading] = useState(true);
  const { setUnreadCount } = useUnreadAlarm();
  const [selectedType, setSelectedType] = useState('all'); // all, estimate, request, review

  // 알람 목록 불러오기
  const fetchAlarms = () => {
    setLoading(true);
    let url =
      selectedType === 'all'
        ? `/alarm/list`
        : `/alarm/list?type=${selectedType}`;
    apiClient
      .get(url, { headers: { Accept: "application/json" } })
      .then(res => {
        setAlarms(Array.isArray(res.data) ? res.data : []);
      })
      .catch(() => setAlarms([]))
      .finally(() => setLoading(false));
  };

  // 미읽음 알림 개수 fetch
  const fetchUnreadCount = () => {
    apiClient.get(`/alarm/unread`, { withCredentials: true })
    .then(res => setUnreadCount(res.data))
    .catch(() => setUnreadCount(0));
  };

  useEffect(() => {
    fetchAlarms();
    fetchUnreadCount();
  }, [selectedType]);

  // 알람 읽음 처리
  const handleRead = async (idx) => {
    try {
      await apiClient.post(`/alarm/read?idx=${idx}`, null);
      fetchAlarms();
      fetchUnreadCount();
    } catch (err) {
      // 에러 핸들링
    }
  };

  // 시간 포맷
  const formatTime = (createdAt) => {
    const now = new Date();
    const created = new Date(createdAt);
    const diffMin = Math.floor((now - created) / 60000);
    if (diffMin < 60) return `${diffMin}분 전`;
    return createdAt.split("T")[0];
  };

  return (
    <>
      <TitleBar title="알림" />
      <div className="alarm">
        <div className="alarm-tabs">
          {['all', 'estimate', 'request', 'review'].map(type => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={selectedType === type ? "active" : ""}
            >{type === 'all' ? '전체' : type === 'estimate' ? '견적서' : type === 'request' ? '의뢰서' : '리뷰'}</button>
          ))}
        </div>
        <div>
          {loading ? (
            <div className='loading'><i className="fa-solid fa-circle-notch"></i></div>
          ) : alarms.length === 0 ? (
            <div style={{padding:"2rem 0"}}>알림이 없습니다.</div>
          ) : (
            alarms.map(alarm => (
              <div
                key={alarm.notification.idx}
                onClick={() => !alarm.isRead && handleRead(alarm.notification.idx)}
                className={`alarm-item${alarm.isRead ? "" : " unread"}`}
              >
                <div className="alarm-notice">
                  {alarm.notification.notice}
                </div>
                <div className="alarm-time">
                  {formatTime(alarm.notification.createdAt)}
                </div>
                {!alarm.isRead && (
                  <span className="unread-dot"></span>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </>
  );
}

export default Alarm;