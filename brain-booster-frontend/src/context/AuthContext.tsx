"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useSyncExternalStore,
  ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { authenticateUser } from "@/api/auth";
import { toast } from "sonner";
import { parseJwt } from "@/utils/jwt";

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isAuthLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_STORAGE_KEY = "token";

// Custom event is needed because the native "storage" event does not fire
// in the same browser tab that changes localStorage.
const TOKEN_CHANGE_EVENT = "brainbooster-token-change";

function isTokenExpired(token: string) {
  const decoded = parseJwt(token);

  if (!decoded || !decoded.exp) {
    return true;
  }

  return decoded.exp * 1000 <= Date.now();
}

// Reads the current token from localStorage.
// useSyncExternalStore calls this on the client to get the latest snapshot.
function getTokenSnapshot() {
  if (typeof window === "undefined") {
    return null;
  }

  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

// Server snapshot must be stable because localStorage is not available during SSR.
function getServerTokenSnapshot() {
  return null;
}

// Subscribes React to token changes from localStorage.
// This keeps auth state in sync after login, logout, token expiry,
// and changes from another browser tab.
function subscribeToTokenChange(callback: () => void) {
  if (typeof window === "undefined") {
    return () => {};
  }

  window.addEventListener("storage", callback);
  window.addEventListener(TOKEN_CHANGE_EVENT, callback);

  return () => {
    window.removeEventListener("storage", callback);
    window.removeEventListener(TOKEN_CHANGE_EVENT, callback);
  };
}

// Used only to know whether the component is already running on the client.
// This avoids reading browser-only state during SSR.
function subscribeToClientReady() {
  return () => {};
}

function getClientSnapshot() {
  return true;
}

function getServerClientSnapshot() {
  return false;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();

  const rawToken = useSyncExternalStore(
    subscribeToTokenChange,
    getTokenSnapshot,
    getServerTokenSnapshot,
  );

  const isClient = useSyncExternalStore(
    subscribeToClientReady,
    getClientSnapshot,
    getServerClientSnapshot,
  );

  // Expose only a valid, non-expired token to the rest of the app.
  const token = rawToken && !isTokenExpired(rawToken) ? rawToken : null;
  const isAuthLoading = !isClient;

  const setStoredToken = useCallback((newToken: string | null) => {
    if (typeof window === "undefined") {
      return;
    }

    if (newToken) {
      localStorage.setItem(TOKEN_STORAGE_KEY, newToken);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }

    // Notify this tab that token state has changed.
    window.dispatchEvent(new Event(TOKEN_CHANGE_EVENT));
  }, []);

  const logout = useCallback(() => {
    setStoredToken(null);
    toast.success("Logged out successfully");
    router.push("/login");
  }, [router, setStoredToken]);

  // Removes an expired token from localStorage as soon as it is detected.
  useEffect(() => {
    if (rawToken && isTokenExpired(rawToken)) {
      setStoredToken(null);
    }
  }, [rawToken, setStoredToken]);

  // Schedules automatic logout exactly when the JWT expires.
  useEffect(() => {
    if (!token) return;

    const decoded = parseJwt(token);

    if (!decoded || !decoded.exp) {
      setStoredToken(null);
      return;
    }

    const expirationTime = decoded.exp * 1000;
    const timeRemaining = expirationTime - Date.now();

    const timeoutId = window.setTimeout(
      () => {
        setStoredToken(null);
        toast.error("Session expired. Please log in again.");
        router.push("/login");
      },
      Math.max(timeRemaining, 0),
    );

    return () => window.clearTimeout(timeoutId);
  }, [token, router, setStoredToken]);

  const login = async (email: string, password: string) => {
    try {
      const newToken = await authenticateUser({ email, password });

      setStoredToken(newToken);

      toast.success("Logged in successfully");
      router.push("/");
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Invalid login credentials";

      toast.error(message);
      throw error;
    }
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        isAuthenticated: !!token,
        isAuthLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
