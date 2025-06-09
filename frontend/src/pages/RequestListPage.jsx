function RequestListPage() {
  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">의뢰서 목록</h1>

      <div className="space-y-4">
        {/* 여기에 반복되는 카드 형태로 의뢰서가 들어올 예정 */}
        <div className="p-4 border rounded shadow">
          <h2 className="text-lg font-semibold">의뢰서 제목</h2>
          <p className="text-sm text-gray-500">작성일: 2025.06.05</p>
        </div>

        <div className="p-4 border rounded shadow">
          <h2 className="text-lg font-semibold">의뢰서 제목 2</h2>
          <p className="text-sm text-gray-500">작성일: 2025.06.04</p>
        </div>
      </div>
    </div>
  );
}

export default RequestListPage;
