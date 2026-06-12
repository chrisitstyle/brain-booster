"use client";

import { useEffect, useMemo, useState } from "react";

import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { parseJwt } from "@/utils/jwt";

export interface StatsFlashcardSet {
  setId: number;
  name?: string | null;
  title?: string | null;
  setName?: string | null;
}

interface UserFlashcardSetsState {
  data: StatsFlashcardSet[];
  dataKey: string | null;
  error: string | null;
  errorKey: string | null;
}

function getAuthenticatedUserId(token: string | null | undefined) {
  if (!token) {
    return null;
  }

  const decodedToken = parseJwt(token);

  if (!decodedToken) {
    return null;
  }

  const payload = decodedToken as Record<string, unknown>;
  const rawUserId = payload.userId ?? payload.id ?? payload.user_id;

  if (typeof rawUserId === "number") {
    return rawUserId;
  }

  if (typeof rawUserId === "string") {
    const parsedUserId = Number(rawUserId);

    return Number.isFinite(parsedUserId) ? parsedUserId : null;
  }

  return null;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error
    ? error.message
    : "Failed to fetch flashcard sets.";
}

export function getSetDisplayName(set: StatsFlashcardSet) {
  return set.name || set.title || set.setName || `Set #${set.setId}`;
}

export function useUserFlashcardSets(token: string | null | undefined) {
  const userId = useMemo(() => getAuthenticatedUserId(token), [token]);

  const queryKey =
    userId !== null && token ? `user-flashcard-sets:${userId}:${token}` : null;

  const [state, setState] = useState<UserFlashcardSetsState>({
    data: [],
    dataKey: null,
    error: null,
    errorKey: null,
  });

  const data = useMemo(() => {
    if (!queryKey || state.dataKey !== queryKey) {
      return [];
    }

    return state.data;
  }, [queryKey, state.dataKey, state.data]);

  const error = useMemo(() => {
    if (!queryKey || state.errorKey !== queryKey) {
      return null;
    }

    return state.error;
  }, [queryKey, state.errorKey, state.error]);

  const isLoading = Boolean(
    queryKey && state.dataKey !== queryKey && state.errorKey !== queryKey,
  );

  useEffect(() => {
    if (userId === null || !token || !queryKey) {
      return;
    }

    const currentUserId = userId;
    const authToken = token;
    let isCancelled = false;

    async function fetchFlashcardSets() {
      try {
        const result = (await getUserFlashcardSetsByUserId(
          currentUserId,
          authToken,
        )) as StatsFlashcardSet[];

        if (isCancelled) {
          return;
        }

        setState({
          data: result,
          dataKey: queryKey,
          error: null,
          errorKey: null,
        });
      } catch (fetchError) {
        if (isCancelled) {
          return;
        }

        setState((previousState) => ({
          ...previousState,
          error: getErrorMessage(fetchError),
          errorKey: queryKey,
        }));
      }
    }

    void fetchFlashcardSets();

    return () => {
      isCancelled = true;
    };
  }, [queryKey, token, userId]);

  return {
    data,
    isLoading,
    error,
  };
}
