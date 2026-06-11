"use client";

import { useCallback, useEffect, useState } from "react";

export interface UseGameAnalyticsQueryResult<T> {
  data: T | null;
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

interface UseGameAnalyticsQueryOptions {
  enabled?: boolean;
  queryKey: string;
}

interface QueryState<T> {
  data: T | null;
  dataKey: string | null;
  error: string | null;
  errorKey: string | null;
  isRefetching: boolean;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error
    ? error.message
    : "Failed to fetch game analytics.";
}

export function useGameAnalyticsQuery<T>(
  queryFn: () => Promise<T>,
  options: UseGameAnalyticsQueryOptions,
): UseGameAnalyticsQueryResult<T> {
  const { enabled = true, queryKey } = options;

  const [state, setState] = useState<QueryState<T>>({
    data: null,
    dataKey: null,
    error: null,
    errorKey: null,
    isRefetching: false,
  });

  const data = enabled && state.dataKey === queryKey ? state.data : null;
  const error = enabled && state.errorKey === queryKey ? state.error : null;

  const isInitialLoading = enabled && data === null && error === null;
  const isLoading = isInitialLoading || state.isRefetching;

  useEffect(() => {
    if (!enabled) {
      return;
    }

    let isCancelled = false;

    async function fetchInitialData() {
      try {
        const result = await queryFn();

        if (isCancelled) {
          return;
        }

        setState({
          data: result,
          dataKey: queryKey,
          error: null,
          errorKey: null,
          isRefetching: false,
        });
      } catch (fetchError) {
        if (isCancelled) {
          return;
        }

        setState((previousState) => ({
          ...previousState,
          error: getErrorMessage(fetchError),
          errorKey: queryKey,
          isRefetching: false,
        }));
      }
    }

    void fetchInitialData();

    return () => {
      isCancelled = true;
    };
  }, [enabled, queryFn, queryKey]);

  const refetch = useCallback(async () => {
    if (!enabled) {
      return;
    }

    try {
      setState((previousState) => ({
        ...previousState,
        error: null,
        errorKey: null,
        isRefetching: true,
      }));

      const result = await queryFn();

      setState({
        data: result,
        dataKey: queryKey,
        error: null,
        errorKey: null,
        isRefetching: false,
      });
    } catch (fetchError) {
      setState((previousState) => ({
        ...previousState,
        error: getErrorMessage(fetchError),
        errorKey: queryKey,
        isRefetching: false,
      }));
    }
  }, [enabled, queryFn, queryKey]);

  return {
    data,
    isLoading,
    error,
    refetch,
  };
}
