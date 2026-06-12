import { redirect } from "next/navigation";
import MyStatsClient from "./my-stats-client";

interface MyStatsPageProps {
  searchParams: Promise<{
    setId?: string | string[];
  }>;
}

export default async function MyStatsPage({ searchParams }: MyStatsPageProps) {
  const { setId } = await searchParams;
  const rawSetId = Array.isArray(setId) ? setId[0] : setId;
  const parsedSetId = Number(rawSetId);

  if (Number.isInteger(parsedSetId) && parsedSetId > 0) {
    redirect(`/profile/sets/${parsedSetId}/stats`);
  }

  return <MyStatsClient />;
}
