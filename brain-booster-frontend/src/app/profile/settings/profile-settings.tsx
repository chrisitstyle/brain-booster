"use client";

import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
  type SubmitEvent,
} from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  AlertCircle,
  ChevronLeft,
  Loader2,
  Mail,
  RefreshCw,
  User,
} from "lucide-react";
import { toast } from "sonner";

import {
  getCurrentUser,
  updateEmail,
  updateNickname,
  type UserDTO,
} from "@/api/profileService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";
import { notifyProfileUpdated } from "@/utils/profile-events";

type ProfileLoadingState = "loading" | "success" | "error";

export function ProfileSettings() {
  const router = useRouter();

  const { token, isAuthenticated, isAuthLoading, replaceToken } = useAuth();

  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);

  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");

  const [profileLoadingState, setProfileLoadingState] =
    useState<ProfileLoadingState>("loading");

  const [isSaving, setIsSaving] = useState(false);
  const [profileError, setProfileError] = useState("");

  useEffect(() => {
    if (!isAuthLoading && !isAuthenticated) {
      router.replace("/login");
    }
  }, [isAuthLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    void getCurrentUser(requestToken)
      .then((user) => {
        if (isCancelled) {
          return;
        }

        setCurrentUser(user);
        setNickname(user.nickname);
        setEmail(user.email);
        setProfileError("");
        setProfileLoadingState("success");
      })
      .catch((error: unknown) => {
        if (isCancelled) {
          return;
        }

        const message =
          error instanceof Error
            ? error.message
            : "Failed to load user profile";

        setCurrentUser(null);
        setProfileError(message);
        setProfileLoadingState("error");

        toast.error(message);
      });

    return () => {
      isCancelled = true;
    };
  }, [token]);

  const avatarFallback = useMemo(() => {
    const firstLetter = currentUser?.nickname.trim().charAt(0);

    return firstLetter ? firstLetter.toUpperCase() : "?";
  }, [currentUser]);

  const normalizedNickname = nickname.trim();
  const normalizedEmail = email.trim().toLowerCase();

  const nicknameHasChanged =
    currentUser !== null && normalizedNickname !== currentUser.nickname;

  const emailHasChanged =
    currentUser !== null && normalizedEmail !== currentUser.email.toLowerCase();

  const fieldsAreValid =
    normalizedNickname.length > 0 && normalizedEmail.length > 0;

  const hasChanges = fieldsAreValid && (nicknameHasChanged || emailHasChanged);

  function handleNicknameChange(event: ChangeEvent<HTMLInputElement>) {
    setNickname(event.target.value);
  }

  function handleEmailChange(event: ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  function handleCancelChanges() {
    if (!currentUser) {
      return;
    }

    setNickname(currentUser.nickname);
    setEmail(currentUser.email);
  }

  async function handleRetry() {
    if (!token) {
      return;
    }

    try {
      setProfileLoadingState("loading");
      setProfileError("");

      const user = await getCurrentUser(token);

      setCurrentUser(user);
      setNickname(user.nickname);
      setEmail(user.email);
      setProfileError("");
      setProfileLoadingState("success");
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Failed to load user profile";

      setCurrentUser(null);
      setProfileError(message);
      setProfileLoadingState("error");

      toast.error(message);
    }
  }

  async function handleSave(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!token || !currentUser || !hasChanges) {
      return;
    }

    setIsSaving(true);

    let updatedUser = currentUser;
    let nicknameWasUpdated = false;

    try {
      if (nicknameHasChanged) {
        updatedUser = await updateNickname(normalizedNickname, token);

        nicknameWasUpdated = true;

        setCurrentUser(updatedUser);
        setNickname(updatedUser.nickname);

        notifyProfileUpdated();
      }

      if (emailHasChanged) {
        const response = await updateEmail(normalizedEmail, token);

        const userWithUpdatedEmail: UserDTO = {
          ...updatedUser,
          email: response.email,
        };

        setCurrentUser(userWithUpdatedEmail);
        setEmail(response.email);

        replaceToken(response.token);
        notifyProfileUpdated();
      }

      const message =
        nicknameHasChanged && emailHasChanged
          ? "Nickname and email have been updated."
          : nicknameHasChanged
            ? "Nickname has been updated."
            : "Email has been updated.";

      toast.success(message);
    } catch (error: unknown) {
      const message =
        error instanceof Error
          ? error.message
          : "Failed to update profile settings";

      if (nicknameWasUpdated && emailHasChanged) {
        toast.error(
          `Nickname was updated, but email could not be updated: ${message}`,
        );
      } else {
        toast.error(message);
      }
    } finally {
      setIsSaving(false);
    }
  }

  if (isAuthLoading || !isAuthenticated || profileLoadingState === "loading") {
    return (
      <main className="container mx-auto flex min-h-[calc(100svh-4rem)] items-center justify-center px-4 py-10">
        <div
          className="flex items-center gap-3 text-muted-foreground"
          role="status"
        >
          <Loader2
            className="h-5 w-5 animate-spin text-pink-500 dark:text-pink-400"
            aria-hidden="true"
          />

          <p>Loading settings...</p>
        </div>
      </main>
    );
  }

  if (profileLoadingState === "error" || !currentUser) {
    return (
      <main className="container mx-auto flex min-h-[calc(100svh-4rem)] max-w-3xl items-center justify-center px-4 py-10">
        <section className="w-full rounded-xl border border-red-200 bg-card p-8 text-center text-card-foreground shadow-sm dark:border-red-900">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-50 dark:bg-red-950/50">
            <AlertCircle
              className="h-7 w-7 text-red-500 dark:text-red-400"
              aria-hidden="true"
            />
          </div>

          <h1 className="mt-4 text-xl font-semibold text-card-foreground">
            Could not load profile
          </h1>

          <p className="mt-2 text-sm text-muted-foreground">
            {profileError || "Your profile information could not be loaded."}
          </p>

          <div className="mt-6 flex flex-col justify-center gap-3 sm:flex-row">
            <Button
              asChild
              type="button"
              variant="outline"
              className="border-border text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
            >
              <Link href="/profile">
                <ChevronLeft className="mr-2 h-4 w-4" aria-hidden="true" />
                Back to profile
              </Link>
            </Button>

            <Button
              type="button"
              onClick={() => {
                void handleRetry();
              }}
              className="bg-pink-500 text-white hover:bg-pink-600"
            >
              <RefreshCw className="mr-2 h-4 w-4" aria-hidden="true" />
              Try again
            </Button>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="container mx-auto min-h-[calc(100svh-4rem)] max-w-3xl px-4 py-8 text-foreground">
      <Link
        href="/profile"
        className="mb-6 inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
      >
        <ChevronLeft className="h-4 w-4" aria-hidden="true" />
        Back to profile
      </Link>

      <div className="mb-8">
        <h1 className="text-2xl font-bold text-foreground">Settings</h1>

        <p className="mt-1 text-muted-foreground">
          Manage your account information
        </p>
      </div>

      <section className="mb-6 flex items-center gap-4 rounded-xl border border-border bg-card p-6 text-card-foreground shadow-sm">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full border-4 border-pink-200 bg-pink-100 text-xl font-semibold text-pink-500 dark:border-pink-900 dark:bg-pink-950/50 dark:text-pink-400">
          {avatarFallback}
        </div>

        <div className="min-w-0">
          <p className="truncate text-lg font-semibold text-card-foreground">
            {currentUser.nickname}
          </p>

          <p className="truncate text-sm text-muted-foreground">
            {currentUser.email}
          </p>
        </div>
      </section>

      <section className="rounded-xl border border-border bg-card text-card-foreground shadow-sm">
        <div className="border-b border-border px-6 py-5">
          <h2 className="text-lg font-semibold text-card-foreground">
            Personal info
          </h2>

          <p className="mt-1 text-sm text-muted-foreground">
            Update your nickname and email address.
          </p>
        </div>

        <form onSubmit={handleSave} className="space-y-6 p-6">
          <div>
            <Label
              htmlFor="nickname"
              className="mb-2 block text-sm font-medium text-foreground"
            >
              Nickname
            </Label>

            <div className="relative">
              <User
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                aria-hidden="true"
              />

              <Input
                id="nickname"
                name="nickname"
                type="text"
                value={nickname}
                onChange={handleNicknameChange}
                placeholder="Enter your nickname"
                autoComplete="nickname"
                disabled={isSaving}
                required
                className="h-11 border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground hover:border-muted-foreground/50 focus-visible:border-pink-400 focus-visible:ring-pink-500/20 disabled:bg-muted"
              />
            </div>

            <p className="mt-2 text-sm text-muted-foreground">
              This is how other users will see you.
            </p>
          </div>

          <div>
            <Label
              htmlFor="email"
              className="mb-2 block text-sm font-medium text-foreground"
            >
              Email
            </Label>

            <div className="relative">
              <Mail
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                aria-hidden="true"
              />

              <Input
                id="email"
                name="email"
                type="email"
                value={email}
                onChange={handleEmailChange}
                placeholder="Enter your email address"
                autoComplete="email"
                disabled={isSaving}
                required
                className="h-11 border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground hover:border-muted-foreground/50 focus-visible:border-pink-400 focus-visible:ring-pink-500/20 disabled:bg-muted"
              />
            </div>

            <p className="mt-2 text-sm text-muted-foreground">
              Used for signing in to your account.
            </p>
          </div>

          <div className="flex flex-col gap-3 border-t border-border pt-5 sm:flex-row sm:items-center sm:justify-end">
            {hasChanges && (
              <Button
                type="button"
                variant="outline"
                onClick={handleCancelChanges}
                disabled={isSaving}
                className="border-border text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
              >
                Cancel changes
              </Button>
            )}

            <Button
              type="submit"
              disabled={!hasChanges || isSaving}
              className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
            >
              {isSaving ? (
                <>
                  <Loader2
                    className="mr-2 h-4 w-4 animate-spin"
                    aria-hidden="true"
                  />
                  Saving...
                </>
              ) : (
                "Save changes"
              )}
            </Button>
          </div>
        </form>
      </section>
    </main>
  );
}
