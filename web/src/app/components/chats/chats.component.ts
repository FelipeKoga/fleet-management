import { Component, OnInit } from "@angular/core";
import { format, toDate } from "date-fns";
import { Chat } from "src/app/models/chat";
import { ChatsService } from "src/app/services/chats/chats.service";

@Component({
  selector: "app-chats",
  templateUrl: "./chats.component.html",
  styleUrls: ["./chats.component.scss"],
})
export class ChatsComponent implements OnInit {
  public chats: Chat[];

  public selectedChat: Chat;

  constructor(private chatsService: ChatsService) {}

  ngOnInit(): void {
    this.selectedChat = new Chat();
    this.chatsService.chatsState$.subscribe((state) => {
      this.chats = state.chats;
    });
    this.chatsService.fetch();
  }

  public openChat(chat: Chat) {
    this.selectedChat = chat;
  }

  public convertMessageDate(createdAt: number) {
    return format(toDate(createdAt), "HH:mm");
  }
}
