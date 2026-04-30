"use client";

import Link from "next/link";
import { useEffect } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { UserX } from "lucide-react";

export default function ProfileNotFound() {
  useEffect(() => {
    const timer = setTimeout(() => {
      toast("Profile not found", {
        icon: <UserX className="h-5 w-5 text-pink-500" />,
        className:
          "bg-white border-2 border-pink-200 text-gray-800 shadow-md font-medium",
      });
    }, 100);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex min-h-[70vh] flex-col items-center justify-center bg-gray-50 text-center px-4">
      <div className="mb-4 rounded-full bg-pink-100 p-6">
        <h2 className="text-6xl font-bold text-pink-500">404</h2>
      </div>
      <h3 className="mb-2 text-2xl font-semibold text-gray-800">
        Profile not found
      </h3>
      <p className="mb-8 text-gray-500 max-w-md">
        The link might be broken, or the user may have removed their account.
      </p>
      <Link href="/">
        <Button className="bg-pink-500 hover:bg-pink-600">
          Go to homepage
        </Button>
      </Link>
    </div>
  );
}
