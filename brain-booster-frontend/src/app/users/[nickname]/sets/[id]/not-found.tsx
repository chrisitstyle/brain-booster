"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  const pathname = usePathname();

  // users/[nickname]/sets/[id]
  const segments = pathname.split("/");
  const nickname = segments[2];

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="container mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-800">
          Study set not found
        </h1>
        <p className="mt-2 text-gray-500">
          The study set you&apos;re looking for doesn&apos;t exist or an error
          occurred.
        </p>
        <Button
          asChild
          className="mt-6 bg-pink-500 hover:bg-pink-600 text-white"
        >
          <Link href={nickname ? `/users/${nickname}/profile` : "/"}>
            Back to profile
          </Link>
        </Button>
      </div>
    </div>
  );
}
