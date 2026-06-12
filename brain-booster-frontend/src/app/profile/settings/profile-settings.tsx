"use client";

import { useEffect, useMemo, useState } from "react";
import type { ChangeEvent, SyntheticEvent } from "react";
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

  function handleRetry() {
    if (!token) {
      return;
    }

    const requestToken = token;

    setProfileLoadingState("loading");
    setProfileError("");

    void getCurrentUser(requestToken)
      .then((user) => {
        setCurrentUser(user);
        setNickname(user.nickname);
        setEmail(user.email);
        setProfileError("");
        setProfileLoadingState("success");
      })
      .catch((error: unknown) => {
        const message =
          error instanceof Error
            ? error.message
            : "Failed to load user profile";

        setCurrentUser(null);
        setProfileError(message);
        setProfileLoadingState("error");

        toast.error(message);
      });
  }

  async function handleSave(event: SyntheticEvent<HTMLFormElement>) {
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
    } catch (error) {
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
      <main className="container mx-auto flex min-h-[calc(100vh-4rem)] items-center justify-center px-4 py-10">
        <div className="flex items-center gap-3 text-gray-500">
          <Loader2 className="h-5 w-5 animate-spin text-pink-500" />

          <p>Loading settings...</p>
        </div>
      </main>
    );
  }

  if (profileLoadingState === "error" || !currentUser) {
    return (
      <main className="container mx-auto flex min-h-[calc(100vh-4rem)] max-w-3xl items-center justify-center px-4 py-10">
        <section className="w-full rounded-xl border border-red-100 bg-white p-8 text-center shadow-sm">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-50">
            <AlertCircle className="h-7 w-7 text-red-500" />
          </div>

          <h1 className="mt-4 text-xl font-semibold text-gray-800">
            Could not load profile
          </h1>

          <p className="mt-2 text-sm text-gray-500">
            {profileError || "Your profile information could not be loaded."}
          </p>

          <div className="mt-6 flex flex-col justify-center gap-3 sm:flex-row">
            <Link
              href="/profile"
              className="inline-flex h-10 items-center justify-center rounded-md border border-gray-200 px-4 text-sm font-medium text-gray-600 transition hover:border-pink-200 hover:text-pink-500"
            >
              <ChevronLeft className="mr-2 h-4 w-4" />
              Back to profile
            </Link>

            <button
              type="button"
              onClick={handleRetry}
              className="inline-flex h-10 items-center justify-center rounded-md bg-pink-500 px-4 text-sm font-medium text-white transition hover:bg-pink-600 focus:outline-none focus:ring-2 focus:ring-pink-300 focus:ring-offset-2"
            >
              <RefreshCw className="mr-2 h-4 w-4" />
              Try again
            </button>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="container mx-auto max-w-3xl px-4 py-8">
      <Link
        href="/profile"
        className="mb-6 inline-flex items-center gap-2 text-sm text-gray-500 transition-colors hover:text-pink-500"
      >
        <ChevronLeft className="h-4 w-4" />
        Back to profile
      </Link>

      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800">Settings</h1>

        <p className="mt-1 text-gray-500">Manage your account information</p>
      </div>

      <section className="mb-6 flex items-center gap-4 rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full border-4 border-pink-200 bg-pink-100 text-xl font-semibold text-pink-500">
          {avatarFallback}
        </div>

        <div className="min-w-0">
          <p className="truncate text-lg font-semibold text-gray-800">
            {currentUser.nickname}
          </p>

          <p className="truncate text-sm text-gray-500">{currentUser.email}</p>
        </div>
      </section>

      <section className="rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="border-b border-gray-100 px-6 py-5">
          <h2 className="text-lg font-semibold text-gray-800">Personal info</h2>

          <p className="mt-1 text-sm text-gray-500">
            Update your nickname and email address.
          </p>
        </div>

        <form onSubmit={handleSave} className="space-y-6 p-6">
          <div>
            <label
              htmlFor="nickname"
              className="mb-2 block text-sm font-medium text-gray-700"
            >
              Nickname
            </label>

            <div className="relative">
              <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

              <input
                id="nickname"
                name="nickname"
                type="text"
                value={nickname}
                onChange={handleNicknameChange}
                placeholder="Enter your nickname"
                autoComplete="nickname"
                disabled={isSaving}
                required
                className="h-11 w-full rounded-md border border-gray-200 bg-white pl-10 pr-3 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 hover:border-gray-300 focus:border-pink-400 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-gray-100"
              />
            </div>

            <p className="mt-2 text-sm text-gray-500">
              This is how other users will see you.
            </p>
          </div>

          <div>
            <label
              htmlFor="email"
              className="mb-2 block text-sm font-medium text-gray-700"
            >
              Email
            </label>

            <div className="relative">
              <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

              <input
                id="email"
                name="email"
                type="email"
                value={email}
                onChange={handleEmailChange}
                placeholder="Enter your email address"
                autoComplete="email"
                disabled={isSaving}
                required
                className="h-11 w-full rounded-md border border-gray-200 bg-white pl-10 pr-3 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 hover:border-gray-300 focus:border-pink-400 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-gray-100"
              />
            </div>

            <p className="mt-2 text-sm text-gray-500">
              Used for signing in to your account.
            </p>
          </div>

          <div className="flex flex-col gap-3 border-t border-gray-100 pt-5 sm:flex-row sm:items-center sm:justify-end">
            {hasChanges && (
              <button
                type="button"
                onClick={handleCancelChanges}
                disabled={isSaving}
                className="inline-flex h-10 items-center justify-center rounded-md border border-gray-200 bg-white px-5 text-sm font-medium text-gray-600 transition hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Cancel changes
              </button>
            )}

            <button
              type="submit"
              disabled={!hasChanges || isSaving}
              className="inline-flex h-10 items-center justify-center rounded-md bg-pink-500 px-5 text-sm font-medium text-white transition hover:bg-pink-600 focus:outline-none focus:ring-2 focus:ring-pink-300 focus:ring-offset-2 disabled:cursor-not-allowed disabled:bg-gray-300"
            >
              {isSaving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving...
                </>
              ) : (
                "Save changes"
              )}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
