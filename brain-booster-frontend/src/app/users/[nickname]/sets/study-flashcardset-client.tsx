"use client";

import { useState } from "react";
import Link from "next/link";
import {
  ArrowLeft,
  ArrowRight,
  Check,
  ChevronLeft,
  Pencil,
  Shuffle,
  Star,
  Volume2,
  X,
} from "lucide-react";
import { toast } from "sonner";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Progress } from "@/components/ui/progress";
import { Input } from "@/components/ui/input";

import { Flashcard, updateFlashcard } from "@/api/flashcardService";
import { FlashcardSet } from "@/api/flashcardSetService";
import { useAuth } from "@/context/AuthContext";

interface StudyFlashcardSetClientProps {
  studySet: FlashcardSet;
  initialFlashcards: Flashcard[];
  nickname: string;
}

export default function StudyFlashcardSetClient({
  studySet,
  initialFlashcards,
  nickname,
}: StudyFlashcardSetClientProps) {
  const { token } = useAuth();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);

  const [flashcards, setFlashcards] = useState<Flashcard[]>(() =>
    initialFlashcards.map((flashcard) => ({
      ...flashcard,
      starred: false,
    })),
  );

  // progress tracking states
  const [knownFlashcards, setKnownFlashcards] = useState<Set<number>>(
    new Set(),
  );
  const [unknownFlashcards, setUnknownFlashcards] = useState<Set<number>>(
    new Set(),
  );

  const [editingFlashcardId, setEditingFlashcardId] = useState<number | null>(
    null,
  );
  const [editTerm, setEditTerm] = useState("");
  const [editDefinition, setEditDefinition] = useState("");

  const currentFlashcard = flashcards[currentIndex];
  const progress =
    flashcards.length > 0
      ? ((knownFlashcards.size + unknownFlashcards.size) / flashcards.length) *
        100
      : 0;

  const handleFlipCard = () => {
    setIsFlipped(!isFlipped);
  };

  const handlePreviousCard = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
      setIsFlipped(false);
    }
  };

  const handleNextCard = () => {
    if (currentIndex < flashcards.length - 1) {
      setCurrentIndex(currentIndex + 1);
      setIsFlipped(false);
    }
  };

  const handleKnownCard = () => {
    if (currentFlashcard) {
      setKnownFlashcards(
        new Set([...knownFlashcards, currentFlashcard.flashcardId]),
      );
      unknownFlashcards.delete(currentFlashcard.flashcardId);
      setUnknownFlashcards(new Set(unknownFlashcards));
      if (currentIndex < flashcards.length - 1) {
        handleNextCard();
      }
    }
  };

  const handleUnknownCard = () => {
    if (currentFlashcard) {
      setUnknownFlashcards(
        new Set([...unknownFlashcards, currentFlashcard.flashcardId]),
      );
      knownFlashcards.delete(currentFlashcard.flashcardId);
      setKnownFlashcards(new Set(knownFlashcards));
      if (currentIndex < flashcards.length - 1) {
        handleNextCard();
      }
    }
  };

  const handleShuffleCards = () => {
    const shuffled = [...flashcards].sort(() => Math.random() - 0.5);
    setFlashcards(shuffled);
    setCurrentIndex(0);
    setIsFlipped(false);
  };

  const toggleStar = (flashcardId: number) => {
    setFlashcards(
      flashcards.map((flashcard) =>
        flashcard.flashcardId === flashcardId
          ? { ...flashcard, starred: !flashcard.starred }
          : flashcard,
      ),
    );
  };

  const speakText = (text: string) => {
    if ("speechSynthesis" in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      speechSynthesis.speak(utterance);
    }
  };

  const startEditing = (flashcard: Flashcard) => {
    setEditingFlashcardId(flashcard.flashcardId);
    setEditTerm(flashcard.term);
    setEditDefinition(flashcard.definition);
  };

  const cancelEditing = () => {
    setEditingFlashcardId(null);
    setEditTerm("");
    setEditDefinition("");
  };

  const saveEditingFlashcard = async (flashcardId: number) => {
    if (!token) {
      toast.error("Unauthorized. Please log in to edit flashcards.");
      return;
    }
    try {
      await updateFlashcard(
        flashcardId,
        { term: editTerm, definition: editDefinition },
        token,
      );

      setFlashcards(
        flashcards.map((flashcard) =>
          flashcard.flashcardId === flashcardId
            ? { ...flashcard, term: editTerm, definition: editDefinition }
            : flashcard,
        ),
      );

      setEditingFlashcardId(null);
      setEditTerm("");
      setEditDefinition("");

      toast.success("Flashcard updated successfully.");
    } catch (error) {
      console.error("Error updating flashcard:", error);
      toast.error("Failed to update flashcard. Please try again.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-6">
        {/* Back Button and Title */}
        <div className="mb-6">
          <Link
            href={`/users/${nickname}/profile`}
            className="mb-4 inline-flex items-center gap-2 text-gray-500 hover:text-pink-500"
          >
            <ChevronLeft className="h-4 w-4" />
            Back to {nickname}&apos;s profile
          </Link>
          <h1 className="text-2xl font-bold text-gray-800 md:text-3xl">
            {studySet.setName}
          </h1>
          {studySet.description && (
            <p className="mt-2 text-gray-600">{studySet.description}</p>
          )}
        </div>

        {flashcards.length > 0 && (
          <div className="mb-6">
            <div className="mb-2 flex items-center justify-between text-sm">
              <span className="text-gray-500">Progress</span>
              <span className="text-gray-500">
                {knownFlashcards.size + unknownFlashcards.size} /{" "}
                {flashcards.length} reviewed
              </span>
            </div>
            <Progress
              value={progress}
              className="h-2 bg-gray-200 [&>div]:bg-pink-500"
            />
            <div className="mt-2 flex gap-4 text-sm">
              <span className="flex items-center gap-1 text-green-600">
                <Check className="h-4 w-4" /> {knownFlashcards.size} known
              </span>
              <span className="flex items-center gap-1 text-red-500">
                <X className="h-4 w-4" /> {unknownFlashcards.size} still
                learning
              </span>
            </div>
          </div>
        )}

        {/* Main Flashcard */}
        {flashcards.length > 0 ? (
          <div className="mx-auto max-w-2xl">
            <div
              className="perspective-1000 mb-4 cursor-pointer"
              onClick={handleFlipCard}
            >
              <div
                className={cn(
                  "relative h-64 w-full transition-transform duration-500 transform-style-3d md:h-80",
                  isFlipped && "rotate-y-180",
                )}
                style={{
                  transformStyle: "preserve-3d",
                  transform: isFlipped ? "rotateY(180deg)" : "rotateY(0deg)",
                }}
              >
                {/* Front of card */}
                <Card
                  className="absolute inset-0 flex items-center justify-center border-gray-200 bg-white p-8 shadow-lg backface-hidden"
                  style={{ backfaceVisibility: "hidden" }}
                >
                  <Button
                    variant="ghost"
                    size="icon"
                    className="absolute top-4 right-4 h-10 w-10 rounded-full text-gray-400 hover:bg-pink-50 hover:text-pink-500"
                    onClick={(e) => {
                      e.stopPropagation();
                      speakText(currentFlashcard.term);
                    }}
                  >
                    <Volume2 className="h-5 w-5" />
                  </Button>
                  <CardContent className="flex flex-col items-center justify-center p-0 text-center">
                    <p className="text-xl font-medium text-gray-800 md:text-2xl">
                      {currentFlashcard?.term}
                    </p>
                  </CardContent>
                </Card>

                {/* Back of card */}
                <Card
                  className="absolute inset-0 flex items-center justify-center border-gray-200 bg-white p-8 shadow-lg"
                  style={{
                    backfaceVisibility: "hidden",
                    transform: "rotateY(180deg)",
                  }}
                >
                  <Button
                    variant="ghost"
                    size="icon"
                    className="absolute top-4 right-4 h-10 w-10 rounded-full text-gray-400 hover:bg-pink-50 hover:text-pink-500"
                    onClick={(e) => {
                      e.stopPropagation();
                      speakText(currentFlashcard.definition);
                    }}
                  >
                    <Volume2 className="h-5 w-5" />
                  </Button>
                  <CardContent className="flex flex-col items-center justify-center p-0 text-center">
                    <p className="text-lg text-gray-700 md:text-xl">
                      {currentFlashcard?.definition}
                    </p>
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Know/Don't Know Buttons */}
            <div className="mb-6 flex items-center justify-center gap-6">
              <Button
                variant="outline"
                size="lg"
                className="h-14 w-14 rounded-full border-2 border-red-200 text-red-500 hover:border-red-400 hover:bg-red-50"
                onClick={handleUnknownCard}
              >
                <X className="h-6 w-6" />
              </Button>

              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="icon"
                  className="border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
                  onClick={handlePreviousCard}
                  disabled={currentIndex === 0}
                >
                  <ArrowLeft className="h-5 w-5" />
                </Button>
                <span className="min-w-20 text-center text-gray-600">
                  {currentIndex + 1} / {flashcards.length}
                </span>
                <Button
                  variant="outline"
                  size="icon"
                  className="border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
                  onClick={handleNextCard}
                  disabled={currentIndex === flashcards.length - 1}
                >
                  <ArrowRight className="h-5 w-5" />
                </Button>
              </div>

              <Button
                variant="outline"
                size="lg"
                className="h-14 w-14 rounded-full border-2 border-green-200 text-green-500 hover:border-green-400 hover:bg-green-50"
                onClick={handleKnownCard}
              >
                <Check className="h-6 w-6" />
              </Button>
            </div>

            {/* Shuffle Button */}
            <div className="mb-8 flex justify-center">
              <Button
                variant="outline"
                className="border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
                onClick={handleShuffleCards}
              >
                <Shuffle className="mr-2 h-4 w-4" />
                Shuffle
              </Button>
            </div>

            {/* Author Info */}
            <div className="mb-8 flex items-center justify-center gap-3 border-t border-gray-200 pt-6">
              <Avatar className="h-10 w-10">
                <AvatarImage src="/placeholder.svg?height=40&width=40" />
                <AvatarFallback className="bg-pink-100 text-pink-500">
                  {studySet.user.nickname.substring(0, 1).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div className="text-sm">
                <p className="text-gray-500">
                  Created by{" "}
                  <Link
                    href={`/users/${studySet.user.nickname}/profile`}
                    className="font-medium text-pink-500 hover:underline"
                  >
                    {studySet.user.nickname}
                  </Link>
                </p>
                <p className="text-gray-400">
                  Created on{" "}
                  {new Date(studySet.createdAt).toLocaleDateString("en-US")}
                </p>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            This study set does not have any flashcards yet.
          </div>
        )}

        {flashcards.length > 0 && (
          <div className="mx-auto max-w-2xl">
            <h2 className="mb-4 text-xl font-semibold text-gray-800">
              Terms in this set ({flashcards.length})
            </h2>
            <div className="space-y-3">
              {flashcards.map((card) => (
                <Card
                  key={card.flashcardId}
                  className="border-gray-200 bg-white transition-all hover:shadow-md"
                >
                  <CardContent className="p-4">
                    {editingFlashcardId === card.flashcardId ? (
                      <div className="space-y-4">
                        <div className="flex-1 grid grid-cols-1 gap-4 md:grid-cols-2">
                          <div>
                            <label className="text-xs font-medium uppercase tracking-wide text-gray-400">
                              Term
                            </label>
                            <Input
                              value={editTerm}
                              onChange={(e) => setEditTerm(e.target.value)}
                              className="mt-1 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                            />
                          </div>
                          <div>
                            <label className="text-xs font-medium uppercase tracking-wide text-gray-400">
                              Definition
                            </label>
                            <Input
                              value={editDefinition}
                              onChange={(e) =>
                                setEditDefinition(e.target.value)
                              }
                              className="mt-1 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                            />
                          </div>
                        </div>
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            className="border-gray-200 text-gray-500 hover:bg-gray-50"
                            onClick={cancelEditing}
                          >
                            Cancel
                          </Button>
                          <Button
                            size="sm"
                            className="bg-pink-500 text-white hover:bg-pink-600"
                            onClick={() =>
                              saveEditingFlashcard(card.flashcardId)
                            }
                          >
                            Save
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 grid grid-cols-1 gap-4 md:grid-cols-2">
                          <div>
                            <p className="font-medium text-gray-800">
                              {card.term}
                            </p>
                          </div>
                          <div>
                            <p className="text-gray-600">{card.definition}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 text-gray-400 hover:text-pink-500"
                            onClick={() => toggleStar(card.flashcardId)}
                          >
                            <Star
                              className={cn(
                                "h-4 w-4",
                                card.starred &&
                                  "fill-yellow-400 text-yellow-400",
                              )}
                            />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 text-gray-400 hover:text-pink-500"
                            onClick={() => speakText(card.term)}
                          >
                            <Volume2 className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 text-gray-400 hover:text-pink-500"
                            onClick={() => startEditing(card)}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
