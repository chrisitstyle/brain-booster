"use client";

import { Fragment, useEffect, useMemo, useState } from "react";
import type { HTMLAttributes } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BookOpen,
  ChevronDown,
  FolderOpen,
  Loader2,
  LogOut,
  Menu,
  Search,
  Settings,
  TrendingUp,
  User,
} from "lucide-react";

import { getCurrentUser, type UserDTO } from "@/api/profileService";
import { useAuth } from "@/context/AuthContext";
import { useMobile } from "@/hooks/use-mobile";
import { cn } from "@/lib/utils";
import { PROFILE_UPDATED_EVENT } from "@/utils/profile-events";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";

interface NavItem {
  title: string;
  href: string;
  disabled?: boolean;
  children?: NavItem[];
}

interface NavbarProps extends HTMLAttributes<HTMLElement> {
  items?: NavItem[];
}

const DEFAULT_ITEMS: NavItem[] = [
  {
    title: "Study",
    href: "#",
    children: [
      {
        title: "Flashcards",
        href: "/flashcards",
      },
      {
        title: "Learn",
        href: "/learn",
      },
      {
        title: "Test",
        href: "/test",
      },
    ],
  },
  {
    title: "Create Set",
    href: "/create-set",
  },
  {
    title: "Solutions",
    href: "#",
    children: [
      {
        title: "For students",
        href: "/students",
      },
      {
        title: "For teachers",
        href: "/teachers",
      },
    ],
  },
  {
    title: "Subjects",
    href: "/subjects",
  },
];

