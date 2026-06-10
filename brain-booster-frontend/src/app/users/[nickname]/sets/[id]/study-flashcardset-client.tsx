"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import {
  type Flashcard,
  getFlashcardsBySetId,
  starFlashcard,
  unstarFlashcard,
  updateFlashcardById,
} from "@/api/flashcardService";
import type { FlashcardSet } from "@/api/flashcardSetService";
import { useAuth } from "@/context/AuthContext";

import type { StudyFlashcard } from "./types";
import StudySetHeader from "../components/study-set-header";
import StudySetOptionsDropdown from "../components/study-set-options-dropdown";
import StudyFlashcardCard from "../components/study-flashcard-card";
import StudyControls from "../components/study-controls";
import StudyProgress from "../components/study-progress";
import StudySetAuthorInfo from "../components/study-set-author-info";
import FlashcardTermsList from "../components/flashcard-terms-list";
import StudyVisibilityControls from "../components/study-visibility-controls";
import StudyGamesSection from "../components/study-games-section";

type HiddenFlashcardSide = "terms" | "definitions" | null;

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
  const router = useRouter();

  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [isProgressTrackingEnabled, setIsProgressTrackingEnabled] =
    useState(true);

  const [hiddenFlashcardSide, setHiddenFlashcardSide] =
    useState<HiddenFlashcardSide>(null);

  const [revealedFlashcardIds, setRevealedFlashcardIds] = useState<Set<number>>(
    new Set(),
  );

  const [flashcards, setFlashcards] =
    useState<StudyFlashcard[]>(initialFlashcards);

  const [pendingStarFlashcardIds, setPendingStarFlashcardIds] = useState<
    Set<number>
  >(new Set());

  const [studyFlashcardIds, setStudyFlashcardIds] = useState<number[]>(() =>
    initialFlashcards.map((flashcard) => flashcard.flashcardId),
  );

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

  const areTermsHidden = hiddenFlashcardSide === "terms";
  const areDefinitionsHidden = hiddenFlashcardSide === "definitions";

  const currentFlashcardId = studyFlashcardIds[currentIndex];

  const currentFlashcard = flashcards.find(
    (flashcard) => flashcard.flashcardId === currentFlashcardId,
  );

  useEffect(() => {
    if (!token) return;

    const authToken = token;
    let isCancelled = false;

    async function fetchFlashcardsWithUserStarredStatus() {
      try {
        const flashcardsWithStarredStatus = await getFlashcardsBySetId(
          studySet.setId,
          authToken,
        );

        if (!isCancelled) {
          setFlashcards(flashcardsWithStarredStatus);
          setStudyFlashcardIds(
            flashcardsWithStarredStatus.map(
              (flashcard) => flashcard.flashcardId,
            ),
          );
        }
      } catch (error) {
        console.error("Error fetching flashcards with starred status:", error);
      }
    }

    void fetchFlashcardsWithUserStarredStatus();

    return () => {
      isCancelled = true;
    };
  }, [studySet.setId, token]);

  const toggleHiddenFlashcardSide = (
    sideToToggle: Exclude<HiddenFlashcardSide, null>,
  ) => {
    setHiddenFlashcardSide((previousSide) =>
      previousSide === sideToToggle ? null : sideToToggle,
    );

    setRevealedFlashcardIds(new Set());
  };

  const revealFlashcardText = (flashcardId: number) => {
    setRevealedFlashcardIds((previousRevealedFlashcardIds) => {
      const nextRevealedFlashcardIds = new Set(previousRevealedFlashcardIds);
      nextRevealedFlashcardIds.add(flashcardId);
      return nextRevealedFlashcardIds;
    });
  };

  const handleFlipCard = () => {
    setIsFlipped((previousValue) => !previousValue);
  };

  const handlePreviousCard = () => {
    if (currentIndex > 0) {
      setCurrentIndex((previousIndex) => previousIndex - 1);
      setIsFlipped(false);
    }
  };

  const handleNextCard = () => {
    if (currentIndex < studyFlashcardIds.length - 1) {
      setCurrentIndex((previousIndex) => previousIndex + 1);
      setIsFlipped(false);
    }
  };

  const handleKnownCard = () => {
    if (!currentFlashcard) {
      return;
    }

    if (isProgressTrackingEnabled) {
      setKnownFlashcards((previousKnownFlashcards) => {
        const nextKnownFlashcards = new Set(previousKnownFlashcards);
        nextKnownFlashcards.add(currentFlashcard.flashcardId);
        return nextKnownFlashcards;
      });

      setUnknownFlashcards((previousUnknownFlashcards) => {
        const nextUnknownFlashcards = new Set(previousUnknownFlashcards);
        nextUnknownFlashcards.delete(currentFlashcard.flashcardId);
        return nextUnknownFlashcards;
      });
    }

    handleNextCard();
  };

  const handleUnknownCard = () => {
    if (!currentFlashcard) {
      return;
    }

    if (isProgressTrackingEnabled) {
      setUnknownFlashcards((previousUnknownFlashcards) => {
        const nextUnknownFlashcards = new Set(previousUnknownFlashcards);
        nextUnknownFlashcards.add(currentFlashcard.flashcardId);
        return nextUnknownFlashcards;
      });

      setKnownFlashcards((previousKnownFlashcards) => {
        const nextKnownFlashcards = new Set(previousKnownFlashcards);
        nextKnownFlashcards.delete(currentFlashcard.flashcardId);
        return nextKnownFlashcards;
      });
    }

    handleNextCard();
  };

  const handleShuffleCards = () => {
    setStudyFlashcardIds((previousIds) => {
      const shuffledIds = [...previousIds];

      for (let i = shuffledIds.length - 1; i > 0; i -= 1) {
        const randomIndex = Math.floor(Math.random() * (i + 1));

        [shuffledIds[i], shuffledIds[randomIndex]] = [
          shuffledIds[randomIndex],
          shuffledIds[i],
        ];
      }

      return shuffledIds;
    });

    setCurrentIndex(0);
    setIsFlipped(false);
  };

  const toggleStar = async (flashcardId: number) => {
    if (!token) {
      toast.error("Please log in to star flashcards.");
      return;
    }

    if (pendingStarFlashcardIds.has(flashcardId)) {
      return;
    }

    const flashcardToUpdate = flashcards.find(
      (flashcard) => flashcard.flashcardId === flashcardId,
    );

    if (!flashcardToUpdate) {
      return;
    }

    const nextStarredValue = !flashcardToUpdate.starred;

    setPendingStarFlashcardIds((previousIds) => {
      const nextIds = new Set(previousIds);
      nextIds.add(flashcardId);
      return nextIds;
    });

    setFlashcards((previousFlashcards) =>
      previousFlashcards.map((flashcard) =>
        flashcard.flashcardId === flashcardId
          ? { ...flashcard, starred: nextStarredValue }
          : flashcard,
      ),
    );

    try {
      const updatedFlashcard = nextStarredValue
        ? await starFlashcard(flashcardId, token)
        : await unstarFlashcard(flashcardId, token);

      setFlashcards((previousFlashcards) =>
        previousFlashcards.map((flashcard) =>
          flashcard.flashcardId === flashcardId
            ? { ...flashcard, starred: updatedFlashcard.starred }
            : flashcard,
        ),
      );
    } catch (error) {
      console.error("Error updating flashcard starred status:", error);

      setFlashcards((previousFlashcards) =>
        previousFlashcards.map((flashcard) =>
          flashcard.flashcardId === flashcardId
            ? { ...flashcard, starred: !nextStarredValue }
            : flashcard,
        ),
      );

      toast.error("Failed to update flashcard starred status.");
    } finally {
      setPendingStarFlashcardIds((previousIds) => {
        const nextIds = new Set(previousIds);
        nextIds.delete(flashcardId);
        return nextIds;
      });
    }
  };

  const speakText = (text: string) => {
    if ("speechSynthesis" in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      speechSynthesis.speak(utterance);
    }
  };

  const startEditing = (flashcard: StudyFlashcard) => {
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
      await updateFlashcardById(
        flashcardId,
        { term: editTerm, definition: editDefinition },
        token,
      );

      setFlashcards((previousFlashcards) =>
        previousFlashcards.map((flashcard) =>
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

  const handleEditSet = () => {
    router.push(`/users/${nickname}/sets/${studySet.setId}/edit`);
  };

  const handleMakeCopy = () => {
    toast.info("Make copy option coming soon.");
  };

  const handlePrint = () => {
    window.print();
  };

  const handleDeleteSet = () => {
    toast.info("Delete option coming soon.");
  };

  return (
    <div className="min-h-screen bg-gray-50 print:bg-white">
      <div className="container mx-auto px-4 pt-6 pb-28 print:px-0 print:py-0">
        <StudySetHeader
          nickname={nickname}
          setName={studySet.setName}
          description={studySet.description}
        />

        <StudyGamesSection nickname={nickname} setId={studySet.setId} />

        {flashcards.length > 0 ? (
          <div className="mx-auto max-w-2xl print:hidden">
            <div className="mb-3 flex justify-end">
              <StudySetOptionsDropdown
                onEdit={handleEditSet}
                onMakeCopy={handleMakeCopy}
                onPrint={handlePrint}
                onDelete={handleDeleteSet}
              />
            </div>

            <StudyFlashcardCard
              currentFlashcard={currentFlashcard}
              isFlipped={isFlipped}
              onFlip={handleFlipCard}
              onSpeak={speakText}
            />

            <StudyControls
              currentIndex={currentIndex}
              totalCards={studyFlashcardIds.length}
              isProgressTrackingEnabled={isProgressTrackingEnabled}
              onToggleProgressTracking={() =>
                setIsProgressTrackingEnabled((previousValue) => !previousValue)
              }
              onUnknown={handleUnknownCard}
              onPrevious={handlePreviousCard}
              onNext={handleNextCard}
              onKnown={handleKnownCard}
              onShuffle={handleShuffleCards}
            />

            {isProgressTrackingEnabled && (
              <StudyProgress
                knownCount={knownFlashcards.size}
                unknownCount={unknownFlashcards.size}
                totalCount={flashcards.length}
              />
            )}

            <StudySetAuthorInfo
              nickname={studySet.user.nickname}
              createdAt={studySet.createdAt}
            />
          </div>
        ) : (
          <div className="py-8 text-center text-gray-500">
            This study set does not have any flashcards yet.
          </div>
        )}

        {flashcards.length > 0 && (
          <>
            <StudyVisibilityControls
              areTermsHidden={areTermsHidden}
              areDefinitionsHidden={areDefinitionsHidden}
              onToggleTerms={() => toggleHiddenFlashcardSide("terms")}
              onToggleDefinitions={() =>
                toggleHiddenFlashcardSide("definitions")
              }
            />

            <FlashcardTermsList
              flashcards={flashcards}
              editingFlashcardId={editingFlashcardId}
              editTerm={editTerm}
              editDefinition={editDefinition}
              areTermsHidden={areTermsHidden}
              areDefinitionsHidden={areDefinitionsHidden}
              revealedFlashcardIds={revealedFlashcardIds}
              pendingStarFlashcardIds={pendingStarFlashcardIds}
              onRevealFlashcardText={revealFlashcardText}
              onEditTermChange={setEditTerm}
              onEditDefinitionChange={setEditDefinition}
              onStartEditing={startEditing}
              onCancelEditing={cancelEditing}
              onSaveEditing={saveEditingFlashcard}
              onToggleStar={(flashcardId) => {
                void toggleStar(flashcardId);
              }}
              onSpeak={speakText}
            />
          </>
        )}
      </div>
    </div>
  );
}
