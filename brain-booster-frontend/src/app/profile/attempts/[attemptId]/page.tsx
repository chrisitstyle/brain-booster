import { Suspense } from "react";

import AttemptDetailsClient from "./attempt-details-client";

export default function AttemptDetailsPage() {
  return (
    <Suspense
      fallback={
        <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
          <div className="mx-auto max-w-6xl px-4 py-10">
            <p className="rounded-xl border border-dashed border-gray-200 bg-gray-50 px-4 py-3 text-sm text-slate-500">
              Loading attempt details...
            </p>
          </div>
        </main>
      }
    >
      <AttemptDetailsClient />
    </Suspense>
  );
}
