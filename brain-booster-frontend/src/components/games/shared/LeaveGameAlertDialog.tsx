"use client";

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
      <AlertDialogContent className="border-pink-100">
        <AlertDialogHeader>
          <AlertDialogTitle className="text-gray-800">
            Leave this game?
          </AlertDialogTitle>

          <AlertDialogDescription className="text-gray-500">
            Your current game progress will not be saved. You can come back to
            this set, but this attempt will be lost.
          </AlertDialogDescription>
        </AlertDialogHeader>

        <AlertDialogFooter>
          <AlertDialogCancel className="border-pink-200 text-pink-500 hover:bg-pink-50 hover:text-pink-600">
            Stay here
          </AlertDialogCancel>

          <AlertDialogAction
            className="bg-pink-500 text-white hover:bg-pink-600"
            onClick={onConfirmLeave}
          >
            Leave game
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
