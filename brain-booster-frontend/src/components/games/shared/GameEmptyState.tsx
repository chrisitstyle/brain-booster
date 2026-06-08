interface GameEmptyStateProps {
  message: string;
  maxWidth?: "md" | "xl";
}

export default function GameEmptyState({
  message,
  maxWidth = "md",
}: GameEmptyStateProps) {
  const maxWidthClass = maxWidth === "xl" ? "max-w-4xl" : "max-w-2xl";

  return (
    <div
      className={`mx-auto ${maxWidthClass} rounded-2xl border border-pink-100 bg-white p-6 text-center text-gray-700 shadow-sm`}
    >
      {message}
    </div>
  );
}
