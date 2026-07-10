import { Search, SlidersHorizontal, X } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface BottomSearchBarProps {
  isOpen: boolean;
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  onClose: () => void;
}

export function BottomSearchBar({
  isOpen,
  searchQuery,
  onSearchQueryChange,
  onClose,
}: BottomSearchBarProps) {
  return (
    <div
      className={cn(
        "fixed inset-x-0 bottom-6 z-50 px-4 transition-all duration-300",
        isOpen
          ? "translate-y-0 opacity-100"
          : "pointer-events-none translate-y-8 opacity-0",
      )}
      aria-hidden={!isOpen}
    >
      <div
        className="mx-auto flex h-14 max-w-2xl items-center gap-3 rounded-full border-2 border-pink-300 bg-card px-4 text-card-foreground shadow-xl dark:border-pink-800"
        role="search"
      >
        <Search
          className="h-5 w-5 shrink-0 text-muted-foreground"
          aria-hidden="true"
        />

        <Input
          type="search"
          autoFocus={isOpen}
          placeholder="Search"
          value={searchQuery}
          onChange={(event) => onSearchQueryChange(event.target.value)}
          className="h-full flex-1 border-0 bg-transparent px-0 text-base text-foreground shadow-none placeholder:text-muted-foreground focus-visible:ring-0 focus-visible:ring-offset-0"
          aria-label="Search flashcards"
          tabIndex={isOpen ? 0 : -1}
        />

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Search filters"
          className="h-9 w-9 shrink-0 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
          onClick={() => toast.info("Search filters coming soon.")}
          tabIndex={isOpen ? 0 : -1}
        >
          <SlidersHorizontal className="h-5 w-5" aria-hidden="true" />
        </Button>

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Close search"
          className="h-9 w-9 shrink-0 rounded-full text-muted-foreground hover:bg-accent hover:text-foreground"
          onClick={onClose}
          tabIndex={isOpen ? 0 : -1}
        >
          <X className="h-5 w-5" aria-hidden="true" />
        </Button>
      </div>
    </div>
  );
}
