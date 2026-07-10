import Link from "next/link";
import { FileQuestion } from "lucide-react";

import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="flex min-h-[calc(100svh-4rem)] items-center justify-center bg-background px-4 py-12 text-foreground">
      <div className="w-full max-w-md space-y-6 text-center">
        <div className="flex justify-center">
          <div className="rounded-full bg-pink-100 p-6 dark:bg-pink-950/50">
            <FileQuestion
              className="h-16 w-16 text-pink-500 dark:text-pink-400"
              aria-hidden="true"
            />
          </div>
        </div>

        <h1 className="text-4xl font-bold text-foreground">Page not found</h1>

        <p className="text-lg text-muted-foreground">
          Oops! It seems the page you&apos;re looking for doesn&apos;t exist or
          has been moved.
        </p>

        <div className="space-y-3">
          <Button
            asChild
            size="lg"
            className="rounded-full bg-pink-500 px-8 text-white hover:bg-pink-600"
          >
            <Link href="/">Return to home</Link>
          </Button>

          <p className="text-sm text-muted-foreground">Error code: 404</p>
        </div>

        <div className="mt-6 border-t border-border pt-6">
          <p className="text-muted-foreground">
            Looking for something specific? Try using the search bar in the
            navigation or return to the homepage to explore available study
            sets.
          </p>
        </div>
      </div>
    </main>
  );
}
