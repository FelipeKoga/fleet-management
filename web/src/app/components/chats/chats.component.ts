import { Component, OnInit } from "@angular/core";
import { format, toDate } from "date-fns";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService, ChatsState } from "src/app/services/chats/chats.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";

@Component({
  selector: "app-chats",
  templateUrl: "./chats.component.html",
  styleUrls: ["./chats.component.scss"],
})
export class ChatsComponent implements OnInit {
  public chats: Chat[];
  public selectedChat: Chat;
  public state$: Observable<ChatsState>;
  public username: string;

  constructor(
    private chatsService: ChatsService,
    private webSocketService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.selectedChat = new Chat();
    this.state$ = this.chatsService.chatsState$;
    this.webSocketService.messages.subscribe((response) => {
      if (
        response.action === Actions.MESSAGE_SENT ||
        response.action === Actions.MESSAGE_RECEIVED
      ) {
        this.chatsService.replaceLastMessage(response.body);
      }
    });
    this.chatsService.fetch();
    this.username = this.authService.getUser().username;
  }

  public openChat(chat: Chat) {
    this.selectedChat = chat;
  }

  public convertMessageDate(createdAt: number) {
    return format(toDate(createdAt), "HH:mm");
  }
}
