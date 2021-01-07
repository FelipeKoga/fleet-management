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
  public convertDate = convertDate;

  constructor(
    private chatsService: ChatsService,
    private webSocketService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.selectedChat = new Chat();
    this.chatsService.chatsState$.subscribe((state) => {
      console.log(state.chats);
      this.chats = state.chats;
      this.isLoadingChats = state.isLoading;
    });
    this.webSocketService.messages.subscribe((response) => {
      console.log(response);
      if (
        response.action === Actions.CHAT_UPDATED ||
        response.action === Actions.CHAT_CREATED
      ) {
        this.chatsService.addOrReplaceChat(response.body);
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
    this.selectedChat = chat;
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
    this.selectedChat = chat;
    this.showNewChat = null;
  }

  public filterChats(text: string) {
    this.filteredChats = this.chats.filter((chat) => {
      if (chat.private) {
        return chat.user.name.includes(text);
      } else {
        return chat.groupName.includes(text);
      }
    });
  }
}
