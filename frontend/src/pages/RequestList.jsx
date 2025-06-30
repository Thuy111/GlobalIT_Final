import { useEffect, useState } from "react";
import axios from "axios";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css"; 
import "slick-carousel/slick/slick-theme.css";
import '../styles/RequestList.css';

function RequestList() {
  const baseUrl = import.meta.env.VITE_API_URL;

  const [allRequests, setAllRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [search, setSearch] = useState("");
  const [hashtags, setHashtags] = useState([]);
  const [selectedTag, setSelectedTag] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(`${baseUrl}/smash/request/list`);
        setAllRequests(res.data.request ?? []);
        setFilteredRequests(res.data.request ?? []);
        const fetchedTags = res.data.hashtags ?? [];
        setHashtags(["전체", ...fetchedTags]);
      } catch (error) {
        console.error("요청 실패:", error);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (selectedTag && selectedTag !== "전체") {
      handleSearch();
    }
  }, [selectedTag]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    handleSearch();
  };

  const handleSearch = () => {
    const keyword = search.trim().toLowerCase();
    const filtered = allRequests.filter((item) => {
      const inTitle = item.title?.toLowerCase().includes(keyword);
      const inContent = item.content?.toLowerCase().includes(keyword);
      const inHashtag = item.hashtags?.toLowerCase().includes(keyword);
      const tagMatch = selectedTag && selectedTag !== "전체"
        ? item.hashtags?.includes(selectedTag)
        : true;
      return (inTitle || inContent || inHashtag) && tagMatch;
    });
    setFilteredRequests(filtered);
  };

  const handleTagClick = (tag) => {
    if (tag === "전체") {
      setSelectedTag("");
      setSearch(""); // ✅ 검색어 초기화
      setFilteredRequests(allRequests); // ✅ 전체 데이터 복원
    } else {
      const newTag = selectedTag === tag ? "" : tag;
      setSelectedTag(newTag);
    }
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
    arrows: false,
  };

  return (
    <div className="request-container">
      {ddayFilteredRequests.length > 0 ? (
        <Slider {...sliderSettings} className="carousel-slider">
          {ddayFilteredRequests.map((req) => (
            <div key={req.idx} className="carousel-wrapper">
              <div className="carousel-card">
                <div className="carousel-overlay" />
                <div className="carousel-badge">🔥 마감임박</div>
                <div className="carousel-icon">
                  <i className="fas fa-hourglass-half fa-beat"></i>
                </div>
                <img src="/images/main.jpg" alt="이미지" />
                <div className="carousel-text">
                  <h4>{req.title}</h4>
                  <p>{req.dday}</p>
                </div>
              </div>
            </div>
          ))}
        </Slider>
      ) : (
        <div className="hide-on-mobile" style={{ padding: "1rem", textAlign: "center", margin: '2rem auto' }}>
          <span className="dday-badge">D-4 이하</span> 의뢰서가 없습니다.
        </div>
      )}

      {/* 검색창 */}
      <form onSubmit={handleSearchSubmit} className="request-search-form">
        <div className="request-search-box">
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="무엇을 찾으시나요?"
          />
          {/* <button type="submit">검색</button> */}
          <button type="submit" id="search"><i className="fa-solid fa-magnifying-glass"></i></button>
        </div>
      </form>

      {/* 해시태그 */}
      {hashtags&&
        <div className="hashtag-badge-container">
          {hashtags.map((tag, index) => (
            <button
              key={index}
              className={`hashtag-badge ${
                selectedTag === tag || (tag === "전체" && selectedTag === "") ? "active" : ""
              }`}
              onClick={() => handleTagClick(tag)}
            >
              {tag}
            </button>
          ))}
        </div>
      }

      {/* 의뢰서 카드 */}
      {filteredRequests.map(item => (
        <div
          key={item.idx}
          className="request-card"
          onClick={() => window.location.href = `${baseUrl}/smash/request/detail/${item.idx}`}
          style={{ cursor: "pointer" }}
        >
          <div className="request-dday">{item.dday}</div>
          <div className="request-date">{item.createdAt?.split('T')[0]}</div>

          <div className="request-header">
            <h3 className="request-title">{item.title}</h3>
            <p className={`request-status ${
              item.isDone === 0 ? 'pending' :
              item.isDone === 1 ? 'completed' : 'failed'
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
