interface GameProgressProps {
  current: number;
  total: number;
  label?: string;
  suffix?: string;
}

function clampPercentage(value: number) {
  return Math.min(Math.max(value, 0), 100);
}

export default function GameProgress({
  current,
  total,
  label = "Progress",
  suffix,
}: GameProgressProps) {
  const progressPercentage =
    total > 0 ? clampPercentage((current / total) * 100) : 0;

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-4 text-sm text-muted-foreground">
        <span>{label}</span>

        <span>
          {current} / {total}
          {suffix ? ` ${suffix}` : ""}
        </span>
      </div>

      <div
        className="h-2 w-full overflow-hidden rounded-full bg-muted"
        role="progressbar"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(progressPercentage)}
      >
        <div
          className="h-full rounded-full bg-pink-500 transition-all duration-500"
          style={{
            width: `${progressPercentage}%`,
          }}
        />
      </div>
    </div>
  );
}
