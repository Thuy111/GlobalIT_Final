import { useEffect, useRef } from 'react';

const EditableField = ({ label, value, onChange, isEditing, isTextarea = false }) => {
  const inputRef = useRef();

  useEffect(() => {
    if (isEditing && inputRef.current) {
      inputRef.current.focus();
    }
  }, [isEditing]);

  return (
    <p>
      <span className="badge">{label}</span>
      {isEditing ? (
        isTextarea ? (
          <textarea
            ref={inputRef}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            rows={2}
            style={{ width: "100%" }}
          />
        ) : (
          <input
            ref={inputRef}
            type="text"
            value={value}
            onChange={(e) => onChange(e.target.value)}
          />
        )
      ) : (
        <span>{value}</span>
      )}
    </p>
  );
};

export default EditableField;
