import { Message } from "@angular/compiler/src/i18n/i18n_ast";
import { User } from "./user";

export class Chat {
  id: string;
  private?: string;
  lastMessage?: Message;
  groupName?: string;
  admin?: string;
  user?: User;
}
