import Link from "next/link";
import { ChevronLeft } from "lucide-react";

interface StudySetHeaderProps {
  nickname: string;
  setName: string;
  description?: string | null;
}

export default function StudySetHeader({
  nickname,
  setName,
  description,
}: StudySetHeaderProps) {
  const profileHref = `/users/${encodeURIComponent(nickname)}/profile`;

  return (
    <header className="mb-6">
      <Link
        href={profileHref}
        className="mb-4 inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400 print:hidden"
      >
        <ChevronLeft className="h-4 w-4" aria-hidden="true" />
        Back to {nickname}&apos;s profile
      </Link>

      <div>
        <h1 className="break-words text-2xl font-bold text-foreground md:text-3xl print:text-black">
          {setName}
        </h1>

        {description && (
          <p className="mt-2 whitespace-pre-wrap break-words text-muted-foreground print:text-black">
            {description}
          </p>
        )}
      </div>
    </header>
  );
}
