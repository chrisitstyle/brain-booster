import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
    <div className="mb-8 space-y-8">
      <div className="space-y-2">
        <Input
          id="set-title"
          type="text"
          placeholder="Enter a title, like 'Biology - Chapter 22: Evolution'"
          value={title}
          onChange={(event) => onTitleChange(event.target.value)}
          className="h-auto rounded-none border-0 border-b-2 border-border bg-transparent px-0 py-3 text-xl font-medium text-foreground shadow-none placeholder:text-muted-foreground focus-visible:border-pink-500 focus-visible:ring-0 dark:focus-visible:border-pink-400"
          aria-describedby="set-title-label"
        />

        <Label
          id="set-title-label"
          htmlFor="set-title"
          className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
        >
          Title
        </Label>
      </div>

      <div className="space-y-2">
        <Textarea
          id="set-description"
          placeholder="Add a description..."
          value={description}
          onChange={(event) => onDescriptionChange(event.target.value)}
          className="min-h-[80px] resize-none rounded-none border-0 border-b-2 border-border bg-transparent px-0 py-3 text-foreground shadow-none placeholder:text-muted-foreground focus-visible:border-pink-500 focus-visible:ring-0 dark:focus-visible:border-pink-400"
          aria-describedby="set-description-label"
        />

        <Label
          id="set-description-label"
          htmlFor="set-description"
          className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
        >
          Description
        </Label>
      </div>
    </div>
  );
}
