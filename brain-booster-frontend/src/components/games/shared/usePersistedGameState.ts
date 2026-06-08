"use client";

import { useEffect, useState } from "react";

export function usePersistedGameState<T>(
  storageKey: string,
  getInitialState: () => T,
) {
  const [state, setState] = useState<T>(() => {
    if (typeof window === "undefined") {
      return getInitialState();
    }

    const savedState = window.sessionStorage.getItem(storageKey);

    if (!savedState) {
      return getInitialState();
    }

    try {
      return JSON.parse(savedState) as T;
    } catch {
      window.sessionStorage.removeItem(storageKey);
      return getInitialState();
    }
  });

  useEffect(() => {
    window.sessionStorage.setItem(storageKey, JSON.stringify(state));
  }, [storageKey, state]);

  function clearPersistedState() {
    window.sessionStorage.removeItem(storageKey);
  }

  return [state, setState, clearPersistedState] as const;
}
