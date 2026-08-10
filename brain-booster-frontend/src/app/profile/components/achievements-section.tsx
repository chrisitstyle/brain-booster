import { Flame, Trophy, Zap } from "lucide-react";

import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

const achievements = [
  {
    icon: Flame,
    label: "7 Day Streak",
    value: "7",
    color: "text-orange-500",
  },
  {
    icon: Trophy,
    label: "Sets Mastered",
    value: "12",
    color: "text-yellow-500",
  },
  {
    icon: Zap,
    label: "Terms Learned",
    value: "847",
    color: "text-pink-500",
  },
];

export function AchievementsSection() {
  return (
    <div className="mb-8 grid gap-4 md:grid-cols-3">
      {achievements.map((achievement) => (
        <Card
          key={achievement.label}
          className="border-border bg-card text-card-foreground"
        >
          <CardContent className="flex items-center gap-4 p-6">
            <div className={cn("rounded-full bg-muted p-3", achievement.color)}>
              <achievement.icon className="h-6 w-6" />
            </div>

            <div>
              <p className="text-2xl font-bold text-card-foreground">
                {achievement.value}
              </p>

              <p className="text-sm text-muted-foreground">
                {achievement.label}
              </p>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
