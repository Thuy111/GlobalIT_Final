import { createContext, useContext, useState, useEffect } from 'react';

const DarkModeContext = createContext(undefined);

export const DarkModeProvider = ({ children }) => {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const saved = localStorage.getItem('darkMode');
    return saved === null ? false : JSON.parse(saved);
  });

  useEffect(() => {
    const saved = localStorage.getItem('darkMode');
    if (saved === null) {
      localStorage.setItem('darkMode', JSON.stringify(false));
    }
    if(localStorage.getItem('darkMode') === 'true') {
      setIsDarkMode(true);
      document.body.classList.add('dark');
    } else {
      setIsDarkMode(false);
      document.body.classList.remove('dark');
    }
  }, [isDarkMode]);

  return (
    <DarkModeContext.Provider value={{ isDarkMode, setIsDarkMode }}>
      {children}
    </DarkModeContext.Provider>
  );
};

export const useDarkMode = () => useContext(DarkModeContext);