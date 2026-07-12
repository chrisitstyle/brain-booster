import { Clock3 } from "lucide-react";

import { formatGameDuration } from "@/components/games/hooks/useGameElapsedSeconds";

interface GameTimerProps {
  seconds: number;
}

export default function GameTimer({ seconds }: GameTimerProps) {
  return (
    <div className="inline-flex items-center gap-1.5 rounded-full border border-border bg-card px-3 py-1 text-xs font-semibold text-muted-foreground shadow-sm">
      <Clock3
        className="h-3.5 w-3.5 text-pink-500 dark:text-pink-400"
        aria-hidden="true"
      />

      <span>{formatGameDuration(seconds)}</span>
    </div>
  );
}
