"use client";

import { useMemo, useState, type KeyboardEvent } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { ImportedFlashcard } from "@/features/sets/types";

type ImportMode = "text" | "json";
type TermDefinitionSeparator = "tab" | "comma" | "custom";
type CardSeparator = "newline" | "semicolon" | "custom";

interface ImportFlashcardsDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onImport: (flashcards: ImportedFlashcard[]) => void;
}

interface ParseResult {
  flashcards: ImportedFlashcard[];
  error?: string;
}

function insertTextAtCursor({
  event,
  value,
  onChange,
  insertedText,
}: {
  event: KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>;
  value: string;
  onChange: (value: string) => void;
  insertedText: string;
}) {
  event.preventDefault();

  const target = event.currentTarget;
  const selectionStart = target.selectionStart ?? value.length;
  const selectionEnd = target.selectionEnd ?? value.length;

  const nextValue =
    value.slice(0, selectionStart) + insertedText + value.slice(selectionEnd);

  onChange(nextValue);

  requestAnimationFrame(() => {
    const nextCursorPosition = selectionStart + insertedText.length;

    target.selectionStart = nextCursorPosition;
    target.selectionEnd = nextCursorPosition;
  });
}

function getTermDefinitionSeparator(
  separator: TermDefinitionSeparator,
  customSeparator: string,
) {
  if (separator === "tab") {
    return "\t";
  }

  if (separator === "comma") {
    return ",";
  }

  return customSeparator;
}

function getCardSeparator(separator: CardSeparator, customSeparator: string) {
  if (separator === "newline") {
    return "\n";
  }

  if (separator === "semicolon") {
    return ";";
  }

  return customSeparator;
}

function splitCards(
  text: string,
  separator: CardSeparator,
  customSeparator: string,
) {
  if (separator === "newline") {
    return text.split(/\r?\n/);
  }

  if (separator === "semicolon") {
    return text.split(";");
  }

  if (!customSeparator) {
    return [];
  }

  return text.split(customSeparator);
}

function getTextPlaceholder({
  termDefinitionSeparator,
  customTermDefinitionSeparator,
  cardSeparator,
  customCardSeparator,
}: {
  termDefinitionSeparator: TermDefinitionSeparator;
  customTermDefinitionSeparator: string;
  cardSeparator: CardSeparator;
  customCardSeparator: string;
}) {
  const termSeparator =
    getTermDefinitionSeparator(
      termDefinitionSeparator,
      customTermDefinitionSeparator,
    ) || " | ";

  const cardsSeparator =
    getCardSeparator(cardSeparator, customCardSeparator) || "\n";

  return [
    `word${termSeparator}definition`,
    `USA${termSeparator}United States of America`,
    `NATO${termSeparator}North Atlantic Treaty Organization`,
  ].join(cardsSeparator);
}

const jsonPlaceholder = `[
  {
    "term": "USA",
    "definition": "United States of America"
  },
  {
    "term": "NATO",
    "definition": "North Atlantic Treaty Organization"
  }
]`;

function parseImportedFlashcardsFromText({
  text,
  termDefinitionSeparator,
  customTermDefinitionSeparator,
  cardSeparator,
  customCardSeparator,
}: {
  text: string;
  termDefinitionSeparator: TermDefinitionSeparator;
  customTermDefinitionSeparator: string;
  cardSeparator: CardSeparator;
  customCardSeparator: string;
}): ParseResult {
  const separator = getTermDefinitionSeparator(
    termDefinitionSeparator,
    customTermDefinitionSeparator,
  );

  if (!text.trim()) {
    return { flashcards: [] };
  }

  if (!separator) {
    return {
      flashcards: [],
      error: "Enter a custom separator between term and definition.",
    };
  }

  const flashcards = splitCards(text, cardSeparator, customCardSeparator)
    .map((rawCard) => rawCard.trim())
    .filter(Boolean)
    .map((rawCard) => {
      const separatorIndex = rawCard.indexOf(separator);

      if (separatorIndex === -1) {
        return null;
      }

      const term = rawCard.slice(0, separatorIndex).trim();
      const definition = rawCard
        .slice(separatorIndex + separator.length)
        .trim();

      if (!term && !definition) {
        return null;
      }

      return {
        term,
        definition,
      };
    })
    .filter((flashcard): flashcard is ImportedFlashcard => Boolean(flashcard));

  return { flashcards };
}

