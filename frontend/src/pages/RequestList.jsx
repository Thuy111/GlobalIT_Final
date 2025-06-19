import { useEffect, useState } from "react";
import axios from "axios";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css"; 
import "slick-carousel/slick/slick-theme.css";
import '../styles/RequestList.css';

function RequestList() {
  const baseUrl = import.meta.env.VITE_API_URL;

  const [allRequests, setAllRequests] = useState([]);       // 캐러셀 전용
  const [filteredRequests, setFilteredRequests] = useState([]); // 검색/필터 전용
  const [search, setSearch] = useState("");
  const [hashtags, setHashtags] = useState([]);
  const [selectedTag, setSelectedTag] = useState("");

  useEffect(() => {
    axios
      .get(`${baseUrl}/smash/request/list`)
      .then((res) => {
        setAllRequests(res.data.request);
        setFilteredRequests(res.data.request); // 초기값은 전체
        setHashtags(res.data.hashtags);
      })
      .catch((err) => {
        console.error("요청 실패:", err);
      });
  }, []);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      const keyword = search.trim().toLowerCase();

      const filtered = allRequests.filter((item) => {
        const inTitle = item.title?.toLowerCase().includes(keyword);
        const inContent = item.content?.toLowerCase().includes(keyword);
        const inHashtag = item.hashtags?.toLowerCase().includes(keyword);
        const tagMatch = selectedTag ? item.hashtags?.includes(selectedTag) : true;

        return (inTitle || inContent || inHashtag) && tagMatch;
      });

      setFilteredRequests(filtered);
    }, 300);

    return () => clearTimeout(delayDebounce);
  }, [search, selectedTag, allRequests]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
  };

  const handleTagClick = (tag) => {
    setSelectedTag(selectedTag === tag ? "" : tag);
  };

  const ddayFilteredRequests = allRequests.filter((req) => {
    const ddayStr = req.dday;
    if (!ddayStr || !ddayStr.startsWith("D-")) return false;
    const num = parseInt(ddayStr.replace("D-", ""));
    return !isNaN(num) && num <= 4;
  });

  const sliderSettings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 2,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
  };

  return (
    <div className="request-container">
      {/* ✅ 캐러셀은 항상 전체 데이터를 기준으로 필터링 */}
      {ddayFilteredRequests.length > 0 ? (
        <Slider {...sliderSettings} className="carousel-slider">
          {ddayFilteredRequests.map((req) => (
            <div key={req.idx} className="carousel-wrapper">
              <div className="carousel-card">
                <img src="/images/finger.png" alt="이미지 아직대기" />
                <h4>{req.title}</h4>
                <p>{req.dday}</p>
              </div>
            </div>
          ))}
        </Slider>
      ) : (
        <div style={{ padding: "1rem", textAlign: "center" }}>
          D-2 이하 의뢰서가 없습니다.
        </div>
      )}

      {/* 검색창 */}
      <form onSubmit={handleSearchSubmit} className="request-search-form">
        <input
          type="text"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setSelectedTag(""); // 검색 시 태그 초기화
          }}
          placeholder="무엇을 찾으시나요?"
        />
      </form>

      {/* 해시태그 필터 */}
      <div className="hashtag-badge-container">
        {hashtags && hashtags.map((tag, index) => (
          <button
            key={index}
            className={`hashtag-badge ${selectedTag === tag ? "active" : ""}`}
            onClick={() => handleTagClick(tag)}
          >
            {tag}
          </button>
        ))}
      </div>

      {/* 필터링된 카드 리스트 */}
      {filteredRequests.map(item => (
        <div key={item.idx} className="request-card">
          <div className="request-dday">{item.dday}</div>
          <div className="request-date">{item.createdAt?.split('T')[0]}</div>

          <div className="request-header">
            <h3 className="request-title">{item.title}</h3>
            <p className={`request-status ${
              item.isDone === 0 ? 'pending' :
              item.isDone === 1 ? 'completed' :
              'failed'
            }`}>
              {item.isDone === 0 ? "낙찰대기" :
               item.isDone === 1 ? "낙찰완료" : "미낙찰"}
            </p>
          </div>

          <p className="request-content">{item.content}</p>
          <div className="request-tags">
            {item.hashtags?.split(" ").map((tag, index) => (
              <span key={index} className="hashtag-badge">{tag}</span>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

export default RequestList;
