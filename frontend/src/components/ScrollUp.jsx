import { useEffect, useState } from "react";

const ScrollUp = () => {
  const [visible, setVisible] = useState(false);

  const handleScroll = () => {
    setVisible(window.scrollY > 300);
  };

  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <div
      className="scroll_box"
      style={{ display: visible ? "flex" : "none" }}
    >
      <div className="up_scroll" onClick={scrollToTop}>
        <i class="fa-solid fa-angle-up"></i>
      </div>
    </div>
  );
};

export default ScrollUp;
