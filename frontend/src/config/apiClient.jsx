import axios from "axios";
import Cookies from 'js-cookie';
const baseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080"; // 환경변수에서 API URL을 가져오거나 기본값 설정

const apiClient = axios.create({
  baseURL: `${baseUrl}/smash`,
  withCredentials: true,
  headers: {
    "X-Frontend-Auth-Check": "true", // 백엔드에 프론트 요청인 것을 구분하기 위한 헤더
  },
});

// 모든 요청에 동적으로 CSRF 토큰을 넣어줌
apiClient.interceptors.request.use(config => {
  const xsrfToken = Cookies.get('XSRF-TOKEN');
  // console.log('XSRF-TOKEN:', xsrfToken); // 디버깅용 로그
  
  if (xsrfToken) {
    config.headers['X-XSRF-TOKEN'] = xsrfToken;
  }
  return config;
});

export { baseUrl };  // baseUrl을 export
export default apiClient;
