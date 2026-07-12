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
      className={`mx-auto ${maxWidthClass} rounded-2xl border border-border bg-card p-6 text-center text-card-foreground shadow-sm`}
    >
      {message}
    </div>
  );
}
