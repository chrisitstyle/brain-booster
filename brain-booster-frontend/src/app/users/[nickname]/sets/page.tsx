import UserSetsPage from "./components/user-sets-page";

interface PageProps {
  params: Promise<{
    nickname: string;
  }>;
}

export default async function Page({ params }: PageProps) {
  const { nickname } = await params;

  return <UserSetsPage nickname={decodeURIComponent(nickname)} />;
}
