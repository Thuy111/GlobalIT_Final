import Login from './member/LoginPage';
import Member from './profile/UserProfile';

const Profile = () => {
  const user = JSON.parse(localStorage.getItem('user'));

  return (
    <div className="profile">
      {!user ? <Login /> : <Member />}
      {/* <Member /> */}
    </div>
  );
}

export default Profile;