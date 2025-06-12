import { useEffect, useState } from "react";
import axios from "axios";
import '../styles/RequestList.css';


function RequestList() {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [request, setRequest] = useState([]);
  const [search, setSearch] = useState("");

  const fetchRequests = () => {
    axios
      .get(`${baseUrl}/smash/request/list`, {
        params: { search }
      })
      .then((res) => {
        setRequest(res.data.request);
      })
      .catch((err) => {
        console.error("요청 실패:", err);
      });
  };

  useEffect(() => {
    fetchRequests(); // 초기 로딩
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchRequests();
  };

  return (
    <div className="request-container">
      <form onSubmit={handleSearch} className="request-search-form">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="무엇을 찾으시나요?"
        />
      </form>
  {request.map(item => (
    <div key={item.idx} className="request-card">
      <div className="request-dday">{item.dDay}</div>
      <div className="request-date">{item.createdAt}</div>

      <div className="request-header">
        <h3 className="request-title">{item.title}</h3>
      <p className={`request-status ${item.isDone === 0 ? 'pending' : item.isDone === 1 ? 'failed' : item.isDone === 2 ? 'completed' : ''}`}>
        {item.isDone === 0 ? "낙찰대기" : item.isDone === 1 ? "미낙찰" : item.isDone === 2 ? "낙찰완료" : "알 수 없음"}
      </p>
      </div>

      <p className="request-content">{item.content}</p>
      <div>{item.hashtags.join(" ")}</div>
    </div>
  ))}
</div>
  );
}
export default RequestList;
