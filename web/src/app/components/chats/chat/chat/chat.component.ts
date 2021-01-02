import { Message } from "@angular/compiler/src/i18n/i18n_ast";
import { ElementRef, SimpleChanges, ViewChild } from "@angular/core";
import { Component, Input, OnInit } from "@angular/core";
import { FormBuilder, FormGroup } from "@angular/forms";
import { Chat } from "src/app/models/chat";
import { AuthService } from "src/app/services/auth/auth.service";
import { MessagesService } from "src/app/services/messages/messages.service";
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
  public messages: Message[];
  public username: string;

  constructor(
    private messagesService: MessagesService,
    private webSocketService: WebsocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUser().username;
    this.messagesService.messageState$.subscribe((state) => {
      console.log(state.messages);
      this.messages = state.messages;
    });

    this.webSocketService.messages.subscribe((response) => {
      console.log("RESPONSE FROM CHAT", response);
      if (
        response.action === Actions.MESSAGE_SENT ||
        response.action === Actions.MESSAGE_RECEIVED
      ) {
        this.messagesService.wsAddMessage(response.body as Message);
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

  public sendMessage(message: string) {
    this.sendInput.nativeElement.value = "";
    this.webSocketService.sendMessage({
      action: Actions.SEND_MESSAGE,
      body: {
        message,
        chatId: this.chat.id,
        username: this.authService.getUser().username,
      },
    });
  }
}
