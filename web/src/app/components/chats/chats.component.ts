import { Component, OnInit } from "@angular/core";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService, ChatsState } from "src/app/services/chats/chats.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";
import { NewChatType } from "./new-chat/new-chat.component";
import { convertDate } from "../../utils/date";
import { ActivatedRoute } from "@angular/router";
import { Message } from "src/app/models/message";
import { User } from "src/app/models/user";
import { getAvatar } from "src/app/utils/avatar";
import { isToday } from "date-fns";
@Component({
  selector: "app-chats",
  templateUrl: "./chats.component.html",
  styleUrls: ["./chats.component.scss"],
})
export class ChatsComponent implements OnInit {
  public chats: Chat[];
  public filteredChats: Chat[];
  public selectedChat: Chat;
  public state$: Observable<ChatsState>;
  public username: string;
  public showNewChat: NewChatType;
  public isLoadingChats: boolean;

  constructor(
    private chatsService: ChatsService,
    private webSocketService: WebsocketService,
    private authService: AuthService,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const withUsername = this.activatedRoute.snapshot.paramMap.get("username");

    this.selectedChat = new Chat();
    this.chatsService.chatsState$.subscribe((state) => {
      if (withUsername) {
        this.selectedChat = state.chats.find(
          (chat) => chat.user && chat.user.username === withUsername
        );
      }

      this.chats = state.chats;
      this.isLoadingChats = state.isLoading;
    });

    this.webSocketService.messages.subscribe((response) => {
      if (
        response.action === Actions.USER_CONNECTED ||
        response.action === Actions.USER_DISCONNECTED
      ) {
        this.chatsService.replaceUserChat(response.body);
        if (this.selectedChat.user) {
          if (this.selectedChat.user.username === response.body.username) {
            this.selectedChat.user = response.body;
          }
        }
      }

      if (
        response.action === Actions.CHAT_UPDATED ||
        response.action === Actions.CHAT_CREATED
      ) {
        const chat = this.chatsService.findChat(response.body.id);
        if (!chat) {
          this.chatsService.addOrReplaceChat(response.body);
        } else {
          this.chatsService.addOrReplaceChat({
            ...response.body,
            messages: chat.messages,
          });
        }
      }

      if (response.action === Actions.CHAT_REMOVED) {
        if (this.selectedChat.id === response.body.chatId) {
          this.selectedChat = new Chat();
        }
        this.chatsService.removeChat(response.body);
      }
    });
    this.chatsService.fetch();
    this.username = this.authService.getUser().username;
  }

  public openChat(chat: Chat) {
    this.showNewChat = null;
    if (this.selectedChat.id !== chat.id) {
      this.selectedChat = chat;
    }
  }

  public newGroup() {
    this.selectedChat = new Chat();
    this.showNewChat = NewChatType.GROUP;
  }
  public newPrivateChat() {
    this.selectedChat = new Chat();
    this.showNewChat = NewChatType.PRIVATE;
  }

  public handleChatCreated(chat: Chat) {
    this.chatsService.addOrReplaceChat(chat);
    this.selectedChat = chat;
    this.showNewChat = null;
  }

  public filterChats(text: string) {
    this.filteredChats = this.chats.filter((chat) => {
      if (chat.private) {
        return chat.user.name.toLowerCase().includes(text.toLowerCase());
      } else {
        return chat.groupName.toLowerCase().includes(text.toLowerCase());
      }
    });
  }

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }

  public getLastMessage(chat: Chat): Message {
    return chat.messages[chat.messages.length - 1];
  }

  public convertDate(timestamp: number) {
    if (isToday(timestamp)) {
      return convertDate(timestamp, "HH:mm");
    }

    return convertDate(timestamp, "dd/MM/yyyy");
  }
}
