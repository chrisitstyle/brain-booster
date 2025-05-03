import Link from "next/link";
import Navbar from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { FileQuestion } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1 flex items-center justify-center p-4 py-12 bg-gray-50">
        <div className="max-w-md w-full text-center space-y-6">
          <div className="flex justify-center">
            <div className="rounded-full bg-pink-100 p-6">
              <FileQuestion className="h-16 w-16 text-pink-500" />
            </div>
          </div>

          <h1 className="text-4xl font-bold text-gray-800">Page not found</h1>

          <p className="text-gray-600 text-lg">
            Oops! It seems the page you&apos;re looking for doesn&apos;t exist
            or has been moved.
          </p>

          <div className="space-y-3">
            <Button
              asChild
              className="bg-pink-500 hover:bg-pink-600 text-white px-8 py-6 text-lg rounded-full"
            >
              <Link href="/">Return to Home</Link>
            </Button>

            <div className="text-sm text-gray-500">Error code: 404</div>
          </div>

          <div className="border-t border-gray-200 pt-6 mt-6">
            <p className="text-gray-600">
              Looking for something specific? Try using the search bar in the
              navigation or check out our{" "}
              <Link
                href="/subjects"
                className="text-pink-500 hover:text-pink-600 underline"
              >
                popular subjects
              </Link>
              .
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
