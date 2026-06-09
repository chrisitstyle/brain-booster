import { Clock3 } from "lucide-react";
import { formatGameDuration } from "@/components/games/hooks/useGameElapsedSeconds";

interface GameTimerProps {
  seconds: number;
}

export default function GameTimer({ seconds }: GameTimerProps) {
  return (
    <div className="inline-flex items-center gap-1.5 rounded-full border border-pink-100 bg-white px-3 py-1 text-xs font-semibold text-gray-500 shadow-sm">
      <Clock3 className="h-3.5 w-3.5 text-pink-500" />
      <span>{formatGameDuration(seconds)}</span>
    </div>
  );
}
