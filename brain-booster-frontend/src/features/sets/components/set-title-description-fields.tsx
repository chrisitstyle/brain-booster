import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

interface SetTitleDescriptionFieldsProps {
  title: string;
  description: string;
  onTitleChange: (value: string) => void;
  onDescriptionChange: (value: string) => void;
}

export function SetTitleDescriptionFields({
  title,
  description,
  onTitleChange,
  onDescriptionChange,
}: SetTitleDescriptionFieldsProps) {
  return (
    <div className="mb-8 space-y-4">
      <div className="group relative">
        <Input
          placeholder="Enter a title, like 'Biology - Chapter 22: Evolution'"
          value={title}
          onChange={(event) => onTitleChange(event.target.value)}
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
          onChange={(event) => onDescriptionChange(event.target.value)}
          className="min-h-[60px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-gray-700 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
        />

        <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
          Description
        </span>
      </div>
    </div>
  );
}
