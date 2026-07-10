"use client";

import { useEffect } from "react";
import Link from "next/link";
import { UserX } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";

export default function ProfileNotFound() {
  useEffect(() => {
    const timer = window.setTimeout(() => {
      toast.error("Profile not found", {
        icon: (
          <UserX
            className="h-5 w-5 text-pink-500 dark:text-pink-400"
            aria-hidden="true"
          />
        ),
      });
    }, 100);

    return () => {
      window.clearTimeout(timer);
    };
  }, []);

  return (
    <main className="flex min-h-[calc(100svh-4rem)] flex-col items-center justify-center bg-background px-4 text-center text-foreground">
      <div className="mb-6 flex h-32 w-32 items-center justify-center rounded-full bg-pink-100 dark:bg-pink-950/50">
        <span className="text-5xl font-bold text-pink-500 dark:text-pink-400">
          404
        </span>
      </div>

      <h1 className="mb-2 text-2xl font-semibold text-foreground">
        Profile not found
      </h1>

      <p className="mb-8 max-w-md text-muted-foreground">
        The link might be broken, or the user may have removed their account.
      </p>

      <Button asChild className="bg-pink-500 text-white hover:bg-pink-600">
        <Link href="/">Go to homepage</Link>
      </Button>
    </main>
  );
}
