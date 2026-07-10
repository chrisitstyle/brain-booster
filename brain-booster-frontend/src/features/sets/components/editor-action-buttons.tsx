import { ArrowLeftRight, Search, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { cn } from "@/lib/utils";

interface EditorActionButtonsProps {
  isSearchOpen: boolean;
  isLoading: boolean;
  isSubmitDisabled: boolean;
  submitLabel: string;
  loadingLabel: string;
  destructiveAriaLabel: string;
  showVisibilitySwitch?: boolean;
  isPublic?: boolean;
  onPublicChange?: (value: boolean) => void;
  onToggleSearch: () => void;
  onFlipTermsAndDefinitions: () => void;
  onDestructiveClick: () => void;
  onSubmit: () => void;
}

export function EditorActionButtons({
  isSearchOpen,
  isLoading,
  isSubmitDisabled,
  submitLabel,
  loadingLabel,
  destructiveAriaLabel,
  showVisibilitySwitch = false,
  isPublic = true,
  onPublicChange,
  onToggleSearch,
  onFlipTermsAndDefinitions,
  onDestructiveClick,
  onSubmit,
}: EditorActionButtonsProps) {
  return (
    <div className="flex items-center gap-1 sm:gap-2">
      {showVisibilitySwitch && (
        <div className="hidden items-center gap-2 lg:flex">
          <Label
            htmlFor="set-visibility"
            className="text-sm text-muted-foreground"
          >
            Visible to everyone
          </Label>

          <Switch
            id="set-visibility"
            checked={isPublic}
            onCheckedChange={onPublicChange}
            disabled={isLoading}
            className="data-[state=checked]:bg-pink-500 data-[state=unchecked]:bg-muted-foreground/30"
          />
        </div>
      )}

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label={isSearchOpen ? "Close card search" : "Search cards"}
        aria-pressed={isSearchOpen}
        disabled={isLoading}
        className={cn(
          "h-10 w-10 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 sm:h-11 sm:w-11",
          "dark:hover:bg-pink-950/40 dark:hover:text-pink-400",
          isSearchOpen &&
            "bg-pink-50 text-pink-500 dark:bg-pink-950/40 dark:text-pink-400",
        )}
        onClick={onToggleSearch}
      >
        <Search className="h-5 w-5" aria-hidden="true" />
      </Button>

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label="Flip terms and definitions"
        disabled={isLoading}
        className="h-10 w-10 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400 sm:h-11 sm:w-11"
        onClick={onFlipTermsAndDefinitions}
      >
        <ArrowLeftRight className="h-5 w-5" aria-hidden="true" />
      </Button>

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label={destructiveAriaLabel}
        disabled={isLoading}
        className="h-10 w-10 rounded-full text-muted-foreground hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-950/40 dark:hover:text-red-400 sm:h-11 sm:w-11"
        onClick={onDestructiveClick}
      >
        <Trash2 className="h-5 w-5" aria-hidden="true" />
      </Button>

      <Button
        type="button"
        onClick={onSubmit}
        disabled={isSubmitDisabled}
        className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
      >
        {isLoading ? loadingLabel : submitLabel}
      </Button>
    </div>
  );
}
