export default interface PushNotification {
  registration(
    success?: (token: string) => void,
    error?: (error: string) => void
  ): void;
  tapped(
    success?: (payload: string) => void,
    error?: (error: string) => void
  ): void;
}