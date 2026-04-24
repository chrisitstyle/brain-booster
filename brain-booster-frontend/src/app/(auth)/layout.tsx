"use client";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col">
      <main className="flex-1 flex items-center justify-center p-4 py-12 bg-gray-50">
        {children}
      </main>
    </div>
  );
}
