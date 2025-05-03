"use client";
import Navbar from "@/components/Navbar";
import { SignupForm } from "@/components/signup-form";

export default function SignupPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1 flex items-center justify-center p-4 py-12 bg-gray-50">
        <SignupForm />
      </main>
    </div>
  );
}
