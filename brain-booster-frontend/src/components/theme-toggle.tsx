"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { useSyncExternalStore } from "react";

import { Button } from "@/components/ui/button";

const emptySubscribe = () => () => {};

/**
 * Allows the user to switch between light and dark themes.
 */
export function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();

  const mounted = useSyncExternalStore(
    emptySubscribe,
    () => true,
    () => false,
  );

  if (!mounted) {
    return (
      <Button
        type="button"
        variant="ghost"
        size="icon"
        disabled
        aria-label="Loading theme switcher"
      >
        <Sun className="size-5" />
      </Button>
    );
  }

  const isDarkTheme = resolvedTheme === "dark";

  function toggleTheme() {
    setTheme(isDarkTheme ? "light" : "dark");
  }

  return (
    <Button
      type="button"
      variant="ghost"
      size="icon"
      onClick={toggleTheme}
      aria-label={
        isDarkTheme ? "Switch to light theme" : "Switch to dark theme"
      }
      title={isDarkTheme ? "Switch to light theme" : "Switch to dark theme"}
    >
      {isDarkTheme ? (
        <Sun className="size-5" aria-hidden="true" />
      ) : (
        <Moon className="size-5" aria-hidden="true" />
      )}

      <span className="sr-only">
        {isDarkTheme ? "Switch to light theme" : "Switch to dark theme"}
      </span>
    </Button>
  );
}
