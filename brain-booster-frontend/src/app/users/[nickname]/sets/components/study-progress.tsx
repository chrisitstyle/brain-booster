"use client";

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
  const progress = totalCount > 0 ? (reviewedCount / totalCount) * 100 : 0;

  return (
    <div className="mb-8">
      <div className="mb-2 flex items-center justify-between text-sm">
        <span className="text-gray-500">Progress</span>
        <span className="text-gray-500">
          {reviewedCount} / {totalCount} reviewed
        </span>
      </div>

      <Progress
        value={progress}
        className="h-2 bg-gray-200 [&>div]:bg-pink-500"
      />

      <div className="mt-2 flex gap-4 text-sm">
        <span className="flex items-center gap-1 text-green-600">
          <Check className="h-4 w-4" /> {knownCount} known
        </span>

        <span className="flex items-center gap-1 text-red-500">
          <X className="h-4 w-4" /> {unknownCount} still learning
        </span>
      </div>
    </div>
  );
}
