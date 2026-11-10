import type { Metadata } from "next";

import ReviewDashboardClient from "./review-dashboard-client";

export const metadata: Metadata = {
  title: "Spaced Repetition Stats | Brain Booster",
  description:
    "Track your flashcard review activity, FSRS learning stages, and upcoming reviews.",
};

/**
 * Displays the current user's spaced repetition dashboard.
 */
export default function ReviewDashboardPage() {
  return <ReviewDashboardClient />;
}
