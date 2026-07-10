import FolderDetailsPage from "./folder-details-page";

export default async function Page({
  params,
}: Readonly<{
  params: Promise<{ id: string }>;
}>) {
  const { id } = await params;

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <FolderDetailsPage folderId={id} />
    </main>
  );
}
