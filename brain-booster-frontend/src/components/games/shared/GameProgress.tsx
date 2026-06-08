interface GameProgressProps {
  current: number;
  total: number;
  label?: string;
  suffix?: string;
}

export default function GameProgress({
  current,
  total,
  label = "Progress",
  suffix,
}: GameProgressProps) {
  const progressPercentage = total > 0 ? (current / total) * 100 : 0;

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-sm text-gray-500">
        <span>{label}</span>

        <span>
          {current} / {total}
          {suffix ? ` ${suffix}` : ""}
        </span>
      </div>

      <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className="h-full rounded-full bg-pink-500 transition-all duration-500"
          style={{ width: `${progressPercentage}%` }}
        />
      </div>
    </div>
  );
}
