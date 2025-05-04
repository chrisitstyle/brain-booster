"use client";
import Navbar from "@/components/Navbar";
import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1 flex items-center justify-center p-4 py-12 bg-gray-50">
        <LoginForm />
      </main>
    </div>
  );
}
