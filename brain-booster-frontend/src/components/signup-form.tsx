"use client";

import { useState } from "react";
import Link from "next/link";
import { Eye, EyeOff } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

import { useRouter } from "next/navigation";
import { registerUser } from "@/api/auth";
import { toast } from "sonner";
export function SignupForm({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  //const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    // setError(null);

    try {
      const response = await registerUser({ nickname, email, password });
      toast.success(response, {
        style: {
          background: "green",
          color: "white",
        },
      });
      router.push("/login");
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : "Something went wrong";
      toast.error(message, {
        style: {
          background: "red",
          color: "white",
        },
      });
      // setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={cn("mx-auto w-full max-w-md", className)} {...props}>
      <Card className="border-gray-200 shadow-sm">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center text-gray-800">
            Create your <span className="text-pink-500">BrainBooster</span>{" "}
            account
          </CardTitle>
          <CardDescription className="text-center text-gray-600">
            Enter your information to get started
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nickname" className="text-gray-700">
                Nickname
              </Label>
              <Input
                id="nickname"
                placeholder="nickname"
                required
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="border-gray-300 focus-visible:ring-pink-500"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email" className="text-gray-700">
                Email
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="name@example.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="border-gray-300 focus-visible:ring-pink-500"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password" className="text-gray-700">
                Password
              </Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="border-gray-300 focus-visible:ring-pink-500 pr-10"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="absolute right-0 top-0 h-full px-3 py-2 text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                  <span className="sr-only">
                    {showPassword ? "Hide password" : "Show password"}
                  </span>
                </Button>
              </div>
              {/* <p className="text-xs text-gray-500">
                Password must be at least 8 characters long and include a number
                and a special character
              </p> */}
            </div>
            <div className="flex items-start space-x-2">
              <Checkbox id="terms" className="mt-1" required />
              <Label
                htmlFor="terms"
                className="text-sm text-gray-600 font-normal"
              >
                I agree to the{" "}
                <Link
                  href="/terms"
                  className="text-pink-500 hover:text-pink-600 underline"
                >
                  Terms of Service
                </Link>{" "}
                and{" "}
                <Link
                  href="/privacy"
                  className="text-pink-500 hover:text-pink-600 underline"
                >
                  Privacy Policy
                </Link>
              </Label>
            </div>
            <Button
              type="submit"
              className="w-full bg-pink-500 hover:bg-pink-600 text-white"
              disabled={isLoading}
            >
              {isLoading ? "Creating account..." : "Sign up"}
            </Button>
          </form>

          <div className="mt-4 relative">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t border-gray-200" />
            </div>
          </div>
        </CardContent>
        <CardFooter className="flex flex-col items-center justify-center space-y-2 border-t border-gray-200 bg-gray-50 p-6">
          <div className="text-center text-sm text-gray-600">
            Already have an account?{" "}
            <Link
              href="/login"
              className="font-medium text-pink-500 hover:text-pink-600"
            >
              Log in
            </Link>
          </div>
          <div className="text-center text-xs text-gray-500">
            By signing up, you&apos;ll get access to all BrainBooster features
            and receive occasional product updates.
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
