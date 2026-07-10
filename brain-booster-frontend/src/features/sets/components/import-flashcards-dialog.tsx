"use client";

import { useMemo, useState, type ChangeEvent, type KeyboardEvent } from "react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import type { ImportedFlashcard } from "@/features/sets/types";

type ImportMode = "text" | "json" | "csv";

type TermDefinitionSeparator = "tab" | "comma" | "custom";

type CardSeparator = "newline" | "semicolon" | "custom";

interface ImportFlashcardsDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onImport: (flashcards: ImportedFlashcard[]) => void;
  allowCsvImport?: boolean;
}

interface ParseResult {
  flashcards: ImportedFlashcard[];
  error?: string;
}

const MAX_CSV_FILE_SIZE_IN_BYTES = 1024 * 1024;

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

function parseCsvRows(text: string, delimiter: string) {
  const rows: string[][] = [];

  let row: string[] = [];
  let field = "";
  let inQuotes = false;

  const normalizedText = text.replace(/^\uFEFF/, "");

  for (let index = 0; index < normalizedText.length; index += 1) {
    const character = normalizedText[index];
    const nextCharacter = normalizedText[index + 1];

    if (character === '"') {
      if (inQuotes && nextCharacter === '"') {
        field += '"';
        index += 1;
      } else {
        inQuotes = !inQuotes;
      }

      continue;
    }

    if (character === delimiter && !inQuotes) {
      row.push(field);
      field = "";

      continue;
    }

    if ((character === "\n" || character === "\r") && !inQuotes) {
      if (character === "\r" && nextCharacter === "\n") {
        index += 1;
      }

      row.push(field);

      if (row.some((cell) => cell.trim())) {
        rows.push(row.map((cell) => cell.trim()));
      }

      row = [];
      field = "";

      continue;
    }

    field += character;
  }

  row.push(field);

  if (row.some((cell) => cell.trim())) {
    rows.push(row.map((cell) => cell.trim()));
  }

  return rows;
}

function detectCsvDelimiter(text: string) {
  const firstNonEmptyLine = text
    .replace(/^\uFEFF/, "")
    .split(/\r?\n/)
    .find((line) => line.trim());

  if (!firstNonEmptyLine) {
    return ",";
  }

  return [",", ";", "\t"].reduce((bestDelimiter, currentDelimiter) => {
    const bestColumnCount =
      parseCsvRows(firstNonEmptyLine, bestDelimiter)[0]?.length ?? 0;

    const currentColumnCount =
      parseCsvRows(firstNonEmptyLine, currentDelimiter)[0]?.length ?? 0;

    return currentColumnCount > bestColumnCount
      ? currentDelimiter
      : bestDelimiter;
  }, ",");
}

function parseImportedFlashcardsFromCsv(text: string): ParseResult {
  if (!text.trim()) {
    return { flashcards: [] };
  }

  const delimiter = detectCsvDelimiter(text);
  const rows = parseCsvRows(text, delimiter);

  if (rows.length === 0) {
    return {
      flashcards: [],
      error: "CSV file is empty.",
    };
  }

  const flashcards = rows
    .map((row) => {
      if (row.length < 2) {
        return null;
      }

      const term = String(row[0] ?? "").trim();
      const definition = String(row[1] ?? "").trim();

      if (!term && !definition) {
        return null;
      }

      return {
        term,
        definition,
      };
    })
    .filter((flashcard): flashcard is ImportedFlashcard => Boolean(flashcard));

  if (flashcards.length === 0) {
    return {
      flashcards: [],
      error: "CSV must contain at least two columns: term and definition.",
    };
  }

  return { flashcards };
}

