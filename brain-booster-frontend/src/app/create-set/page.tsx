"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { GripVertical, Plus, Trash2, X, Image, Volume2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface Term {
  id: string;
  term: string;
  definition: string;
}

export default function CreateSetPage() {
  const router = useRouter();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [terms, setTerms] = useState<Term[]>([
    { id: "1", term: "", definition: "" },
    { id: "2", term: "", definition: "" },
    { id: "3", term: "", definition: "" },
    { id: "4", term: "", definition: "" },
  ]);

  const addTerm = () => {
    setTerms([
      ...terms,
      { id: Date.now().toString(), term: "", definition: "" },
    ]);
  };

  const removeTerm = (id: string) => {
    if (terms.length > 2) {
      setTerms(terms.filter((term) => term.id !== id));
    }
  };

  const updateTerm = (
    id: string,
    field: "term" | "definition",
    value: string,
  ) => {
    setTerms(
      terms.map((term) =>
        term.id === id ? { ...term, [field]: value } : term,
      ),
    );
  };

  const handleCreate = () => {
    console.log({ title, description, isPublic, terms });
    // after creating the set, we navigate to the profile page
    router.push("/profile");
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="sticky top-0 z-10 flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3 shadow-sm md:px-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => router.back()}
            className="rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700"
          >
            <X className="h-5 w-5" />
          </button>
          <h1 className="text-lg font-semibold text-gray-800">
            Create a new study set
          </h1>
        </div>
        <div className="flex items-center gap-3">
          <div className="hidden items-center gap-2 md:flex">
            <Label htmlFor="visibility" className="text-sm text-gray-600">
              Visible to everyone
            </Label>
            <Switch
              id="visibility"
              checked={isPublic}
              onCheckedChange={setIsPublic}
              className="data-[state=checked]:bg-pink-500"
            />
          </div>
          <Button
            onClick={handleCreate}
            disabled={
              !title.trim() ||
              terms.filter((t) => t.term && t.definition).length < 2
            }
            className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-300"
          >
            Create
          </Button>
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-4xl px-4 py-6 md:px-6">
          {/* Title and Description */}
          <div className="mb-8 space-y-4">
            <div className="group relative">
              <Input
                placeholder="Enter a title, like 'Biology - Chapter 22: Evolution'"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-xl font-medium text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
              />
              <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
                Title
              </span>
            </div>

            <div className="group relative mt-8">
              <Textarea
                placeholder="Add a description..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="min-h-[60px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-gray-700 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
              />
              <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
                Description
              </span>
            </div>
          </div>

          {/* Import Options */}
          <div className="mb-6 flex flex-wrap gap-3">
            <Button
              variant="outline"
              size="sm"
              className="border-gray-200 text-gray-600 hover:border-pink-300 hover:text-pink-500"
            >
              <Plus className="mr-2 h-4 w-4" />
              Import from Word, Excel, Google Docs, etc.
            </Button>
            <Select defaultValue="both">
              <SelectTrigger className="w-[180px] border-gray-200 text-gray-600">
                <SelectValue placeholder="Study both sides" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="both">Study both sides</SelectItem>
                <SelectItem value="term">Term only</SelectItem>
                <SelectItem value="definition">Definition only</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Terms */}
          <div className="space-y-4">
            {terms.map((term, index) => (
              <TermCard
                key={term.id}
                index={index + 1}
                term={term}
                onUpdate={(field, value) => updateTerm(term.id, field, value)}
                onRemove={() => removeTerm(term.id)}
                canRemove={terms.length > 2}
              />
            ))}
          </div>

          {/* Add Term Button */}
          <button
            onClick={addTerm}
            className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed border-gray-200 bg-white py-4 text-gray-500 transition-all hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500"
          >
            <Plus className="h-5 w-5" />
            <span className="font-medium">Add card</span>
          </button>

          {/* Bottom Actions */}
          <div className="mt-8 flex justify-center pb-8">
            <Button
              onClick={handleCreate}
              disabled={
                !title.trim() ||
                terms.filter((t) => t.term && t.definition).length < 2
              }
              size="lg"
              className="bg-pink-500 px-12 text-white hover:bg-pink-600 disabled:bg-gray-300"
            >
              Create
            </Button>
          </div>
        </div>
      </main>
    </div>
  );
}

interface TermCardProps {
  index: number;
  term: Term;
  onUpdate: (field: "term" | "definition", value: string) => void;
  onRemove: () => void;
  canRemove: boolean;
}

function TermCard({
  index,
  term,
  onUpdate,
  onRemove,
  canRemove,
}: TermCardProps) {
  return (
    <div className="group overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-all hover:shadow-md">
      {/* Card Header */}
      <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-4 py-2">
        <div className="flex items-center gap-3">
          <GripVertical className="h-4 w-4 cursor-grab text-gray-400" />
          <span className="font-medium text-gray-600">{index}</span>
        </div>
        <button
          onClick={onRemove}
          disabled={!canRemove}
          className={cn(
            "rounded p-1 transition-colors",
            canRemove
              ? "text-gray-400 hover:bg-gray-200 hover:text-gray-600"
              : "cursor-not-allowed text-gray-200",
          )}
        >
          <Trash2 className="h-4 w-4" />
        </button>
      </div>

      {/* Card Content */}
      <div className="grid gap-4 p-4 md:grid-cols-2">
        <div className="space-y-2">
          <div className="relative">
            <Textarea
              placeholder="Enter term"
              value={term.term}
              onChange={(e) => onUpdate("term", e.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
            />
            <div className="absolute bottom-2 right-0 flex gap-2">
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <Image className="h-4 w-4" />
              </button>
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <Volume2 className="h-4 w-4" />
              </button>
            </div>
          </div>
          <span className="text-xs font-medium uppercase tracking-wide text-gray-400">
            Term
          </span>
        </div>

        <div className="space-y-2">
          <div className="relative">
            <Textarea
              placeholder="Enter definition"
              value={term.definition}
              onChange={(e) => onUpdate("definition", e.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
            />
            <div className="absolute bottom-2 right-0 flex gap-2">
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <Image className="h-4 w-4" />
              </button>
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <Volume2 className="h-4 w-4" />
              </button>
            </div>
          </div>
          <span className="text-xs font-medium uppercase tracking-wide text-gray-400">
            Definition
          </span>
        </div>
      </div>
    </div>
  );
}
