"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { ChevronDown, Menu, Search } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { useMobile } from "@/hooks/use-mobile";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface NavItem {
  title: string;
  href: string;
  disabled?: boolean;
  children?: NavItem[];
}

export function Navbar({
  className,
  items,
}: React.HTMLAttributes<HTMLElement> & { items?: NavItem[] }) {
  const pathname = usePathname();
  const isMobile = useMobile();
  const [isOpen, setIsOpen] = React.useState(false);

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
      title: "Create",
      href: "/create",
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
        className
      )}
    >
      <div className="container flex h-16 items-center justify-between">
        <div className="flex items-center gap-6 md:gap-10">
          <Link href="/" className="flex items-center space-x-2">
            <span className="inline-block text-xl font-bold text-pink-500">
              BrainBooster
            </span>
          </Link>
        </div>

        {!isMobile && (
          <nav className="flex items-center gap-6">
            {navItems.map((item, index) => (
              <React.Fragment key={index}>
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
                          <Link href={child.href} className="w-full">
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
                      item.disabled && "cursor-not-allowed opacity-80"
                    )}
                  >
                    {item.title}
                  </Link>
                )}
              </React.Fragment>
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
              />
            </div>
          )}

          {!isMobile ? (
            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                size="sm"
                className="text-gray-600 hover:text-pink-500"
              >
                Login
              </Button>
              <Button
                size="sm"
                className="rounded-full bg-pink-500 hover:bg-pink-600 text-white"
              >
                Sign up free
              </Button>
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
                            item.disabled && "cursor-not-allowed opacity-80"
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
                    <Button
                      variant="ghost"
                      size="sm"
                      className="justify-start text-gray-600 hover:text-pink-500"
                    >
                      Login
                    </Button>
                    <Button
                      size="sm"
                      className="bg-pink-500 hover:bg-pink-600 text-white"
                    >
                      Sign up free
                    </Button>
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
