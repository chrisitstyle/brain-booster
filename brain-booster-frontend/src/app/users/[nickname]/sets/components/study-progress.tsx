import { Check, X } from "lucide-react";

import { Progress } from "@/components/ui/progress";

interface StudyProgressProps {
  knownCount: number;
  unknownCount: number;
  totalCount: number;
}

export default function StudyProgress({
  knownCount,
  unknownCount,
  totalCount,
}: StudyProgressProps) {
  const reviewedCount = knownCount + unknownCount;

  const progress =
    totalCount > 0
      ? Math.min(100, Math.max(0, (reviewedCount / totalCount) * 100))
      : 0;

  return (
    <section className="mb-8" aria-labelledby="study-progress-title">
      <div className="mb-2 flex items-center justify-between gap-4 text-sm">
        <span id="study-progress-title" className="font-medium text-foreground">
          Progress
        </span>

        <span className="text-muted-foreground" aria-live="polite">
          {reviewedCount} / {totalCount} reviewed
        </span>
      </div>

      <Progress
        value={progress}
        className="h-2 bg-muted [&>div]:bg-pink-500"
        aria-label={`${reviewedCount} of ${totalCount} flashcards reviewed`}
      />

      <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-sm">
        <span className="flex items-center gap-1 font-medium text-green-600 dark:text-green-400">
          <Check className="h-4 w-4" aria-hidden="true" />
          {knownCount} known
        </span>

        <span className="flex items-center gap-1 font-medium text-red-500 dark:text-red-400">
          <X className="h-4 w-4" aria-hidden="true" />
          {unknownCount} still learning
        </span>
      </div>
    </section>
  );
}
