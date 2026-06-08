import type { ReactNode } from "react";
import "./game-animations.css";

interface GameShellProps {
  children: ReactNode;
  maxWidth?: "md" | "lg" | "xl";
}

const maxWidthClasses = {
  md: "max-w-2xl",
  lg: "max-w-3xl",
  xl: "max-w-4xl",
};

export default function GameShell({
  children,
  maxWidth = "md",
}: GameShellProps) {
  return (
    <div
      className={`mx-auto ${maxWidthClasses[maxWidth]} space-y-6 rounded-2xl border border-pink-100 bg-white p-6 shadow-sm`}
    >
      {children}
    </div>
  );
}
