import React, { useEffect, useState } from "react";
import axios from "axios";

function RequestList() {
  const [requests, setRequests] = useState([]);

  useEffect(() => {
    axios.get("http://localhost:8080/smash/requests")
      .then((res) => {
        setRequests(res.data);
      })
      .catch((err) => {
        console.error("요청 실패:", err);
      });
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h2>의뢰서 목록</h2>
      {requests.map((item) => (
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