export function ImportFlashcardsDialog({
  open,
  onOpenChange,
  onImport,
  allowCsvImport = false,
}: ImportFlashcardsDialogProps) {
  const [text, setText] = useState("");
  const [importMode, setImportMode] = useState<ImportMode>("text");

  const [termDefinitionSeparator, setTermDefinitionSeparator] =
    useState<TermDefinitionSeparator>("tab");

  const [cardSeparator, setCardSeparator] = useState<CardSeparator>("newline");

  const [customTermDefinitionSeparator, setCustomTermDefinitionSeparator] =
    useState("");

  const [customCardSeparator, setCustomCardSeparator] = useState("");

  const [csvFileName, setCsvFileName] = useState("");

  const [csvFileError, setCsvFileError] = useState<string | undefined>();

  const [csvInputKey, setCsvInputKey] = useState(0);

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
    if (csvFileError) {
      return {
        flashcards: [],
        error: csvFileError,
      };
    }

    if (importMode === "json") {
      return parseImportedFlashcardsFromJson(text);
    }

    if (importMode === "csv") {
      return parseImportedFlashcardsFromCsv(text);
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
    csvFileError,
  ]);

  const parsedFlashcards = parseResult.flashcards;

  const previewFlashcard = parsedFlashcards[0];

  function resetCsvState() {
    setCsvFileName("");
    setCsvFileError(undefined);
    setCsvInputKey((currentKey) => currentKey + 1);
  }

  function handleImportModeChange(value: string) {
    setImportMode(value as ImportMode);
    setText("");
    resetCsvState();
  }

  function handleTextareaKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: text,
        onChange: setText,
        insertedText: "\t",
      });
    }
  }

  function handleCustomTermDefinitionSeparatorKeyDown(
    event: KeyboardEvent<HTMLInputElement>,
  ) {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: customTermDefinitionSeparator,
        onChange: setCustomTermDefinitionSeparator,
        insertedText: "\t",
      });
    }
  }

  function handleCustomCardSeparatorKeyDown(
    event: KeyboardEvent<HTMLInputElement>,
  ) {
    if (event.key === "Tab" && !event.shiftKey) {
      insertTextAtCursor({
        event,
        value: customCardSeparator,
        onChange: setCustomCardSeparator,
        insertedText: "\t",
      });
    }
  }

  async function handleCsvFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];

    setText("");
    setCsvFileName("");
    setCsvFileError(undefined);

    if (!file) {
      return;
    }

    if (!file.name.toLowerCase().endsWith(".csv")) {
      setCsvFileError("Choose a CSV file.");
      return;
    }

    if (file.size > MAX_CSV_FILE_SIZE_IN_BYTES) {
      setCsvFileError("CSV file is too large. Maximum size is 1 MB.");

      return;
    }

    try {
      const fileContent = await file.text();

      setText(fileContent);
      setCsvFileName(file.name);
    } catch {
      setCsvFileError("Failed to read CSV file.");
    }
  }

  function handleImport() {
    if (parsedFlashcards.length === 0 || parseResult.error) {
      return;
    }

    onImport(parsedFlashcards);
    setText("");
    resetCsvState();
    onOpenChange(false);
  }

  function handleClose() {
    onOpenChange(false);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-3xl overflow-y-auto overflow-x-hidden border-border bg-background text-foreground">
        <DialogHeader>
          <DialogTitle>Import your flashcards</DialogTitle>

          <DialogDescription>
            Paste your terms and definitions or choose a CSV file, then import
            them into this set.
          </DialogDescription>
        </DialogHeader>

        <div className="min-w-0 space-y-6">
          <div className="rounded-lg border border-border bg-muted/50 p-4">
            <p className="mb-3 text-sm font-semibold text-foreground">
              Import format
            </p>

            <Select value={importMode} onValueChange={handleImportModeChange}>
              <SelectTrigger
                className="w-[180px] border-input bg-background text-foreground"
                aria-label="Import format"
              >
                <SelectValue />
              </SelectTrigger>

              <SelectContent className="border-border bg-popover text-popover-foreground">
                <SelectItem value="text">Text</SelectItem>

                <SelectItem value="json">JSON</SelectItem>

                {allowCsvImport && <SelectItem value="csv">CSV</SelectItem>}
              </SelectContent>
            </Select>
          </div>

          {importMode === "csv" ? (
            <div className="rounded-xl border border-dashed border-border bg-card p-4 text-card-foreground">
              <Input
                key={csvInputKey}
                type="file"
                accept=".csv,text/csv"
                onChange={(event) => {
                  void handleCsvFileChange(event);
                }}
                className="cursor-pointer border-input bg-background text-foreground file:text-foreground"
                aria-label="Choose CSV file"
              />

              <p className="mt-3 text-sm text-muted-foreground">
                The first column will be imported as the term and the second
                column as the definition. Do not include a header row unless you
                want it to become a flashcard.
              </p>

              {csvFileName ? (
                <p className="mt-2 text-sm text-foreground">
                  Selected file:{" "}
                  <span className="font-medium">{csvFileName}</span>
                </p>
              ) : (
                <p className="mt-2 text-sm text-muted-foreground">
                  No CSV file selected.
                </p>
              )}
            </div>
          ) : (
            <Textarea
              value={text}
              onChange={(event) => setText(event.target.value)}
              onKeyDown={handleTextareaKeyDown}
              placeholder={
                importMode === "json" ? jsonPlaceholder : textPlaceholder
              }
              wrap="off"
              className="h-56 max-h-56 min-h-56 w-full min-w-0 resize-none overflow-x-auto overflow-y-auto rounded-xl border-input bg-background font-mono text-sm leading-6 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-500 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-400"
              aria-label="Flashcards to import"
            />
          )}

          {importMode === "text" ? (
            <div className="grid gap-4 md:grid-cols-2">
              <div className="rounded-lg border border-border bg-muted/50 p-4">
                <p className="mb-3 text-sm font-semibold text-foreground">
                  Between term and definition
                </p>

                <Select
                  value={termDefinitionSeparator}
                  onValueChange={(value) =>
                    setTermDefinitionSeparator(value as TermDefinitionSeparator)
                  }
                >
                  <SelectTrigger
                    className="border-input bg-background text-foreground"
                    aria-label="Term and definition separator"
                  >
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent className="border-border bg-popover text-popover-foreground">
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
                    className="mt-3 border-input bg-background text-foreground placeholder:text-muted-foreground"
                    aria-label="Custom term and definition separator"
                  />
                )}
              </div>

              <div className="rounded-lg border border-border bg-muted/50 p-4">
                <p className="mb-3 text-sm font-semibold text-foreground">
                  Between cards
                </p>

                <Select
                  value={cardSeparator}
                  onValueChange={(value) =>
                    setCardSeparator(value as CardSeparator)
                  }
                >
                  <SelectTrigger
                    className="border-input bg-background text-foreground"
                    aria-label="Card separator"
                  >
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent className="border-border bg-popover text-popover-foreground">
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
                    className="mt-3 border-input bg-background text-foreground placeholder:text-muted-foreground"
                    aria-label="Custom card separator"
                  />
                )}
              </div>
            </div>
          ) : importMode === "json" ? (
            <div className="rounded-lg border border-border bg-muted/50 p-4 text-sm text-muted-foreground">
              JSON can be an array of objects with{" "}
              <span className="font-medium text-foreground">term</span> and{" "}
              <span className="font-medium text-foreground">definition</span>,
              or an object with a{" "}
              <span className="font-medium text-foreground">flashcards</span>{" "}
              array.
            </div>
          ) : (
            <div className="rounded-lg border border-border bg-muted/50 p-4 text-sm text-muted-foreground">
              CSV supports comma, semicolon and tab-separated files. The parser
              also supports quoted values, for example{" "}
              <span className="font-medium text-foreground">
                &quot;USA&quot;,&quot;United States of America&quot;
              </span>
              .
            </div>
          )}

          <div className="min-w-0 rounded-lg border border-border bg-card text-card-foreground">
            <div className="flex items-center justify-between border-b border-border px-4 py-3">
              <h3 className="font-semibold text-card-foreground">Preview</h3>

              <span
                className="text-sm text-muted-foreground"
                aria-live="polite"
              >
                {parsedFlashcards.length}{" "}
                {parsedFlashcards.length === 1 ? "card" : "cards"}
              </span>
            </div>

            {parseResult.error && (text.trim() || importMode === "csv") ? (
              <div
                className="px-4 py-8 text-center text-sm text-red-500 dark:text-red-400"
                role="alert"
              >
                {parseResult.error}
              </div>
            ) : previewFlashcard ? (
              <div className="grid min-w-0 gap-4 px-4 py-4 md:grid-cols-[40px_minmax(0,1fr)_minmax(0,1fr)]">
                <span className="text-sm text-muted-foreground">1</span>

                <p className="min-w-0 break-words text-sm text-card-foreground">
                  {previewFlashcard.term || (
                    <span className="text-muted-foreground">No term</span>
                  )}
                </p>

                <p className="min-w-0 break-words text-sm text-muted-foreground">
                  {previewFlashcard.definition || (
                    <span className="text-muted-foreground">No definition</span>
                  )}
                </p>
              </div>
            ) : (
              <div className="px-4 py-8 text-center text-sm text-muted-foreground">
                {importMode === "csv"
                  ? "Choose a CSV file above to see a preview."
                  : "Paste text above to see a preview."}
              </div>
            )}
          </div>
        </div>

        <DialogFooter className="gap-2 sm:justify-end">
          <Button
            type="button"
            variant="outline"
            onClick={handleClose}
            className="border-border text-muted-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
          >
            Cancel
          </Button>

          <Button
            type="button"
            onClick={handleImport}
            disabled={
              parsedFlashcards.length === 0 || Boolean(parseResult.error)
            }
            className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
          >
            Import
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
