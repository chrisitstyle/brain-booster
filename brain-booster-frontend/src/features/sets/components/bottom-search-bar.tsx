import { Search, SlidersHorizontal, X } from "lucide-react";
import { toast } from "sonner";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

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
    >
      <div className="mx-auto flex h-14 max-w-2xl items-center gap-3 rounded-full border-2 border-pink-300 bg-white px-4 shadow-[0_10px_35px_rgba(0,0,0,0.15)]">
        <Search className="h-5 w-5 shrink-0 text-gray-500" />

        <Input
          autoFocus={isOpen}
          placeholder="Search"
          value={searchQuery}
          onChange={(event) => onSearchQueryChange(event.target.value)}
          className="h-full flex-1 border-0 bg-transparent px-0 text-base text-gray-800 placeholder:text-gray-400 focus-visible:ring-0 focus-visible:ring-offset-0"
        />

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Search filters"
          className="h-9 w-9 shrink-0 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500"
          onClick={() => toast.info("Search filters coming soon.")}
        >
          <SlidersHorizontal className="h-5 w-5" />
        </Button>

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Close search"
          className="h-9 w-9 shrink-0 rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-700"
          onClick={onClose}
        >
          <X className="h-5 w-5" />
        </Button>
      </div>
    </div>
  );
}
