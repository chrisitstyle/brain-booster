import type { ReactNode } from "react";

import { Button } from "@/components/ui/button";

import GameProgress from "./GameProgress";

interface GameResultCardProps {
  title: string;
  scoreLabel: string;
  score: number;
  total: number;
  progressSuffix?: string;
  primaryActionLabel: string;
  onPrimaryAction: () => void;
  secondaryActionLabel?: string;
  onSecondaryAction?: () => void;
  children?: ReactNode;
}

export default function GameResultCard({
  title,
  scoreLabel,
  score,
  total,
  progressSuffix = "correct",
  primaryActionLabel,
  onPrimaryAction,
  secondaryActionLabel,
  onSecondaryAction,
  children,
}: GameResultCardProps) {
  return (
    <div className="mx-auto max-w-2xl rounded-2xl border border-border bg-card p-6 text-center text-card-foreground shadow-sm">
      <h1 className="text-2xl font-bold text-card-foreground">{title}</h1>

      <p className="mt-4 text-lg text-muted-foreground">
        {scoreLabel}:{" "}
        <span className="font-semibold text-pink-500 dark:text-pink-400">
          {score} / {total}
        </span>
      </p>

      {children}

      <div className="mt-6">
        <GameProgress current={score} total={total} suffix={progressSuffix} />
      </div>

      {secondaryActionLabel && onSecondaryAction ? (
        <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <Button
            type="button"
            variant="outline"
            className="w-full border-border bg-background text-pink-500 hover:border-pink-200 hover:bg-pink-50 hover:text-pink-600 dark:text-pink-400 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-300"
            onClick={onSecondaryAction}
          >
            {secondaryActionLabel}
          </Button>

          <Button
            type="button"
            className="w-full bg-pink-500 text-white hover:bg-pink-600"
            onClick={onPrimaryAction}
          >
            {primaryActionLabel}
          </Button>
        </div>
      ) : (
        <Button
          type="button"
          className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
          onClick={onPrimaryAction}
        >
          {primaryActionLabel}
        </Button>
      )}
    </div>
  );
}
