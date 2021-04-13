export default interface PushNotification {
  registration(
    success?: (token: string) => void,
    error?: (error: string) => void
  ): Promise<any>; 
}