function parseImportedFlashcardsFromJson(text: string): ParseResult {
  if (!text.trim()) {
    return { flashcards: [] };
  }

  try {
    const parsed: unknown = JSON.parse(text);

    let rawFlashcards: unknown[] = [];

    if (Array.isArray(parsed)) {
      rawFlashcards = parsed;
    } else if (
      parsed &&
      typeof parsed === "object" &&
      "flashcards" in parsed &&
      Array.isArray(parsed.flashcards)
    ) {
      rawFlashcards = parsed.flashcards;
    } else if (
      parsed &&
      typeof parsed === "object" &&
      "cards" in parsed &&
      Array.isArray(parsed.cards)
    ) {
      rawFlashcards = parsed.cards;
    } else {
      return {
        flashcards: [],
        error:
          'JSON must be an array or an object with a "flashcards" / "cards" array.',
      };
    }

    const flashcards = rawFlashcards
      .map((item) => {
        if (Array.isArray(item)) {
          const term = String(item[0] ?? "").trim();
          const definition = String(item[1] ?? "").trim();

          if (!term && !definition) {
            return null;
          }

          return {
            term,
            definition,
          };
        }

        if (!item || typeof item !== "object") {
          return null;
        }

        const objectItem = item as Record<string, unknown>;

        const term = String(
          objectItem.term ??
            objectItem.word ??
            objectItem.front ??
            objectItem.question ??
            "",
        ).trim();

        const definition = String(
          objectItem.definition ?? objectItem.answer ?? objectItem.back ?? "",
        ).trim();

        if (!term && !definition) {
          return null;
        }

        return {
          term,
          definition,
        };
      })
      .filter((flashcard): flashcard is ImportedFlashcard =>
        Boolean(flashcard),
      );

    return { flashcards };
  } catch {
    return {
      flashcards: [],
      error: "Invalid JSON format.",
    };
  }
}

