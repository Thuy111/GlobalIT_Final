import { useEffect, useState } from "react";
import axios from "axios";

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
    fetchRequests(); // 초기 로딩 시 전체 조회
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchRequests(); // 검색 버튼 눌렀을 때
  };

  return (
    <div style={{ padding: "20px" }}>
      <form onSubmit={handleSearch} style={{ marginBottom: "20px" }}>
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="검색어를 입력하세요"
        />
        <button type="submit">검색</button>
      </form>

      {request.map((item) => (
        <div
          key={item.idx}
          style={{
            border: "1px solid #ccc",
            borderRadius: "10px",
            padding: "10px",
            marginBottom: "10px",
          }}
        >
          <h3>{item.title}</h3>
          <p>{item.createdAt}</p>
        </div>
      ))}
    </div>
  );
}

export default RequestList;
