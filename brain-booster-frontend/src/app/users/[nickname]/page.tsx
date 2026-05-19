import { redirect } from "next/navigation";

export default async function Page({
  params,
}: {
  params: Promise<{ nickname: string }>;
}) {
  const { nickname } = await params;

  redirect(`/users/${nickname}/profile`);
}
