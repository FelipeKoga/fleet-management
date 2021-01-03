import { Message } from "./message";
import { User } from "./user";

export class Chat {
  id: string;
  private?: string;
  lastMessage?: Message;
  newMessages: number;
  groupName?: string;
  admin?: string;
  user?: User;
  createdAt: number;
}
