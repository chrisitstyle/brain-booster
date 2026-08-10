import { useEffect, useState } from "react";
import { toast } from "sonner";

import { getMyFolders, type Folder as FolderDTO } from "@/api/folderService";
import { getCurrentUser, type UserDTO } from "@/api/profileService";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { PROFILE_UPDATED_EVENT } from "@/utils/profile-events";

export interface StudySet {
  id: string;
  title: string;
  description: string;
  termCount: number;
  author: string;
  lastStudied?: string;
}

interface FlashcardSetDTO {
  setId: number;
  user: {
    nickname: string;
  };
  setName: string;
  description: string;
  createdAt: string;
  termCount: number;
}

export interface DashboardFolder {
  id: string;
  title: string;
  description: string;
  setCount: number;
}

interface ProfileDashboardData {
  user: UserDTO;
  sets: StudySet[];
  folders: DashboardFolder[];
}

async function getProfileDashboardData(
  token: string,
): Promise<ProfileDashboardData> {
  const user = await getCurrentUser(token);

  const [setsData, foldersData] = await Promise.all([
    getUserFlashcardSetsByUserId(user.userId, token),
    getMyFolders(token),
  ]);

  const formattedSets: StudySet[] = setsData.map((set: FlashcardSetDTO) => ({
    id: set.setId.toString(),
    title: set.setName,
    description: set.description,
    termCount: set.termCount,
    author: set.user.nickname || user.nickname,
    lastStudied: new Date(set.createdAt).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    }),
  }));

  const formattedFolders: DashboardFolder[] = foldersData.map(
    (folder: FolderDTO) => ({
      id: folder.folderId.toString(),
      title: folder.name,
      description: folder.description ?? "",
      setCount: folder.setCount,
    }),
  );

  return {
    user,
    sets: formattedSets,
    folders: formattedFolders,
  };
}

export function useProfileDashboardData(token: string | null) {
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [sets, setSets] = useState<StudySet[]>([]);
  const [folders, setFolders] = useState<DashboardFolder[]>([]);
  const [loadedContentToken, setLoadedContentToken] = useState<string | null>(
    null,
  );

  const isContentLoading = Boolean(token && loadedContentToken !== token);

  useEffect(() => {
    if (!token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    async function loadDashboardData({
      clearOnError,
      markAsLoaded,
    }: {
      clearOnError: boolean;
      markAsLoaded: boolean;
    }) {
      try {
        const {
          user,
          sets: loadedSets,
          folders: loadedFolders,
        } = await getProfileDashboardData(requestToken);

        if (isCancelled) {
          return;
        }

        setCurrentUser(user);
        setSets(loadedSets);
        setFolders(loadedFolders);
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error loading profile content:", error);

        if (clearOnError) {
          setCurrentUser(null);
          setSets([]);
          setFolders([]);

          toast.error("Failed to load profile content.");
        } else {
          toast.error("Failed to refresh profile content.");
        }
      } finally {
        if (!isCancelled && markAsLoaded) {
          setLoadedContentToken(requestToken);
        }
      }
    }

    void loadDashboardData({
      clearOnError: true,
      markAsLoaded: true,
    });

    function handleProfileUpdated() {
      void loadDashboardData({
        clearOnError: false,
        markAsLoaded: false,
      });
    }

    window.addEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      isCancelled = true;

      window.removeEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, [token]);

  return {
    currentUser,
    sets,
    setSets,
    folders,
    setFolders,
    isContentLoading,
  };
}
