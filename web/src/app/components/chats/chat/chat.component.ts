import { ElementRef, SimpleChanges, ViewChild } from "@angular/core";
import { Component, Input, OnInit } from "@angular/core";
import { format, toDate } from "date-fns";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { Message, MessageStatus } from "src/app/models/message";
import { UserStatus } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import {
  MessagesService,
  MessagesState,
} from "src/app/services/messages/messages.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";

@Component({
  selector: "app-chat",
  templateUrl: "./chat.component.html",
  styleUrls: ["./chat.component.scss"],
  providers: [MessagesService],
})
export class ChatComponent implements OnInit {
  @ViewChild("sendInput") sendInput: ElementRef;
  @ViewChild("messageList") private myScrollContainer: ElementRef;
  @Input() chat: Chat;
  public username: string;

  public state$: Observable<MessagesState>;

  constructor(
    private messagesService: MessagesService,
    private webSocketService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUser().username;
    this.state$ = this.messagesService.messageState$;

    this.webSocketService.messages.subscribe((response) => {
      if (
        response.action === Actions.MESSAGE_SENT ||
        response.action === Actions.MESSAGE_RECEIVED
      ) {
        this.messagesService.addOrReplaceMessage(response.body);
      }
    });
  }

  ngOnChanges(_: SimpleChanges): void {
    this.messagesService.fetch(this.chat.id);
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  public sendMessage(text: string) {
    this.sendInput.nativeElement.value = "";
    const message = new Message({
      chatId: this.chat.id,
      message: text,
      username: this.authService.getUser().username,
      status: MessageStatus.PENDING,
    });

    this.messagesService.addOrReplaceMessage(message);

    this.webSocketService.sendMessage({
      action: Actions.SEND_MESSAGE,
      body: message,
    });
  }

  public translateStatus(status: UserStatus) {
    if (status === UserStatus.ONLINE) return "Online";
    if (status === UserStatus.OFFLINE) return "Offline";
  }

  public convertMessageDate(createdAt: number) {
    return format(toDate(createdAt), "HH:mm");
  }
}
