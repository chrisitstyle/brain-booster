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
      className={`relative rounded-2xl border border-pink-100 bg-pink-50/40 p-5 ${className}`}
    >
      {children}
    </div>
  );
}
