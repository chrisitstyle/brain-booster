import type { ReactNode } from "react";

interface GameQuestionCardProps {
  children: ReactNode;
  className?: string;
}

export default function GameQuestionCard({
  children,
  className = "",
}: GameQuestionCardProps) {
  return (
    <div
      className={`relative rounded-2xl border border-pink-200 bg-pink-50/60 p-5 text-foreground dark:border-pink-900 dark:bg-pink-950/20 ${className}`}
    >
      {children}
    </div>
  );
}
