import type React from "react";
import { cn } from "@/lib/utils";

interface HomeButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "outline";
}

export function HomeButton({
  className,
  variant = "default",
  ...props
}: HomeButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center whitespace-nowrap rounded-full px-8 py-6 text-lg font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-pink-500 disabled:pointer-events-none disabled:opacity-50",
        variant === "default"
          ? "bg-pink-500 text-white hover:bg-pink-600"
          : "border border-pink-200 text-pink-500 hover:bg-pink-50",
        className,
      )}
      {...props}
    />
  );
}
