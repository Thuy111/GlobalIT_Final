const UserProfile = () => {

    return(
        <div className="member-page">
            <div className="member_main_container">
                <h1>마이페이지</h1>
                <div className="member_inform">
                    <div className="inform_img">
                        {/* 이미지 파일 작성 <img src={info.profileImage} alt="프로필" /> */}
                    </div>
                    <div className="inform_text">
                        {/* 유저 닉네임, 로그인 타입 출력 */}
                    </div>
                </div>

                <section className="account_setting">
                    <h2>SMaSh 이용 내역</h2>
                    <ul>
                        <li>작성 글</li>
                        <li>받은 견적서</li>
                        <li>작성한 리뷰</li>
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

export default UserProfile;