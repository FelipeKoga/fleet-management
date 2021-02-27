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
  recipient?: string;
  duration?: number;

  constructor({
    chatId,
    message,
    username,
    status,
    hasAudio,
    messageId = nanoid(),
    recipient = "",
    duration = null,
  }: Omit<Message, "createdAt">) {
    this.chatId = chatId;
    this.message = message;
    this.username = username;
    this.status = status;
    this.createdAt = +new Date();
    this.hasAudio = hasAudio;
    this.messageId = messageId;
    this.recipient = recipient;
    this.duration = duration;
  }
}
