"use client";
import FoldersPage from "./components/folders-page";

export default function Page() {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <main className="flex-1">
        <FoldersPage />
      </main>
    </div>
  );
}
