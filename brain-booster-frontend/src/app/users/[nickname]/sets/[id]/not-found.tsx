"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BookX } from "lucide-react";

import { Button } from "@/components/ui/button";

export default function NotFound() {
  const pathname = usePathname();

  const nicknameMatch = pathname.match(/^\/users\/([^/]+)\/sets(?:\/|$)/);

  const encodedNickname = nicknameMatch?.[1];

  const backHref = encodedNickname ? `/users/${encodedNickname}/profile` : "/";

  return (
    <main className="flex min-h-[calc(100svh-4rem)] items-center justify-center bg-background px-4 text-foreground">
      <div className="container mx-auto py-16 text-center">
        <div className="mx-auto mb-6 flex h-24 w-24 items-center justify-center rounded-full bg-pink-100 dark:bg-pink-950/50">
          <BookX
            className="h-11 w-11 text-pink-500 dark:text-pink-400"
            aria-hidden="true"
          />
        </div>

        <h1 className="text-2xl font-bold text-foreground">
          Study set not found
        </h1>

        <p className="mx-auto mt-2 max-w-md text-muted-foreground">
          The study set you&apos;re looking for doesn&apos;t exist or an error
          occurred while loading it.
        </p>

        <Button
          asChild
          className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
        >
          <Link href={backHref}>
            {encodedNickname ? "Back to profile" : "Go to homepage"}
          </Link>
        </Button>
      </div>
    </main>
  );
}
