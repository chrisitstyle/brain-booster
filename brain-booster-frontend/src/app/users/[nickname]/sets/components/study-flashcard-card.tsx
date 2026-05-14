"use client";

import { Volume2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

import type { StudyFlashcard } from "../[id]/types";

interface StudyFlashcardCardProps {
  currentFlashcard?: StudyFlashcard;
  isFlipped: boolean;
  onFlip: () => void;
  onSpeak: (text: string) => void;
}

export default function StudyFlashcardCard({
  currentFlashcard,
  isFlipped,
  onFlip,
  onSpeak,
}: StudyFlashcardCardProps) {
  return (
    <div className="perspective-1000 mb-4 cursor-pointer" onClick={onFlip}>
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
        <Card
          className="absolute inset-0 flex items-center justify-center border-gray-200 bg-white p-8 shadow-lg backface-hidden"
          style={{ backfaceVisibility: "hidden" }}
        >
          <Button
            variant="ghost"
            size="icon"
            className="absolute top-4 right-4 h-10 w-10 rounded-full text-gray-400 hover:bg-pink-50 hover:text-pink-500"
            onClick={(event) => {
              event.stopPropagation();
              onSpeak(currentFlashcard?.term ?? "");
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
            onClick={(event) => {
              event.stopPropagation();
              onSpeak(currentFlashcard?.definition ?? "");
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
  );
}
