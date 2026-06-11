import { Suspense } from "react";

import ProfileStatsClient from "./profile-stats-client";

export default function ProfileStatsPage() {
  return (
    <Suspense
      fallback={
        <main className="mx-auto max-w-6xl px-4 py-8">
          <p className="text-muted-foreground">Loading stats...</p>
        </main>
      }
    >
      <ProfileStatsClient />
    </Suspense>
  );
}