export default function Navbar({ className, items }: NavbarProps) {
  const pathname = usePathname();
  const isMobile = useMobile();

  const { token, isAuthenticated, isAuthLoading, logout } = useAuth();

  const [isOpen, setIsOpen] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [loadedUserToken, setLoadedUserToken] = useState<string | null>(null);

  const navItems = items ?? DEFAULT_ITEMS;

  const isUserLoading = Boolean(
    token && isAuthenticated && loadedUserToken !== token,
  );

  useEffect(() => {
    if (!token || !isAuthenticated) {
      setCurrentUser(null);
      setLoadedUserToken(null);
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    void getCurrentUser(requestToken)
      .then((user) => {
        if (isCancelled) {
          return;
        }

        setCurrentUser(user);
      })
      .catch((error: unknown) => {
        if (isCancelled) {
          return;
        }

        console.error("Failed to load navbar user:", error);

        setCurrentUser(null);
      })
      .finally(() => {
        if (!isCancelled) {
          setLoadedUserToken(requestToken);
        }
      });

    return () => {
      isCancelled = true;
    };
  }, [token, isAuthenticated]);

  useEffect(() => {
    if (!token || !isAuthenticated) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    function handleProfileUpdated() {
      void getCurrentUser(requestToken)
        .then((user) => {
          if (isCancelled) {
            return;
          }

          setCurrentUser(user);
        })
        .catch((error: unknown) => {
          if (isCancelled) {
            return;
          }

          console.error("Failed to refresh navbar user:", error);
        })
        .finally(() => {
          if (!isCancelled) {
            setLoadedUserToken(requestToken);
          }
        });
    }

    window.addEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      isCancelled = true;

      window.removeEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, [token, isAuthenticated]);

  const avatarFallback = useMemo(() => {
    const firstLetter = currentUser?.nickname.trim().charAt(0);

    return firstLetter ? firstLetter.toUpperCase() : "?";
  }, [currentUser]);

  const displayedNickname = currentUser?.nickname ?? "User";
  const displayedEmail = currentUser?.email ?? "";

  function refreshCurrentUser() {
    if (!token || !isAuthenticated) {
      return;
    }

    const requestToken = token;

    void getCurrentUser(requestToken)
      .then((user) => {
        setCurrentUser(user);
      })
      .catch((error: unknown) => {
        console.error("Failed to refresh navbar user:", error);

        setCurrentUser(null);
      })
      .finally(() => {
        setLoadedUserToken(requestToken);
      });
  }

  function handleMobileMenuChange(open: boolean) {
    setIsOpen(open);

    if (open && isAuthenticated) {
      refreshCurrentUser();
    }
  }

  function handleLogout() {
    setCurrentUser(null);
    setLoadedUserToken(null);
    setIsOpen(false);

    logout();
  }

  return (
    <header
      className={cn(
        "sticky top-0 z-40 w-full border-b bg-white shadow-sm",
        className,
      )}
    >
      <div className="container flex h-16 items-center justify-between">
        <div className="flex items-center gap-6 md:gap-10">
          <Link href="/" className="flex items-center space-x-2">
            <span className="inline-block text-xl font-bold text-pink-500">
              BrainBooster
            </span>

            <span className="sr-only">Home</span>
          </Link>
        </div>

        {!isMobile && (
          <nav className="flex items-center gap-6">
            {navItems.map((item) => (
              <Fragment key={`${item.title}-${item.href}`}>
                {item.children ? (
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant="ghost"
                        className="flex items-center gap-1 text-gray-600 hover:text-pink-500"
                      >
                        {item.title}

                        <ChevronDown className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>

                    <DropdownMenuContent align="start" className="w-48">
                      {item.children.map((child) => (
                        <DropdownMenuItem
                          key={`${child.title}-${child.href}`}
                          asChild
                        >
                          <Link
                            href={child.href}
                            className="w-full cursor-pointer"
                          >
                            {child.title}
                          </Link>
                        </DropdownMenuItem>
                      ))}
                    </DropdownMenuContent>
                  </DropdownMenu>
                ) : (
                  <Link
                    href={item.disabled ? "#" : item.href}
                    className={cn(
                      "text-sm font-medium transition-colors hover:text-pink-500",
                      item.href === pathname
                        ? "font-semibold text-pink-500"
                        : "text-gray-600",
                      item.disabled && "cursor-not-allowed opacity-80",
                    )}
                  >
                    {item.title}
                  </Link>
                )}
              </Fragment>
            ))}
          </nav>
        )}

        <div className="flex items-center gap-4">
          {!isMobile && (
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />

              <Input
                type="search"
                placeholder="Search..."
                className="w-[200px] rounded-full bg-gray-100 pl-9 focus-visible:ring-pink-500"
                aria-label="Search"
              />
            </div>
          )}

          {!isMobile ? (
            <div className="flex items-center gap-2">
              {isAuthLoading ? (
                <div className="h-10 w-16 rounded-full bg-gray-100" />
              ) : isAuthenticated ? (
                <DropdownMenu
                  onOpenChange={(open) => {
                    if (open) {
                      refreshCurrentUser();
                    }
                  }}
                >
                  <DropdownMenuTrigger asChild>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex h-10 items-center gap-2 rounded-full border border-gray-200 px-2 py-1 transition-all duration-200 hover:border-pink-300 hover:bg-pink-50"
                      aria-label="Open profile menu"
                    >
                      <Menu className="h-4 w-4 text-gray-500" />

                      <Avatar className="h-7 w-7 border-2 border-pink-200">
                        <AvatarFallback className="bg-linear-to-br from-pink-400 to-pink-600 text-xs font-semibold text-white">
                          {isUserLoading && !currentUser ? (
                            <Loader2 className="h-3.5 w-3.5 animate-spin" />
                          ) : (
                            avatarFallback
                          )}
                        </AvatarFallback>
                      </Avatar>
                    </Button>
                  </DropdownMenuTrigger>

                  <DropdownMenuContent
                    align="end"
                    className="mt-2 w-64 rounded-xl border-gray-100 p-2 shadow-lg"
                  >
                    <DropdownMenuLabel className="px-3 py-2">
                      <div className="flex min-w-0 items-center gap-3">
                        <Avatar className="h-10 w-10 shrink-0 border-2 border-pink-200">
                          <AvatarFallback className="bg-linear-to-br from-pink-400 to-pink-600 font-semibold text-white">
                            {isUserLoading && !currentUser ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              avatarFallback
                            )}
                          </AvatarFallback>
                        </Avatar>

                        <div className="flex min-w-0 flex-col">
                          <span className="truncate text-sm font-semibold text-gray-800">
                            {isUserLoading && !currentUser
                              ? "Loading..."
                              : displayedNickname}
                          </span>

                          {displayedEmail && (
                            <span className="truncate text-xs font-normal text-gray-500">
                              {displayedEmail}
                            </span>
                          )}
                        </div>
                      </div>
                    </DropdownMenuLabel>

                    <DropdownMenuSeparator className="my-2" />

                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile"
                        className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-pink-50"
                      >
                        <User className="h-4 w-4 text-gray-500" />

                        <span className="font-medium text-gray-700">
                          Profile
                        </span>
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile/sets"
                        className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-pink-50"
                      >
                        <BookOpen className="h-4 w-4 text-gray-500" />

                        <span className="font-medium text-gray-700">
                          My Sets
                        </span>
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile/folders"
                        className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-pink-50"
                      >
                        <FolderOpen className="h-4 w-4 text-gray-500" />

                        <span className="font-medium text-gray-700">
                          My Folders
                        </span>
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile/stats"
                        className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-pink-50"
                      >
                        <TrendingUp className="h-4 w-4 text-gray-500" />

                        <span className="font-medium text-gray-700">
                          My Stats
                        </span>
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuSeparator className="my-2" />

                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile/settings"
                        className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-pink-50"
                      >
                        <Settings className="h-4 w-4 text-gray-500" />

                        <span className="font-medium text-gray-700">
                          Settings
                        </span>
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuSeparator className="my-2" />

                    <DropdownMenuItem
                      onClick={handleLogout}
                      className="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 text-red-500 transition-colors hover:bg-red-50 hover:text-red-600 focus:bg-red-50 focus:text-red-600"
                    >
                      <LogOut className="h-4 w-4" />

                      <span className="font-medium">Logout</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ) : (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-gray-600 hover:text-pink-500"
                    asChild
                  >
                    <Link href="/login">Login</Link>
                  </Button>

                  <Button
                    size="sm"
                    className="rounded-full bg-pink-500 text-white hover:bg-pink-600"
                    asChild
                  >
                    <Link href="/signup">Sign up free</Link>
                  </Button>
                </>
              )}
            </div>
          ) : (
            <Sheet open={isOpen} onOpenChange={handleMobileMenuChange}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden">
                  <Menu className="h-5 w-5" />

                  <span className="sr-only">Toggle menu</span>
                </Button>
              </SheetTrigger>

              <SheetContent
                side="right"
                className="w-[min(360px,calc(100vw-1rem))] overflow-y-auto p-0 sm:max-w-sm"
              >
                <SheetHeader className="sr-only">
                  <SheetTitle>Mobile Navigation Menu</SheetTitle>

                  <SheetDescription>
                    Use the menu to navigate through the main sections of the
                    app.
                  </SheetDescription>
                </SheetHeader>

                <div className="flex min-h-full flex-col px-5 pb-6 pt-10">
                  <div className="mb-6 pr-8">
                    <Link
                      href="/"
                      className="inline-block text-xl font-bold text-pink-500"
                      onClick={() => setIsOpen(false)}
                    >
                      BrainBooster
                    </Link>
                  </div>

                  {isAuthenticated && (
                    <div className="mb-5 flex min-w-0 items-center gap-3 rounded-2xl border border-pink-100 bg-pink-50/60 p-3">
                      <Avatar className="h-11 w-11 shrink-0 border-2 border-pink-200">
                        <AvatarFallback className="bg-linear-to-br from-pink-400 to-pink-600 font-semibold text-white">
                          {isUserLoading && !currentUser ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            avatarFallback
                          )}
                        </AvatarFallback>
                      </Avatar>

                      <div className="flex min-w-0 flex-col">
                        <span className="truncate text-sm font-semibold text-gray-800">
                          {isUserLoading && !currentUser
                            ? "Loading..."
                            : displayedNickname}
                        </span>

                        {displayedEmail && (
                          <span className="truncate text-xs text-gray-500">
                            {displayedEmail}
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  <div className="relative mb-6">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

                    <Input
                      type="search"
                      placeholder="Search..."
                      className="h-11 w-full rounded-full border-gray-200 bg-gray-50 pl-10 pr-4 focus-visible:ring-2 focus-visible:ring-pink-500"
                      aria-label="Search"
                    />
                  </div>

                  <nav className="space-y-2">
                    {navItems.map((item) => (
                      <div
                        key={`${item.title}-${item.href}`}
                        className="space-y-1"
                      >
                        {item.children ? (
                          <>
                            <div className="rounded-lg px-3 py-2 text-sm font-semibold text-gray-800">
                              {item.title}
                            </div>

                            <div className="space-y-1 pl-3">
                              {item.children.map((child) => (
                                <Link
                                  key={`${child.title}-${child.href}`}
                                  href={child.href}
                                  className={cn(
                                    "block rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                                    child.href === pathname
                                      ? "bg-pink-50 text-pink-600"
                                      : "text-gray-500 hover:bg-gray-50 hover:text-pink-500",
                                  )}
                                  onClick={() => setIsOpen(false)}
                                >
                                  {child.title}
                                </Link>
                              ))}
                            </div>
                          </>
                        ) : (
                          <Link
                            href={item.disabled ? "#" : item.href}
                            aria-disabled={item.disabled}
                            className={cn(
                              "block rounded-lg px-3 py-2 text-sm font-semibold transition-colors",
                              item.href === pathname
                                ? "bg-pink-50 text-pink-600"
                                : "text-gray-700 hover:bg-gray-50 hover:text-pink-500",
                              item.disabled && "cursor-not-allowed opacity-60",
                            )}
                            onClick={() => {
                              if (!item.disabled) {
                                setIsOpen(false);
                              }
                            }}
                          >
                            {item.title}
                          </Link>
                        )}
                      </div>
                    ))}
                  </nav>

                  <div className="mt-8 border-t border-gray-100 pt-5">
                    {isAuthLoading ? (
                      <div className="h-10 w-full rounded-xl bg-gray-100" />
                    ) : isAuthenticated ? (
                      <div className="space-y-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start gap-2 rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile"
                            onClick={() => setIsOpen(false)}
                          >
                            <User className="h-4 w-4" />
                            Profile
                          </Link>
                        </Button>

                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start gap-2 rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile/sets"
                            onClick={() => setIsOpen(false)}
                          >
                            <BookOpen className="h-4 w-4" />
                            My Sets
                          </Link>
                        </Button>

                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start gap-2 rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile/folders"
                            onClick={() => setIsOpen(false)}
                          >
                            <FolderOpen className="h-4 w-4" />
                            My Folders
                          </Link>
                        </Button>

                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start gap-2 rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile/stats"
                            onClick={() => setIsOpen(false)}
                          >
                            <TrendingUp className="h-4 w-4" />
                            My Stats
                          </Link>
                        </Button>

                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start gap-2 rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile/settings"
                            onClick={() => setIsOpen(false)}
                          >
                            <Settings className="h-4 w-4" />
                            Settings
                          </Link>
                        </Button>

                        <Button
                          variant="outline"
                          size="sm"
                          className="mt-3 h-10 w-full justify-start gap-2 rounded-lg border-gray-200 text-gray-600 hover:border-red-200 hover:bg-red-50 hover:text-red-500"
                          onClick={handleLogout}
                        >
                          <LogOut className="h-4 w-4" />
                          Logout
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-10 w-full justify-start rounded-lg px-3 text-gray-600 hover:bg-gray-50 hover:text-pink-500"
                          asChild
                        >
                          <Link href="/login" onClick={() => setIsOpen(false)}>
                            Login
                          </Link>
                        </Button>

                        <Button
                          size="sm"
                          className="h-11 w-full rounded-xl bg-pink-500 text-white hover:bg-pink-600"
                          asChild
                        >
                          <Link href="/signup" onClick={() => setIsOpen(false)}>
                            Sign up free
                          </Link>
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
              </SheetContent>
            </Sheet>
          )}
        </div>
      </div>
    </header>
  );
}
