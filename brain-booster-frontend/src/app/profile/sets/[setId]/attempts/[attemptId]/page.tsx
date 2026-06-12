import { notFound } from "next/navigation";

import AttemptDetailsClient from "./attempt-details-client";

interface AttemptDetailsPageProps {
  params: Promise<{
    setId: string;
    attemptId: string;
  }>;
}

export default async function AttemptDetailsPage({
  params,
}: AttemptDetailsPageProps) {
  const { setId, attemptId } = await params;

  const parsedSetId = Number(setId);
  const parsedAttemptId = Number(attemptId);

  if (
    !Number.isInteger(parsedSetId) ||
    parsedSetId <= 0 ||
    !Number.isInteger(parsedAttemptId) ||
    parsedAttemptId <= 0
  ) {
    notFound();
  }

  return (
    <AttemptDetailsClient setId={parsedSetId} attemptId={parsedAttemptId} />
  );
}
