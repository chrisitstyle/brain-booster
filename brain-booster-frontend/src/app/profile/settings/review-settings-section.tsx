"use client";

import { useEffect, useState, type ChangeEvent, type SubmitEvent } from "react";

import {
  BrainCircuit,
  Loader2,
  RefreshCw,
  RotateCcw,
  Save,
} from "lucide-react";

import { toast } from "sonner";

import {
  getReviewSettings,
  updateReviewSettings,
} from "@/api/spacedRepetitionService";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import type {
  UpdateUserReviewSettingsRequest,
  UserReviewSettings,
} from "@/types/spaced-repetition";

interface ReviewSettingsSectionProps {
  token: string;
}

interface ReviewSettingsFormValues {
  retentionPercentage: number;
  newCardsPerDay: string;
  reviewLimitPerDay: string;
}

type SettingsLoadingState = "loading" | "success" | "error";

const MIN_RETENTION_PERCENTAGE = 70;
const MAX_RETENTION_PERCENTAGE = 97;
const MAX_DAILY_LIMIT = 10000;

/**
 * Converts persisted settings into editable form values.
 */
function createFormValues(
  settings: UserReviewSettings,
): ReviewSettingsFormValues {
  return {
    retentionPercentage: settings.desiredRetention * 100,
    newCardsPerDay: String(settings.newCardsPerDay),
    reviewLimitPerDay: String(settings.reviewLimitPerDay),
  };
}

/**
 * Validates a non-negative daily limit within the supported range.
 */
function isValidDailyLimit(value: string): boolean {
  if (!/^\d+$/.test(value)) {
    return false;
  }

  const numericValue = Number(value);

  return (
    Number.isSafeInteger(numericValue) &&
    numericValue >= 0 &&
    numericValue <= MAX_DAILY_LIMIT
  );
}

/**
 * Detects the browser's IANA time zone.
 *
 * UTC is used when the browser does not provide a valid identifier.
 */
function detectUserTimezone(): string {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";
  } catch {
    return "UTC";
  }
}

/**
 * Converts an unknown request failure into a readable message.
 */
function getSettingsErrorMessage(
  error: unknown,
  fallbackMessage: string,
): string {
  return error instanceof Error ? error.message : fallbackMessage;
}

/**
 * Displays and updates the authenticated user's FSRS preferences.
 *
 * <p>The user controls desired retention and daily review limits.
 * The browser time zone is detected automatically and synchronized
 * when the settings are saved.
 *
 * <p>Only changed values are submitted to preserve PATCH semantics.
 */
