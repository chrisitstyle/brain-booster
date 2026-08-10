import { Flame } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

const weekDays = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export function WeeklyProgressCard() {
  return (
    <Card className="mb-8 border-border bg-card text-card-foreground">
      <CardHeader className="pb-2">
        <CardTitle className="text-lg font-semibold text-card-foreground">
          Weekly Progress
        </CardTitle>
      </CardHeader>

      <CardContent>
        <div className="mb-2 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            5 of 7 days studied
          </span>

          <span className="text-sm font-medium text-pink-500 dark:text-pink-400">
            71%
          </span>
        </div>

        <Progress value={71} className="h-2 bg-muted" />

        <div className="mt-4 flex justify-between">
          {weekDays.map((day, index) => (
            <div key={day} className="flex flex-col items-center gap-2">
              <div
                className={cn(
                  "flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium",
                  index < 5
                    ? "bg-pink-500 text-white"
                    : "bg-muted text-muted-foreground",
                )}
              >
                {index < 5 ? <Flame className="h-4 w-4" /> : null}
              </div>

              <span className="text-xs text-muted-foreground">{day}</span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
