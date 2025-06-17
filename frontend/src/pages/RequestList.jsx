  import { useEffect, useState } from "react";
  import axios from "axios";
  import '../styles/RequestList.css';

  function RequestList() {
    const baseUrl = import.meta.env.VITE_API_URL;
    const [request, setRequest] = useState([]);
    const [search, setSearch] = useState("");
    const [hashtags, setHashtags] = useState([]);
    const [selectedTag, setSelectedTag] = useState(""); // ✅ 선택된 해시태그 상태

    // 의뢰서 리스트 + 해시태그 목록 가져오기
    const fetchRequests = () => {
      const params = {};
      if (search) params.search = search;
      if (selectedTag) params.search = selectedTag; // 해시태그 클릭 시 우선 적용됨

      axios
        .get(`${baseUrl}/smash/request/list`, { params })
        .then((res) => {
          setRequest(res.data.request);
          setHashtags(res.data.hashtags);
        })
        .catch((err) => {
          console.error("요청 실패:", err);
        });
    };

    // 초기 및 검색/필터링 시 호출
    useEffect(() => {
      fetchRequests();
    }, [search, selectedTag]);

    const handleSearchSubmit = (e) => {
      e.preventDefault();
      fetchRequests();
    };

    // ✅ 해시태그 버튼 클릭
    const handleTagClick = (tag) => {
      if (selectedTag === tag) {
        setSelectedTag(""); // 같은 태그 누르면 해제
      } else {
        setSelectedTag(tag);
      }
    };

    return (
      <div className="request-container">

        {/* 검색창 */}
        <form onSubmit={handleSearchSubmit} className="request-search-form">
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setSelectedTag(""); // ✅ 텍스트 검색 시 해시태그 초기화
            }}
            placeholder="무엇을 찾으시나요?"
          />
        </form>

        {/* 해시태그 필터 */}
        <div className="hashtag-badge-container">
          {hashtags &&hashtags.map((tag, index) => (
            <button
              key={index}
              className={`hashtag-badge ${selectedTag === tag ? "active" : ""}`}
              onClick={() => handleTagClick(tag)}
            >
              {tag}
            </button>
          ))}
        </div>

        {/* 카드 리스트 */}
        {request.map(item => (
          <div key={item.idx} className="request-card">
            <div className="request-dday">{item.dDay}</div>
            <div className="request-date">{item.createdAt}</div>

            <div className="request-header">
              <h3 className="request-title">{item.title}</h3>
              <p className={`request-status ${
                item.isDone === 0 ? 'pending' :
                item.isDone === 1 ? 'failed' :
                item.isDone === 2 ? 'completed' : ''
              }`}>
                {item.isDone === 0 ? "낙찰대기" :
                item.isDone === 1 ? "미낙찰" :
                item.isDone === 2 ? "낙찰완료" : "알 수 없음"}
              </p>
            </div>

            <p className="request-content">{item.content}</p>
            <div className="request-tags">{item.hashtags.join(" ")}</div>
          </div>
        ))}
      </div>
    );
  }

  export default RequestList;
