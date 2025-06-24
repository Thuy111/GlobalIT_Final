import { useState, useEffect } from "react";
import axios from "axios";
import { useUser } from '../contexts/UserContext';

const Alarm = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const user = useUser();
  const [alarms, setAlarms] = useState([]);
  const [loading, setLoading] = useState(true);

  // 알람 목록 불러오기
  useEffect(() => {
    setLoading(true);
    axios.get(`${baseUrl}/smash/alarm/list`, {
      headers: {
        Accept: "application/json",
      },
      })
      .then(res => {
        console.log("알람 응답:", res.data);
        setAlarms(Array.isArray(res.data) ? res.data : []);
      })
      .catch((err) => {
        console.error("알람 에러:", err);
        setAlarms([]);
      })
      .finally(() => setLoading(false));
  }, [baseUrl]);

  // 알람 읽음 처리
  const handleRead = async (idx) => {
    try {
      await axios.post(`${baseUrl}/smash/alarm/read`, null, {
        params: { idx, isRead: true }
      });
      setAlarms(prev =>
        prev.map(a => a.idx === idx ? { ...a, isRead: true } : a)
      );
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
    <div className="alarm" style={{maxWidth:400,margin:"0 auto",padding:16}}>
      <div style={{fontWeight:"bold",fontSize:18,marginBottom:12}}>{"< 알림"}</div>
      <div>
        {loading ? (
          <div style={{padding:"2rem 0"}}>불러오는 중...</div>
        ) : alarms.length === 0 ? (
          <div style={{padding:"2rem 0"}}>알림이 없습니다.</div>
        ) : (
          alarms.map(alarm => (
            <div
              key={alarm.idx}
              className={`alarm-item${alarm.isRead ? "" : " unread"}`}
              onClick={() => !alarm.isRead && handleRead(alarm.idx)}
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
                {alarm.notice}
              </div>
              <div style={{ color: "#888", fontSize: 13 }}>
                {formatTime(alarm.createdAt)}
              </div>
              {!alarm.isRead && (
                <span
                  style={{
                    position: "absolute",
                    top: 18,
                    right: 18,
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
  );
}

export default Alarm;