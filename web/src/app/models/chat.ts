import { Message } from "./message";
import { User } from "./user";

export class Chat {
  id: string;
  private?: string;
  messages: Message[];
  newMessages: number;
  groupName?: string;
  admin?: string;
  user?: User;
  createdAt: number;
  avatar?: string;
  members?: User[];
}
