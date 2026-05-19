import CreateFolderForm from "../components/create-folder-form";

export default function Page() {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <main className="flex-1">
        <CreateFolderForm />
      </main>
    </div>
  );
}
