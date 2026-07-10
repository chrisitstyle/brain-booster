"use client";

import type { ComponentProps } from "react";
import { useSyncExternalStore } from "react";
import { useTheme } from "next-themes";
import { Toaster as SonnerToaster } from "sonner";

type AppToasterProps = ComponentProps<typeof SonnerToaster>;

const emptySubscribe = () => () => {};

/**
 * Displays application notifications using the currently selected theme.
 */
export function AppToaster(props: AppToasterProps) {
  const { resolvedTheme } = useTheme();

  const mounted = useSyncExternalStore(
    emptySubscribe,
    () => true,
    () => false,
  );

  if (!mounted) {
    return null;
  }

  return (
    <SonnerToaster
      theme={resolvedTheme === "dark" ? "dark" : "light"}
      position="top-right"
      richColors
      closeButton
      {...props}
    />
  );
}
