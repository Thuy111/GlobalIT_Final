import axios from "axios";
const baseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080"; // 환경변수에서 API URL을 가져오거나 기본값 설정

const apiClient = axios.create({
  baseURL: `${baseUrl}/smash`,
  withCredentials: true,
  headers: {
    "X-Frontend-Auth-Check": "true", // 백엔드에 프론트 요청인 것을 구분하기 위한 헤더
  },
});

export default apiClient;
