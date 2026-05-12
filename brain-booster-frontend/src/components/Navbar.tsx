"use client";

import { useState, Fragment } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  ChevronDown,
  Menu,
  Search,
  User,
  Settings,
  BookOpen,
  FolderOpen,
  LogOut,
} from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetTrigger,
  SheetTitle,
  SheetDescription,
  SheetHeader,
} from "@/components/ui/sheet";
import { useMobile } from "@/hooks/use-mobile";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
  DropdownMenuLabel,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

import { useAuth } from "@/context/AuthContext";

interface NavItem {
  title: string;
  href: string;
  disabled?: boolean;
  children?: NavItem[];
}

export default function Navbar({
  className,
  items,
}: React.HTMLAttributes<HTMLElement> & { items?: NavItem[] }) {
  const pathname = usePathname();
  const isMobile = useMobile();
  const [isOpen, setIsOpen] = useState(false);

  const { isAuthenticated, logout } = useAuth();

  const defaultItems: NavItem[] = [
    {
      title: "Study",
      href: "#",
      children: [
        { title: "Flashcards", href: "/flashcards" },
        { title: "Learn", href: "/learn" },
        { title: "Test", href: "/test" },
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
        { title: "For students", href: "/students" },
        { title: "For teachers", href: "/teachers" },
      ],
    },
    {
      title: "Subjects",
      href: "/subjects",
    },
  ];

  const navItems = items || defaultItems;

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
            {navItems.map((item, index) => (
              <Fragment key={index}>
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
                      {item.children.map((child, childIndex) => (
                        <DropdownMenuItem key={childIndex} asChild>
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
                        ? "text-pink-500 font-semibold"
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
              {/* logic for desktop version */}
              {isAuthenticated ? (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex items-center gap-2 rounded-full border border-gray-200 px-2 py-1 hover:border-pink-300 hover:bg-pink-50 transition-all duration-200 h-10"
                    >
                      <Menu className="h-4 w-4 text-gray-500" />
                      <Avatar className="h-7 w-7 border-2 border-pink-200">
                        <AvatarFallback className="bg-gradient-to-br from-pink-400 to-pink-600 text-white text-xs font-semibold">
                          JD
                        </AvatarFallback>
                      </Avatar>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent
                    align="end"
                    className="w-56 mt-2 p-2 rounded-xl shadow-lg border-gray-100"
                  >
                    <DropdownMenuLabel className="px-3 py-2">
                      <div className="flex items-center gap-3">
                        <Avatar className="h-10 w-10 border-2 border-pink-200">
                          <AvatarFallback className="bg-gradient-to-br from-pink-400 to-pink-600 text-white font-semibold">
                            JD
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex flex-col">
                          <span className="text-sm font-semibold text-gray-800">
                            John Doe
                          </span>
                          <span className="text-xs text-gray-500">
                            @johndoe
                          </span>
                        </div>
                      </div>
                    </DropdownMenuLabel>
                    <DropdownMenuSeparator className="my-2" />
                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile"
                        className="flex items-center gap-3 px-3 py-2 cursor-pointer rounded-lg hover:bg-pink-50 transition-colors"
                      >
                        <User className="h-4 w-4 text-gray-500" />
                        <span className="font-medium text-gray-700">
                          Profile
                        </span>
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile?tab=sets"
                        className="flex items-center gap-3 px-3 py-2 cursor-pointer rounded-lg hover:bg-pink-50 transition-colors"
                      >
                        <BookOpen className="h-4 w-4 text-gray-500" />
                        <span className="font-medium text-gray-700">
                          My Sets
                        </span>
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuItem asChild>
                      <Link
                        href="/profile?tab=folders"
                        className="flex items-center gap-3 px-3 py-2 cursor-pointer rounded-lg hover:bg-pink-50 transition-colors"
                      >
                        <FolderOpen className="h-4 w-4 text-gray-500" />
                        <span className="font-medium text-gray-700">
                          My Folders
                        </span>
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator className="my-2" />
                    <DropdownMenuItem asChild>
                      <Link
                        href="/settings"
                        className="flex items-center gap-3 px-3 py-2 cursor-pointer rounded-lg hover:bg-pink-50 transition-colors"
                      >
                        <Settings className="h-4 w-4 text-gray-500" />
                        <span className="font-medium text-gray-700">
                          Settings
                        </span>
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator className="my-2" />
                    <DropdownMenuItem
                      onClick={logout}
                      className="flex items-center gap-3 px-3 py-2 cursor-pointer rounded-lg text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors focus:text-red-600 focus:bg-red-50"
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
                    className="rounded-full bg-pink-500 hover:bg-pink-600 text-white"
                    asChild
                  >
                    <Link href="/signup">Sign up free</Link>
                  </Button>
                </>
              )}
            </div>
          ) : (
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden">
                  <Menu className="h-5 w-5" />
                  <span className="sr-only">Toggle menu</span>
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-[300px]">
                <SheetHeader>
                  <SheetTitle className="sr-only">
                    Mobile Navigation Menu
                  </SheetTitle>
                  <SheetDescription className="sr-only">
                    Use the menu to navigate through the main sections of the
                    app.
                  </SheetDescription>
                </SheetHeader>

                <div className="flex flex-col gap-6 py-4">
                  <div className="relative">
                    <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
                    <Input
                      type="search"
                      placeholder="Search..."
                      className="w-full rounded-full bg-gray-100 pl-9 focus-visible:ring-pink-500"
                    />
                  </div>

                  <nav className="flex flex-col gap-4">
                    {navItems.map((item, index) => (
                      <div key={index} className="flex flex-col gap-2">
                        <Link
                          href={
                            item.disabled || item.children ? "#" : item.href
                          }
                          className={cn(
                            "text-base font-medium transition-colors",
                            item.href === pathname
                              ? "text-pink-500 font-semibold"
                              : "text-gray-600",
                            item.disabled && "cursor-not-allowed opacity-80",
                          )}
                          onClick={() => !item.children && setIsOpen(false)}
                        >
                          {item.title}
                        </Link>

                        {item.children && (
                          <div className="ml-4 flex flex-col gap-2 border-l border-gray-200 pl-4">
                            {item.children.map((child, childIndex) => (
                              <Link
                                key={childIndex}
                                href={child.href}
                                className="text-sm text-gray-500 hover:text-pink-500"
                                onClick={() => setIsOpen(false)}
                              >
                                {child.title}
                              </Link>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </nav>

                  <div className="mt-4 flex flex-col gap-2">
                    {/* logic for mobile version */}
                    {isAuthenticated ? (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="justify-start text-gray-600 hover:text-pink-500"
                          asChild
                        >
                          <Link
                            href="/profile"
                            onClick={() => setIsOpen(false)}
                          >
                            Profile
                          </Link>
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          className="justify-start text-gray-600 border-gray-300 hover:bg-red-50 hover:text-red-500 hover:border-red-200"
                          onClick={() => {
                            logout();
                            setIsOpen(false);
                          }}
                        >
                          Logout
                        </Button>
                      </>
                    ) : (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="justify-start text-gray-600 hover:text-pink-500"
                          asChild
                        >
                          <Link href="/login" onClick={() => setIsOpen(false)}>
                            Login
                          </Link>
                        </Button>
                        <Button
                          size="sm"
                          className="bg-pink-500 hover:bg-pink-600 text-white"
                          asChild
                        >
                          <Link href="/signup" onClick={() => setIsOpen(false)}>
                            Sign up free
                          </Link>
                        </Button>
                      </>
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
