import { nanoid } from "nanoid";

export enum MessageStatus {
  PROCESSING_AUDIO = "PROCESSING_AUDIO",
  PENDING = "PENDING",
  SENT = "SENT",
}
export class Message {
  chatId: string;
  messageId?: string;
  message: string;
  createdAt: number;
  username: string;
  status: MessageStatus;
  hasAudio?: boolean;

  constructor({
    chatId,
    message,
    username,
    status,
    hasAudio,
    messageId = nanoid(),
  }: Omit<Message, "createdAt">) {
    this.chatId = chatId;
    this.message = message;
    this.username = username;
    this.status = status;
    this.createdAt = +new Date();
    this.hasAudio = hasAudio;
    this.messageId = messageId;
  }
}
