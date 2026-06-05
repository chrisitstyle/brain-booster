import Link from "next/link";
import { BookOpen, Plus } from "lucide-react";

import { Button } from "@/components/ui/button";

interface EmptySetsStateProps {
  title: string;
  description: string;
  showCreateButton?: boolean;
}

export default function EmptySetsState({
  title,
  description,
  showCreateButton = false,
}: EmptySetsStateProps) {
  return (
    <div className="rounded-lg border border-dashed border-gray-200 bg-white px-6 py-12 text-center">
      <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-pink-100">
        <BookOpen className="h-6 w-6 text-pink-500" />
      </div>

      <h2 className="text-lg font-semibold text-gray-800">{title}</h2>

      <p className="mt-2 text-sm text-gray-500">{description}</p>

      {showCreateButton && (
        <Button
          className="mt-5 bg-pink-500 text-white hover:bg-pink-600"
          asChild
        >
          <Link href="/create-set">
            <Plus className="mr-2 h-4 w-4" />
            Create set
          </Link>
        </Button>
      )}
    </div>
  );
}
