/**
 * The name of the custom browser event dispatched after the user's
 * profile data has been updated, such as the nickname or email address.
 *
 * Components such as Navbar or ProfileDashboard can listen for this event
 * and fetch the latest user data again.
 */
export const PROFILE_UPDATED_EVENT = "brainbooster-profile-updated";

/**
 * Notifies components in the current browser tab that the user's
 * profile data has been updated.
 *
 * The function dispatches the {@link PROFILE_UPDATED_EVENT} event
 * on the window object. It does nothing during server-side rendering,
 * because window is only available in the browser.
 *
 * @example
 * ```ts
 * await updateNickname("newNickname", token);
 * notifyProfileUpdated();
 * ```
 */
export function notifyProfileUpdated(): void {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(new Event(PROFILE_UPDATED_EVENT));
}
