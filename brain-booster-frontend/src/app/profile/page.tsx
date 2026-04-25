"use client";

import { ProfileDashboard } from "@/components/profile-dashboard";

export default function ProfilePage() {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <main className="flex-1">
        <ProfileDashboard />
      </main>
    </div>
  );
}
