

const PartnerProfile = () => {

    return(
        <div className="profile_container">
            <div className="profile_main_container">
                <h1>마이페이지</h1>
                <div className="profile_inform">
                    <div className="inform_img">
                        {/* 이미지 파일 작성 <img src={info.profileImage} alt="프로필" /> */}
                    </div>
                    <div className="inform_text">
                        {/* 사업체 닉네임, 로그인 타입 출력 */}
                    </div>
                </div>

                <div className="partner_infom">
                    <h2>업체명</h2>
                    <h2>사업자 번호</h2>
                </div>

                <section className="account_setting">
                    <h2>SMaSh 이용 내역</h2>
                    <ul>
                        <li>회사 소개</li>
                        <li>보낸 견적서</li>
                        <li>작성된 리뷰</li>
                    </ul>
                </section>

                <section className="account_setting">
                    <h2>계정 설정</h2>
                    <ul>
                        <li>개인 정보 수정</li>
                        <li>로그아웃</li>
                        <li>계정 탈퇴</li>
                    </ul>
                </section>
            </div>
        </div>
    );
}

export default PartnerProfile;