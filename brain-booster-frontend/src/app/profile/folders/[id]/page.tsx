import FolderDetailsPage from "./folder-details-page";

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <main className="flex-1">
        <FolderDetailsPage folderId={id} />
      </main>
    </div>
  );
}
