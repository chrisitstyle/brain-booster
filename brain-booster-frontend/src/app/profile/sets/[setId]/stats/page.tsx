import { notFound } from "next/navigation";

import ProfileSetStatsClient from "./profile-set-stats-client";

interface ProfileSetStatsPageProps {
  params: Promise<{
    setId: string;
  }>;
}

export default async function ProfileSetStatsPage({
  params,
}: ProfileSetStatsPageProps) {
  const { setId } = await params;
  const parsedSetId = Number(setId);

  if (!Number.isInteger(parsedSetId) || parsedSetId <= 0) {
    notFound();
  }

  return <ProfileSetStatsClient setId={parsedSetId} />;
}
