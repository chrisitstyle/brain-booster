"use client";

import type { FormEvent, HTMLAttributes } from "react";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff } from "lucide-react";
import { toast } from "sonner";

import { registerUser } from "@/api/auth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

export function SignupForm({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) {
  const router = useRouter();

  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsLoading(true);

    try {
      const response = await registerUser({
        nickname,
        email,
        password,
      });

      toast.success(response);
      router.push("/login");
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Something went wrong";

      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  }

  function togglePasswordVisibility() {
    setShowPassword((currentValue) => !currentValue);
  }

  return (
    <div className={cn("mx-auto w-full max-w-md", className)} {...props}>
      <Card className="border-border bg-card text-card-foreground shadow-sm">
        <CardHeader className="space-y-1">
          <CardTitle className="text-center text-2xl font-bold text-card-foreground">
            Create your <span className="text-pink-500">BrainBooster</span>{" "}
            account
          </CardTitle>

          <CardDescription className="text-center text-muted-foreground">
            Enter your information to get started
          </CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nickname" className="text-foreground">
                Nickname
              </Label>

              <Input
                id="nickname"
                type="text"
                placeholder="nickname"
                autoComplete="nickname"
                required
                disabled={isLoading}
                value={nickname}
                onChange={(event) => setNickname(event.target.value)}
                className="border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:ring-pink-500"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-foreground">
                Email
              </Label>

              <Input
                id="email"
                type="email"
                placeholder="name@example.com"
                autoComplete="email"
                required
                disabled={isLoading}
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:ring-pink-500"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-foreground">
                Password
              </Label>

              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  autoComplete="new-password"
                  required
                  disabled={isLoading}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  className="border-input bg-background pr-10 text-foreground placeholder:text-muted-foreground focus-visible:ring-pink-500"
                />

                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="absolute right-0 top-0 h-full px-3 py-2 text-muted-foreground hover:bg-transparent hover:text-foreground"
                  onClick={togglePasswordVisibility}
                  disabled={isLoading}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  aria-pressed={showPassword}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4" aria-hidden="true" />
                  ) : (
                    <Eye className="h-4 w-4" aria-hidden="true" />
                  )}
                </Button>
              </div>
            </div>

            <div className="flex items-start gap-2">
              <Checkbox
                id="terms"
                required
                disabled={isLoading}
                className="mt-1 border-input data-[state=checked]:border-pink-500 data-[state=checked]:bg-pink-500 data-[state=checked]:text-white"
              />

              <Label
                htmlFor="terms"
                className="text-sm font-normal leading-relaxed text-muted-foreground"
              >
                I agree to the{" "}
                <Link
                  href="/terms"
                  className="text-pink-500 underline transition-colors hover:text-pink-600 dark:hover:text-pink-400"
                >
                  Terms of Service
                </Link>{" "}
                and{" "}
                <Link
                  href="/privacy"
                  className="text-pink-500 underline transition-colors hover:text-pink-600 dark:hover:text-pink-400"
                >
                  Privacy Policy
                </Link>
              </Label>
            </div>

            <Button
              type="submit"
              className="w-full bg-pink-500 text-white hover:bg-pink-600"
              disabled={isLoading}
              aria-live="polite"
            >
              {isLoading ? "Creating account..." : "Sign up"}
            </Button>
          </form>
        </CardContent>

        <CardFooter className="flex flex-col items-center justify-center space-y-2 border-t border-border bg-muted/40 p-6">
          <div className="text-center text-sm text-muted-foreground">
            Already have an account?{" "}
            <Link
              href="/login"
              className="font-medium text-pink-500 transition-colors hover:text-pink-600 dark:hover:text-pink-400"
            >
              Log in
            </Link>
          </div>

          <div className="text-center text-xs text-muted-foreground">
            By signing up, you&apos;ll get access to all BrainBooster features
            and receive occasional product updates.
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
