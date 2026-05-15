import { ArrowLeftRight, Search, Trash2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";

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
    <div className="flex items-center gap-2">
      {showVisibilitySwitch && (
        <div className="hidden items-center gap-2 lg:flex">
          <Label htmlFor="visibility" className="text-sm text-gray-600">
            Visible to everyone
          </Label>

          <Switch
            id="visibility"
            checked={isPublic}
            onCheckedChange={onPublicChange}
            className="data-[state=checked]:bg-pink-500"
          />
        </div>
      )}

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label="Search cards"
        className={cn(
          "h-11 w-11 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500",
          isSearchOpen && "bg-pink-50 text-pink-500",
        )}
        onClick={onToggleSearch}
      >
        <Search className="h-5 w-5" />
      </Button>

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label="Flip terms and definitions"
        className="h-11 w-11 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500"
        onClick={onFlipTermsAndDefinitions}
      >
        <ArrowLeftRight className="h-5 w-5" />
      </Button>

      <Button
        type="button"
        variant="ghost"
        size="icon"
        aria-label={destructiveAriaLabel}
        className="h-11 w-11 rounded-full text-gray-500 hover:bg-red-50 hover:text-red-500"
        onClick={onDestructiveClick}
      >
        <Trash2 className="h-5 w-5" />
      </Button>

      <Button
        onClick={onSubmit}
        disabled={isSubmitDisabled}
        className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-300"
      >
        {isLoading ? loadingLabel : submitLabel}
      </Button>
    </div>
  );
}
