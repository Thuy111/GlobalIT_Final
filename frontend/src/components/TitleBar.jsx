const TitleBar = ({title}) => {
  return (
    <div className="titleBar_container">
      <i class="fa-solid fa-chevron-left"></i>
      <h1>{title}</h1>
    </div>
  );
}

export default TitleBar;