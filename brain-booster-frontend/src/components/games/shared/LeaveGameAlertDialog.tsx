"use client";

import { AlertTriangle } from "lucide-react";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface LeaveGameAlertDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirmLeave: () => void;
}

export default function LeaveGameAlertDialog({
  open,
  onOpenChange,
  onConfirmLeave,
}: LeaveGameAlertDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent className="w-[calc(100%-2rem)] max-w-lg rounded-2xl border border-border bg-card p-6 text-card-foreground shadow-2xl">
        <AlertDialogHeader>
          <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
            <AlertTriangle className="h-5 w-5" aria-hidden="true" />
          </div>

          <AlertDialogTitle className="text-xl font-semibold text-card-foreground">
            Leave this game?
          </AlertDialogTitle>

          <AlertDialogDescription className="text-sm leading-6 text-muted-foreground">
            Your current game progress will not be saved. You can come back to
            this set, but this attempt will be lost.
          </AlertDialogDescription>
        </AlertDialogHeader>

        <AlertDialogFooter className="mt-4 gap-2 sm:gap-2">
          <AlertDialogCancel className="mt-0 border-border bg-background text-foreground hover:bg-accent hover:text-accent-foreground focus-visible:ring-ring">
            Stay here
          </AlertDialogCancel>

          <AlertDialogAction
            onClick={onConfirmLeave}
            className="!bg-pink-500 !text-white hover:!bg-pink-600 focus-visible:!ring-pink-500/40"
          >
            Leave game
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