export default function ReviewSettingsSection({
  token,
}: ReviewSettingsSectionProps) {
  const [savedSettings, setSavedSettings] = useState<UserReviewSettings | null>(
    null,
  );

  const [formValues, setFormValues] = useState<ReviewSettingsFormValues | null>(
    null,
  );

  const [loadingState, setLoadingState] =
    useState<SettingsLoadingState>("loading");

  const [loadingError, setLoadingError] = useState("");

  const [isSaving, setIsSaving] = useState(false);

  const [detectedTimezone, setDetectedTimezone] = useState<string | null>(null);

  const [reloadCounter, setReloadCounter] = useState(0);

  useEffect(() => {
    setDetectedTimezone(detectUserTimezone());
  }, []);

  useEffect(() => {
    const abortController = new AbortController();

    setLoadingState("loading");
    setLoadingError("");

    async function loadSettings() {
      try {
        const settings = await getReviewSettings(token, abortController.signal);

        if (abortController.signal.aborted) {
          return;
        }

        setSavedSettings(settings);
        setFormValues(createFormValues(settings));
        setLoadingState("success");
      } catch (error: unknown) {
        if (abortController.signal.aborted) {
          return;
        }

        const message = getSettingsErrorMessage(
          error,
          "Failed to load review settings.",
        );

        setLoadingError(message);
        setLoadingState("error");
      }
    }

    void loadSettings();

    return () => {
      abortController.abort();
    };
  }, [token, reloadCounter]);

  const isNewCardsLimitValid =
    formValues !== null && isValidDailyLimit(formValues.newCardsPerDay);

  const isReviewLimitValid =
    formValues !== null && isValidDailyLimit(formValues.reviewLimitPerDay);

  const isRetentionValid =
    formValues !== null &&
    Number.isFinite(formValues.retentionPercentage) &&
    formValues.retentionPercentage >= MIN_RETENTION_PERCENTAGE &&
    formValues.retentionPercentage <= MAX_RETENTION_PERCENTAGE;

  const isFormValid =
    isNewCardsLimitValid && isReviewLimitValid && isRetentionValid;

  const hasEditableChanges =
    savedSettings !== null &&
    formValues !== null &&
    (formValues.retentionPercentage !== savedSettings.desiredRetention * 100 ||
      Number(formValues.newCardsPerDay) !== savedSettings.newCardsPerDay ||
      Number(formValues.reviewLimitPerDay) !== savedSettings.reviewLimitPerDay);

  const timezoneNeedsSync =
    savedSettings !== null &&
    detectedTimezone !== null &&
    detectedTimezone !== savedSettings.timezone;

  const hasChanges = hasEditableChanges || timezoneNeedsSync;

  const canSave =
    loadingState === "success" && !isSaving && isFormValid && hasChanges;

  function handleRetentionChange(event: ChangeEvent<HTMLInputElement>) {
    const retentionPercentage = Number(event.target.value);

    setFormValues((previousValues) =>
      previousValues
        ? {
            ...previousValues,
            retentionPercentage,
          }
        : previousValues,
    );
  }

  function handleNewCardsLimitChange(event: ChangeEvent<HTMLInputElement>) {
    const newCardsPerDay = event.target.value;

    setFormValues((previousValues) =>
      previousValues
        ? {
            ...previousValues,
            newCardsPerDay,
          }
        : previousValues,
    );
  }

  function handleReviewLimitChange(event: ChangeEvent<HTMLInputElement>) {
    const reviewLimitPerDay = event.target.value;

    setFormValues((previousValues) =>
      previousValues
        ? {
            ...previousValues,
            reviewLimitPerDay,
          }
        : previousValues,
    );
  }

  function handleCancelChanges() {
    if (!savedSettings || isSaving) {
      return;
    }

    setFormValues(createFormValues(savedSettings));
  }

  function handleRetryLoading() {
    if (isSaving) {
      return;
    }

    setReloadCounter((previousCounter) => previousCounter + 1);
  }

  async function handleSaveSettings(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!savedSettings || !formValues || !canSave) {
      return;
    }

    const desiredRetention = Number(
      (formValues.retentionPercentage / 100).toFixed(4),
    );

    const newCardsPerDay = Number(formValues.newCardsPerDay);
    const reviewLimitPerDay = Number(formValues.reviewLimitPerDay);

    const settingsUpdate: UpdateUserReviewSettingsRequest = {};

    if (desiredRetention !== savedSettings.desiredRetention) {
      settingsUpdate.desiredRetention = desiredRetention;
    }

    if (newCardsPerDay !== savedSettings.newCardsPerDay) {
      settingsUpdate.newCardsPerDay = newCardsPerDay;
    }

    if (reviewLimitPerDay !== savedSettings.reviewLimitPerDay) {
      settingsUpdate.reviewLimitPerDay = reviewLimitPerDay;
    }

    if (detectedTimezone && detectedTimezone !== savedSettings.timezone) {
      settingsUpdate.timezone = detectedTimezone;
    }

    if (Object.keys(settingsUpdate).length === 0) {
      return;
    }

    setIsSaving(true);

    try {
      const updatedSettings = await updateReviewSettings(settingsUpdate, token);

      setSavedSettings(updatedSettings);
      setFormValues(createFormValues(updatedSettings));

      toast.success("Spaced repetition settings updated.");
    } catch (error: unknown) {
      toast.error(
        getSettingsErrorMessage(error, "Failed to update review settings."),
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="mt-6 overflow-hidden rounded-xl border border-border bg-card text-card-foreground shadow-sm">
      <div className="border-b border-border px-6 py-5">
        <div className="flex items-center gap-2">
          <BrainCircuit
            className="h-5 w-5 text-pink-500 dark:text-pink-400"
            aria-hidden="true"
          />

          <h2 className="text-lg font-semibold text-card-foreground">
            Spaced repetition
          </h2>
        </div>

        <p className="mt-1 text-sm text-muted-foreground">
          Customize your FSRS learning schedule and daily review limits.
        </p>
      </div>

      {loadingState === "loading" && (
        <div
          role="status"
          className="flex items-center justify-center gap-3 px-6 py-12 text-muted-foreground"
        >
          <Loader2
            className="h-5 w-5 animate-spin text-pink-500"
            aria-hidden="true"
          />

          <span>Loading review settings...</span>
        </div>
      )}

      {loadingState === "error" && (
        <div className="space-y-4 p-6">
          <p role="alert" className="text-sm text-destructive">
            {loadingError}
          </p>

          <Button type="button" variant="outline" onClick={handleRetryLoading}>
            <RefreshCw className="h-4 w-4" aria-hidden="true" />
            Try again
          </Button>
        </div>
      )}

      {loadingState === "success" && formValues && (
        <form onSubmit={handleSaveSettings} className="space-y-6 p-6">
          <div className="space-y-3">
            <div className="flex items-center justify-between gap-4">
              <Label htmlFor="fsrs-retention">Desired retention</Label>

              <span className="font-semibold text-pink-500 dark:text-pink-400">
                {Number(formValues.retentionPercentage.toFixed(1))}%
              </span>
            </div>

            <input
              id="fsrs-retention"
              name="desiredRetention"
              type="range"
              min={MIN_RETENTION_PERCENTAGE}
              max={MAX_RETENTION_PERCENTAGE}
              step="0.5"
              value={formValues.retentionPercentage}
              onChange={handleRetentionChange}
              disabled={isSaving}
              aria-describedby="fsrs-retention-description"
              className="h-2 w-full cursor-pointer accent-pink-500 disabled:cursor-not-allowed disabled:opacity-50"
            />

            <div className="flex justify-between text-xs text-muted-foreground">
              <span>70%</span>
              <span>97%</span>
            </div>

            <p
              id="fsrs-retention-description"
              className="text-sm text-muted-foreground"
            >
              Higher retention usually means more frequent reviews. The default
              is 90%.
            </p>
          </div>

          <div className="grid gap-6 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="fsrs-new-cards-limit">New cards per day</Label>

              <Input
                id="fsrs-new-cards-limit"
                name="newCardsPerDay"
                type="number"
                min={0}
                max={MAX_DAILY_LIMIT}
                step={1}
                required
                value={formValues.newCardsPerDay}
                onChange={handleNewCardsLimitChange}
                disabled={isSaving}
                aria-invalid={!isNewCardsLimitValid}
                aria-describedby="fsrs-new-cards-description"
                className="h-11 border-input bg-background text-foreground focus-visible:border-pink-400 focus-visible:ring-pink-500/20"
              />

              <p
                id="fsrs-new-cards-description"
                className="text-sm text-muted-foreground"
              >
                Maximum number of new flashcards introduced daily. Set to 0 to
                pause new cards.
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="fsrs-review-limit">Reviews per day</Label>

              <Input
                id="fsrs-review-limit"
                name="reviewLimitPerDay"
                type="number"
                min={0}
                max={MAX_DAILY_LIMIT}
                step={1}
                required
                value={formValues.reviewLimitPerDay}
                onChange={handleReviewLimitChange}
                disabled={isSaving}
                aria-invalid={!isReviewLimitValid}
                aria-describedby="fsrs-review-limit-description"
                className="h-11 border-input bg-background text-foreground focus-visible:border-pink-400 focus-visible:ring-pink-500/20"
              />

              <p
                id="fsrs-review-limit-description"
                className="text-sm text-muted-foreground"
              >
                Maximum number of existing-card reviews per day. Set to 0 to
                pause those reviews.
              </p>
            </div>
          </div>

          <div className="rounded-lg border border-border bg-muted/30 px-4 py-3">
            <p className="text-sm font-medium text-foreground">Time zone</p>

            <p className="mt-1 text-sm text-muted-foreground">
              {detectedTimezone
                ? `Automatically detected: ${detectedTimezone}`
                : "Detecting your time zone..."}
            </p>

            <p className="mt-1 text-xs text-muted-foreground">
              Used to calculate daily limits. Your detected time zone is
              synchronized when you save these settings.
            </p>
          </div>

          <div className="flex flex-col gap-3 border-t border-border pt-5 sm:flex-row sm:items-center sm:justify-end">
            {hasEditableChanges && (
              <Button
                type="button"
                variant="outline"
                onClick={handleCancelChanges}
                disabled={isSaving}
                className="border-border text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
              >
                <RotateCcw className="h-4 w-4" aria-hidden="true" />
                Cancel changes
              </Button>
            )}

            <Button
              type="submit"
              disabled={!canSave}
              className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
            >
              {isSaving ? (
                <>
                  <Loader2
                    className="h-4 w-4 animate-spin"
                    aria-hidden="true"
                  />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="h-4 w-4" aria-hidden="true" />
                  Save review settings
                </>
              )}
            </Button>
          </div>
        </form>
      )}
    </section>
  );
}
