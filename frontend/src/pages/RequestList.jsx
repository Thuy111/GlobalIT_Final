import { useEffect, useState } from "react";
import apiClient from "../config/apiClient";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import "../styles/RequestList.css";

function RequestList() {
  const [allRequests, setAllRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [search, setSearch] = useState("");
  const [hashtags, setHashtags] = useState([]);
  const [selectedTag, setSelectedTag] = useState("");
  const [loading, setLoading] = useState(true);
  const [hideExpired, setHideExpired] = useState(false);

  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(true);
  const [isFetching, setIsFetching] = useState(false);

  const baseUrl = import.meta.env.VITE_API_URL;

  // 🔄 데이터 페이징으로 불러오기
  const fetchPage = async (pageToLoad) => {
    if (isFetching || !hasNext) return;

    setIsFetching(true);
    try {
      const res = await apiClient.get(`/request/main`, {
        params: {
          page: pageToLoad,
          size: 10,
          hideExpired,
        },
      });

      const newRequests = res.data.request ?? [];

      if (pageToLoad === 0) {
        setAllRequests(newRequests);
        setFilteredRequests(newRequests);
      } else {
        setAllRequests((prev) => [...prev, ...newRequests]);
        setFilteredRequests((prev) => [...prev, ...newRequests]);
      }

      const fetchedTags = res.data.hashtags ?? [];
      setHashtags(["전체", ...fetchedTags]);

      setHasNext(res.data.hasNext);
      setPage(pageToLoad);
    } catch (error) {
      console.error("요청 실패:", error);
    }
    setIsFetching(false);
    setLoading(false);
  };

  useEffect(() => {
    setPage(0);
    fetchPage(0); // 첫 페이지 초기화
  }, [hideExpired]);

  // 🔍 검색 필터
  const handleSearch = () => {
    const keyword = search.trim().toLowerCase();
    const filtered = allRequests.filter((item) => {
      const inTitle = item.title?.toLowerCase().includes(keyword);
      const inContent = item.content?.toLowerCase().includes(keyword);
      const inHashtag = item.hashtags?.toLowerCase().includes(keyword);
      const tagMatch =
        selectedTag && selectedTag !== "전체"
          ? item.hashtags?.includes(selectedTag)
          : true;
      return (inTitle || inContent || inHashtag) && tagMatch;
    });
    setFilteredRequests(filtered);
  };

  useEffect(() => {
    if (selectedTag && selectedTag !== "전체") {
      handleSearch();
    }
  }, [selectedTag]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    handleSearch();
  };

  const handleTagClick = (tag) => {
    if (tag === "전체") {
      setSelectedTag("");
      setSearch("");
      setFilteredRequests(allRequests);
    } else {
      const newTag = selectedTag === tag ? "" : tag;
      setSelectedTag(newTag);
    }
  };

  // 무한스크롤: 스크롤 이벤트
  useEffect(() => {
    const handleScroll = () => {
      if (
        window.innerHeight + window.scrollY >=
          document.documentElement.offsetHeight - 300 &&
        hasNext &&
        !isFetching
      ) {
        fetchPage(page + 1);
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [page, hasNext, isFetching]);

  // D-4 이하 필터링 (캐러셀용)
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
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
    arrows: false,
  };

  if (loading) return <div className="loading"><i className="fa-solid fa-circle-notch fa-spin"></i></div>;

  return (
    <div className="request-container">
      {/* 🔥 캐러셀 */}
      {ddayFilteredRequests.length > 0 && (
        <Slider {...sliderSettings} className="carousel-slider">
          {ddayFilteredRequests.map((req) => (
            <div
              key={req.idx}
              className="carousel-wrapper"
              onClick={() =>
                window.location.href = `${baseUrl}/smash/request/detail/${req.idx}`
              }
            >
              <div className="carousel-card" style={{ cursor: "pointer" }}>
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
      )}

      {/* 🔍 검색창 */}
      <form onSubmit={handleSearchSubmit} className="request-search-form">
        <div className="request-search-box">
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="무엇을 찾으시나요?"
          />
          <button type="submit" id="search">
            <i className="fa-solid fa-magnifying-glass"></i>
          </button>
        </div>
      </form>

      {/* 🏷 해시태그 필터 */}
      {hashtags && (
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
      )}

      {/* ✅ 종료 숨기기 토글 */}
      <div className="hide-expired-toggle" style={{ textAlign: "right", margin: "10px" }}>
        <label style={{ fontSize: "14px", cursor: "pointer" }}>
          <input
            type="checkbox"
            checked={hideExpired}
            onChange={(e) => setHideExpired(e.target.checked)}
            style={{ marginRight: "5px" }}
          />
          종료된 의뢰 숨기기
        </label>
      </div>

      {/* 📄 의뢰서 카드 리스트 */}
      {filteredRequests.map((item) => (
        <div
          key={item.idx}
          className="request-card"
          onClick={() =>
            window.location.href = `${baseUrl}/smash/request/detail/${item.idx}`
          }
          style={{ cursor: "pointer" }}
        >
          <div className="request-dday">{item.dday}</div>
          <div className="request-date">{item.createdAt?.split("T")[0]}</div>

          <div className="request-header">
            <h3 className="request-title">{item.title}</h3>
            <p
              className={`request-status ${
                item.isDone === 0
                  ? item.dday === "종료" ? "failed" : "pending"
                  : item.isDone === 1
                  ? "completed"
                  : "failed"
              }`}
            >
              {item.isDone === 0
                ? item.dday === "종료"
                  ? "미낙찰"
                  : "낙찰대기"
                : item.isDone === 1
                ? "낙찰완료"
                : "미낙찰"}
            </p>
          </div>

          <p className="request-content">{item.content}</p>

          <div className="request-tags">
            {item.hashtags &&
              item.hashtags.split(" ").map((tag, index) =>
                tag ? (
                  <span key={index} className="hashtag-badge">
                    {tag}
                  </span>
                ) : null
              )}
          </div>

          <div
            className="request-min-price"
            style={{ textAlign: "right", fontWeight: "bold", marginTop: "8px" }}
          >
            현재 최저가:{" "}
            {item.minEstimatePrice != null
              ? item.minEstimatePrice.toLocaleString() + "원"
              : "없음"}
          </div>
        </div>
      ))}
    </div>
  );
}

export default RequestList;
