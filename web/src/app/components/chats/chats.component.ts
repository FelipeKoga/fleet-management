import { Component, OnInit } from "@angular/core";
import { format, toDate } from "date-fns";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { ChatsService, ChatsState } from "src/app/services/chats/chats.service";

@Component({
  selector: "app-chats",
  templateUrl: "./chats.component.html",
  styleUrls: ["./chats.component.scss"],
})
export class ChatsComponent implements OnInit {
  public chats: Chat[];
  public selectedChat: Chat;
  public state$: Observable<ChatsState>;

  constructor(private chatsService: ChatsService) {}

  ngOnInit(): void {
    this.selectedChat = new Chat();
    this.state$ = this.chatsService.chatsState$;
    this.chatsService.fetch();
  }

  public openChat(chat: Chat) {
    this.selectedChat = chat;
  }

  public convertMessageDate(createdAt: number) {
    return format(toDate(createdAt), "HH:mm");
  }
}
