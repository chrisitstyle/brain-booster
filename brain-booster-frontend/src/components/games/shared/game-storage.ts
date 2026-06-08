export type GameModeStorageKey =
  | "multiple-choice"
  | "written"
  | "matching"
  | "custom-test";

export function getGameStorageKey(
  setId: number | string,
  mode: GameModeStorageKey,
) {
  return `brain-booster:game:${setId}:${mode}`;
}
