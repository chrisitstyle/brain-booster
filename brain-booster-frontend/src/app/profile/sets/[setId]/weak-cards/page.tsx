import { Suspense } from "react";

import WeakCardsClient from "./weak-cards-client";

export default function WeakCardsPage() {
  return (
    <Suspense
      fallback={
        <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
          <div className="mx-auto max-w-5xl px-4 py-10">
            <p className="rounded-xl border border-dashed border-gray-200 bg-white px-4 py-3 text-sm text-slate-500">
              Loading weak cards...
            </p>
          </div>
        </main>
      }
    >
      <WeakCardsClient />
    </Suspense>
  );
}
