import { Message } from "./message";
import { User } from "./user";

export class Chat {
  id: string;
  private?: string;
  lastMessage?: Message;
  groupName?: string;
  admin?: string;
  user?: User;
}
