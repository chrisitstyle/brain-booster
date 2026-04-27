"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { authenticateUser } from "@/api/auth";
import { toast } from "sonner";
import { parseJwt } from "@/utils/jwt";

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const router = useRouter();

  // Wrap logout in useCallback to safely use it as a dependency in useEffect
  const logout = useCallback(() => {
    setToken(null);
    localStorage.removeItem("token");
    toast.success("Logged out successfully");
    router.push("/login");
  }, [router]);

  // Load token from localStorage on initial render
  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    if (storedToken) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setToken(storedToken);
    }
  }, []);

  // Handle automatic logout when the JWT token expires
  useEffect(() => {
    if (!token) return; // Do nothing if the user is not logged in

    const decoded = parseJwt(token);
    if (!decoded || !decoded.exp) return;

    // JWT expiration (exp) is in seconds, Date.now() is in milliseconds. Convert to match.
    const expirationTime = decoded.exp * 1000;
    const timeRemaining = expirationTime - Date.now();

    if (timeRemaining <= 0) {
      // Token has already expired (e.g., while the app was closed)
      setTimeout(() => {
        toast.error("Session expired. Please log in again.");
        logout();
      }, 0);
    } else {
      // Set a timer to log out the user exactly when the token expires
      const timeoutId = setTimeout(() => {
        toast.error("Session expired. Please log in again.");
        logout();
      }, timeRemaining);

      // Cleanup function to clear the timeout if the component unmounts
      // or if the token state changes before the time runs out
      return () => clearTimeout(timeoutId);
    }
  }, [token, logout]);

  const login = async (email: string, password: string) => {
    try {
      const newToken = await authenticateUser({ email, password });
      setToken(newToken);
      localStorage.setItem("token", newToken);
      toast.success("Logged in successfully");
      router.push("/"); // Redirect to home page after successful login
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Invalid login credentials";
      toast.error(message);
      throw error;
    }
  };

  return (
    <AuthContext.Provider
      value={{ token, isAuthenticated: !!token, login, logout }}
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
