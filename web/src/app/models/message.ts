import { nanoid } from "nanoid";

export enum MessageStatus {
  PENDING = "PENDING",
  SENT = "SENT",
}
export class Message {
  chatId: string;
  messageId: string;
  message: string;
  createdAt: number;
  username: string;
  status: MessageStatus;

  constructor({
    chatId,
    message,
    username,
    status,
  }: Omit<Message, "messageId" | "createdAt">) {
    this.chatId = chatId;
    this.messageId = nanoid();
    this.message = message;
    this.username = username;
    this.status = status;
    this.createdAt = +new Date();
  }
}