export function ImportFlashcardsDialog({
  open,
  onOpenChange,
  onImport,
}: ImportFlashcardsDialogProps) {
  const [text, setText] = useState("");
  const [importMode, setImportMode] = useState<ImportMode>("text");

  const [termDefinitionSeparator, setTermDefinitionSeparator] =
    useState<TermDefinitionSeparator>("tab");
  const [cardSeparator, setCardSeparator] = useState<CardSeparator>("newline");

  const [customTermDefinitionSeparator, setCustomTermDefinitionSeparator] =
    useState("");
  const [customCardSeparator, setCustomCardSeparator] = useState("");

  const textPlaceholder = useMemo(
    () =>
      getTextPlaceholder({
        termDefinitionSeparator,
        customTermDefinitionSeparator,
        cardSeparator,
        customCardSeparator,
      }),
    [
      termDefinitionSeparator,
      customTermDefinitionSeparator,
      cardSeparator,
      customCardSeparator,
    ],
  );

  const parseResult = useMemo(() => {
    if (importMode === "json") {
      return parseImportedFlashcardsFromJson(text);
    }

    return parseImportedFlashcardsFromText({
      text,
      termDefinitionSeparator,
      customTermDefinitionSeparator,
      cardSeparator,
      customCardSeparator,
    });
  }, [
    importMode,
    text,
    termDefinitionSeparator,
    customTermDefinitionSeparator,
    cardSeparator,
    customCardSeparator,
  ]);

  const parsedFlashcards = parseResult.flashcards;
  const previewFlashcard = parsedFlashcards[0];

  const handleTextareaKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: text,
        onChange: setText,
        insertedText: "\t",
      });
    }
  };

  const handleCustomTermDefinitionSeparatorKeyDown = (
    event: KeyboardEvent<HTMLInputElement>,
  ) => {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: customTermDefinitionSeparator,
        onChange: setCustomTermDefinitionSeparator,
        insertedText: "\t",
      });
    }
  };

  const handleCustomCardSeparatorKeyDown = (
    event: KeyboardEvent<HTMLInputElement>,
  ) => {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: customCardSeparator,
        onChange: setCustomCardSeparator,
        insertedText: "\t",
      });
    }
  };

  const handleImport = () => {
    if (parsedFlashcards.length === 0 || parseResult.error) {
      return;
    }

    onImport(parsedFlashcards);
    setText("");
    onOpenChange(false);
  };

  const handleClose = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-3xl overflow-y-auto overflow-x-hidden">
        <DialogHeader>
          <DialogTitle>Import your flashcards</DialogTitle>
          <DialogDescription>
            Paste your terms and definitions, then choose how they are
            separated.
          </DialogDescription>
        </DialogHeader>

        <div className="min-w-0 space-y-6">
          <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
            <p className="mb-3 text-sm font-semibold text-gray-700">
              Import format
            </p>

            <Select
              value={importMode}
              onValueChange={(value) => {
                setImportMode(value as ImportMode);
                setText("");
              }}
            >
              <SelectTrigger className="w-[180px] border-gray-200 bg-white">
                <SelectValue />
              </SelectTrigger>

              <SelectContent>
                <SelectItem value="text">Text</SelectItem>
                <SelectItem value="json">JSON</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Textarea
            value={text}
            onChange={(event) => setText(event.target.value)}
            onKeyDown={handleTextareaKeyDown}
            placeholder={
              importMode === "json" ? jsonPlaceholder : textPlaceholder
            }
            wrap="off"
            className="h-56 max-h-56 min-h-56 w-full min-w-0 resize-none overflow-x-auto overflow-y-auto rounded-xl border-gray-300 bg-white font-mono text-sm leading-6 focus:border-pink-500 focus:ring-pink-500"
          />

          {importMode === "text" ? (
            <div className="grid gap-4 md:grid-cols-2">
              <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
                <p className="mb-3 text-sm font-semibold text-gray-700">
                  Between term and definition
                </p>

                <Select
                  value={termDefinitionSeparator}
                  onValueChange={(value) =>
                    setTermDefinitionSeparator(value as TermDefinitionSeparator)
                  }
                >
                  <SelectTrigger className="border-gray-200 bg-white">
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent>
                    <SelectItem value="tab">Tab</SelectItem>
                    <SelectItem value="comma">Comma</SelectItem>
                    <SelectItem value="custom">Custom</SelectItem>
                  </SelectContent>
                </Select>

                {termDefinitionSeparator === "custom" && (
                  <Input
                    value={customTermDefinitionSeparator}
                    onChange={(event) =>
                      setCustomTermDefinitionSeparator(event.target.value)
                    }
                    onKeyDown={handleCustomTermDefinitionSeparatorKeyDown}
                    placeholder="Enter custom separator"
                    className="mt-3 border-gray-200 bg-white"
                  />
                )}
              </div>

              <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
                <p className="mb-3 text-sm font-semibold text-gray-700">
                  Between cards
                </p>

                <Select
                  value={cardSeparator}
                  onValueChange={(value) =>
                    setCardSeparator(value as CardSeparator)
                  }
                >
                  <SelectTrigger className="border-gray-200 bg-white">
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent>
                    <SelectItem value="newline">New line</SelectItem>
                    <SelectItem value="semicolon">Semicolon</SelectItem>
                    <SelectItem value="custom">Custom</SelectItem>
                  </SelectContent>
                </Select>

                {cardSeparator === "custom" && (
                  <Input
                    value={customCardSeparator}
                    onChange={(event) =>
                      setCustomCardSeparator(event.target.value)
                    }
                    onKeyDown={handleCustomCardSeparatorKeyDown}
                    placeholder="Enter custom separator"
                    className="mt-3 border-gray-200 bg-white"
                  />
                )}
              </div>
            </div>
          ) : (
            <div className="rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm text-gray-600">
              JSON can be an array of objects with{" "}
              <span className="font-medium text-gray-800">term</span> and{" "}
              <span className="font-medium text-gray-800">definition</span>, or
              an object with a{" "}
              <span className="font-medium text-gray-800">flashcards</span>{" "}
              array.
            </div>
          )}

          <div className="min-w-0 rounded-lg border border-gray-200 bg-white">
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <h3 className="font-semibold text-gray-800">Preview</h3>
              <span className="text-sm text-gray-500">
                {parsedFlashcards.length}{" "}
                {parsedFlashcards.length === 1 ? "card" : "cards"}
              </span>
            </div>

            {parseResult.error && text.trim() ? (
              <div className="px-4 py-8 text-center text-sm text-red-500">
                {parseResult.error}
              </div>
            ) : previewFlashcard ? (
              <div className="grid min-w-0 gap-4 px-4 py-4 md:grid-cols-[40px_minmax(0,1fr)_minmax(0,1fr)]">
                <span className="text-sm text-gray-400">1</span>

                <p className="min-w-0 break-words text-sm text-gray-800">
                  {previewFlashcard.term || (
                    <span className="text-gray-400">No term</span>
                  )}
                </p>

                <p className="min-w-0 break-words text-sm text-gray-600">
                  {previewFlashcard.definition || (
                    <span className="text-gray-400">No definition</span>
                  )}
                </p>
              </div>
            ) : (
              <div className="px-4 py-8 text-center text-sm text-gray-500">
                Paste text above to see a preview.
              </div>
            )}
          </div>
        </div>

        <DialogFooter className="gap-2 sm:justify-between">
          <Button
            type="button"
            variant="ghost"
            onClick={handleClose}
            className="text-gray-500 hover:text-gray-700"
          >
            Cancel
          </Button>

          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              className="border-gray-200 text-gray-600 hover:border-pink-300 hover:text-pink-500"
            >
              Cancel import
            </Button>

            <Button
              type="button"
              onClick={handleImport}
              disabled={
                parsedFlashcards.length === 0 || Boolean(parseResult.error)
              }
              className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-300"
            >
              Import
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
