import { notFound } from "next/navigation";

import AttemptsClient from "./attempts-client";

interface AttemptsPageProps {
  params: Promise<{
    setId: string;
  }>;
}

export default async function AttemptsPage({ params }: AttemptsPageProps) {
  const { setId } = await params;
  const parsedSetId = Number(setId);

  if (!Number.isInteger(parsedSetId) || parsedSetId <= 0) {
    notFound();
  }

  return <AttemptsClient setId={parsedSetId} />;
}
