import { useState, useEffect } from "react";
import { useUser } from '../contexts/UserContext';
import { useUnreadAlarm } from '../contexts/UnreadAlarmContext';
import TitleBar from "../components/TitleBar";
import axios from "axios";

const Alarm = () => {
  axios.defaults.withCredentials = true;
  const baseUrl = import.meta.env.VITE_API_URL;
  const user = useUser();
  const [alarms, setAlarms] = useState([]);
  const [loading, setLoading] = useState(true);
  const { setUnreadCount } = useUnreadAlarm();

  // 알람 목록 불러오기
  const fetchAlarms = () => {
    setLoading(true);
    axios.get(`${baseUrl}/smash/alarm/list`, {
      headers: { Accept: "application/json" }
    }).then(res => {
        // NotificationMappingDTO 배열
        setAlarms(Array.isArray(res.data) ? res.data : []);
      })
      .catch(() => setAlarms([]))
      .finally(() => setLoading(false));
  };

  // 미읽음 알림 개수 fetch
  const fetchUnreadCount = () => {
  axios.get(`${baseUrl}/smash/alarm/unread`, { withCredentials: true })
    .then(res => setUnreadCount(res.data))
    .catch(() => setUnreadCount(0));
  };

  useEffect(() => {
    fetchAlarms();
    // eslint-disable-next-line
  }, [baseUrl]);

  // 알람 읽음 처리
  const handleRead = async (idx) => {
    try {
      await axios.post(`${baseUrl}/smash/alarm/read?idx=${idx}`, null);
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
      <div className="alarm" style={{maxWidth:400,margin:"0 auto",padding:16}}>
        <div>
          {loading ? (
            <div style={{padding:"2rem 0"}}>불러오는 중...</div>
          ) : alarms.length === 0 ? (
            <div style={{padding:"2rem 0"}}>알림이 없습니다.</div>
          ) : (
            alarms.map(alarm => (
              <div
                key={alarm.notification.idx}
                className={`alarm-item${alarm.isRead ? "" : " unread"}`}
                onClick={() => !alarm.isRead && handleRead(alarm.notification.idx)}
                style={{
                  background: alarm.isRead ? "#fff" : "#f6f6f6",
                  cursor: "pointer",
                  borderBottom: "1px solid #eee",
                  padding: "16px",
                  position: "relative",
                  marginBottom:8
                }}
              >
                <div style={{ fontWeight: "bold", marginBottom: 4, fontSize:15 }}>
                  {alarm.notification.notice}
                </div>
                <div style={{ color: "#888", fontSize: 13 }}>
                  {formatTime(alarm.notification.createdAt)}
                </div>
                {!alarm.isRead && (
                  <span
                    style={{
                      position: "absolute",
                      top: 4,
                      right: 4,
                      width: 10,
                      height: 10,
                      background: "#ff9900",
                      borderRadius: "50%",
                      display: "inline-block",
                    }}
                  ></span>
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