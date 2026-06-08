"use client";

import type { ReactNode } from "react";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { ChevronLeft } from "lucide-react";

import LeaveGameAlertDialog from "./LeaveGameAlertDialog";

interface GamePageLayoutProps {
  nickname: string;
  setId: number | string;
  children: ReactNode;
  warnBeforeLeaving?: boolean;
  storageKeyToClearOnLeave?: string;
}

export default function GamePageLayout({
  nickname,
  setId,
  children,
  warnBeforeLeaving = true,
  storageKeyToClearOnLeave,
}: GamePageLayoutProps) {
  const router = useRouter();
  const [isLeaveDialogOpen, setIsLeaveDialogOpen] = useState(false);

  const isRefreshingRef = useRef(false);

  const backToSetHref = `/users/${nickname}/sets/${setId}`;

  useEffect(() => {
    if (!warnBeforeLeaving) return;

    function handleBeforeUnload(event: BeforeUnloadEvent) {
      isRefreshingRef.current = true;

      event.preventDefault();
      event.returnValue = "";
    }

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [warnBeforeLeaving]);

  useEffect(() => {
    return () => {
      if (!storageKeyToClearOnLeave) return;

      if (isRefreshingRef.current) return;

      window.sessionStorage.removeItem(storageKeyToClearOnLeave);
    };
  }, [storageKeyToClearOnLeave]);

  function clearGameProgress() {
    if (!storageKeyToClearOnLeave) return;

    window.sessionStorage.removeItem(storageKeyToClearOnLeave);
  }

  function handleBackToSetClick() {
    if (!warnBeforeLeaving) {
      clearGameProgress();
      router.push(backToSetHref);
      return;
    }

    setIsLeaveDialogOpen(true);
  }

  function handleConfirmLeave() {
    clearGameProgress();
    setIsLeaveDialogOpen(false);
    router.push(backToSetHref);
  }

  return (
    <main className="min-h-screen bg-gray-50 p-8">
      <div className="mx-auto max-w-4xl">
        <button
          type="button"
          className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-gray-500 transition hover:text-pink-500"
          onClick={handleBackToSetClick}
        >
          <ChevronLeft className="h-4 w-4" />
          Back to set
        </button>

        {children}
      </div>

      <LeaveGameAlertDialog
        open={isLeaveDialogOpen}
        onOpenChange={setIsLeaveDialogOpen}
        onConfirmLeave={handleConfirmLeave}
      />
    </main>
  );
}
