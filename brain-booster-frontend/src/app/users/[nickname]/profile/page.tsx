import { getUserFlashcardSetsByNickname } from "@/api/userService";
import PublicProfileContent from "./public-profile-content";
import { notFound } from "next/navigation";

export default async function PublicProfilePage({
  params,
}: {
  params: Promise<{ nickname: string }>;
}) {
  const resolvedParams = await params;
  const nickname = resolvedParams.nickname;

  if (!nickname) {
    notFound();
  }
  // fetching data on the server to avoid showing the profile page before knowning if the user exists or not
  let initialSets = [];
  try {
    initialSets = await getUserFlashcardSetsByNickname(nickname);
  } catch (error) {
    console.error("Failed to fetch flashcard sets on server", error);
    notFound();
  }

  return <PublicProfileContent nickname={nickname} initialSets={initialSets} />;
}
