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

  const encodedNickname = encodeURIComponent(nickname);

  const backToSetHref = `/users/${encodedNickname}/sets/${setId}`;

  useEffect(() => {
    if (!warnBeforeLeaving) {
      return;
    }

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
      if (!storageKeyToClearOnLeave) {
        return;
      }

      if (isRefreshingRef.current) {
        return;
      }

      window.sessionStorage.removeItem(storageKeyToClearOnLeave);
    };
  }, [storageKeyToClearOnLeave]);

  function clearGameProgress() {
    if (!storageKeyToClearOnLeave) {
      return;
    }

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
    <main className="min-h-[calc(100svh-4rem)] bg-background px-4 py-8 text-foreground sm:px-8">
      <div className="mx-auto max-w-4xl">
        <button
          type="button"
          className="mb-6 inline-flex items-center gap-2 rounded-sm text-sm font-medium text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
          onClick={handleBackToSetClick}
        >
          <ChevronLeft className="h-4 w-4" aria-hidden="true" />
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
