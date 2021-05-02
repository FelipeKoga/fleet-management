import { HttpClient } from "@angular/common/http";
import { ElementRef, SimpleChanges, ViewChild } from "@angular/core";
import { Component, Input, OnInit } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { MatBottomSheet } from "@angular/material/bottom-sheet";
import { nanoid } from "nanoid";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { Message, MessageStatus } from "src/app/models/message";
import { User, UserStatus } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService, ChatsState } from "src/app/services/chats/chats.service";
import {
  MessagesService,
  MessagesState,
} from "src/app/services/messages/messages.service";
import { PttService } from "src/app/services/ptt/ptt.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";
import { getAvatar } from "src/app/utils/avatar";
import { convertDate } from "src/app/utils/date";
import { MapBottomSheetComponent } from "../../map/map-bottom-sheet/map-bottom-sheet.component";
declare var MediaRecorder: any;

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
  public showChatDetails: boolean;
  public convertDate = convertDate;
  public user: User = new User({});
  public state$: Observable<ChatsState>;
  public receiving$: Observable<boolean>;

  public recordingAudio: boolean;
  public recordingPTT: boolean;

  private mediaRecorder;

  constructor(
    private chatsService: ChatsService,
    private webSocketService: WebsocketService,
    private authService: AuthService,
    private http: HttpClient,
    private pttService: PttService,
    private _bottomSheet: MatBottomSheet
  ) {}

  ngOnInit() {
    this.user = this.authService.getUser();
    this.state$ = this.chatsService.chatsState$;
    this.receiving$ = this.pttService.receiving$;

    this.chatsService.chatsState$.subscribe((state) => {
      const chat = state.chats.find((item) => item.id === this.chat.id);
      if (chat) {
        this.chat = chat;
      }
    });

    this.webSocketService.messages.subscribe((response) => {
      if (
        response.action === Actions.MESSAGE_SENT ||
        response.action === Actions.MESSAGE_RECEIVED
      ) {
        if (response.body.chatId === this.chat.id) {
          this.viewedMessages();
          this.chatsService.addOrReplaceMessage(response.body);
        }
      }
    });

    this.viewedMessages();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.viewedMessages();
    this.showChatDetails = false;
    if (this.chat.messages && this.chat.messages.length <= 1) {
      this.chatsService.fetchMessages(this.chat.id);
    }
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private viewedMessages() {
    if (this.user.username && this.chat.id) {
      this.webSocketService.sendMessage({
        action: Actions.VIEWED_MESSAGES,
        body: {
          username: this.user.username,
          chatId: this.chat.id,
        },
      });
    }
  }

  public openBottomSheetMap() {
    this._bottomSheet.open(MapBottomSheetComponent, { data: this.chat.user });
  }

  public scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  public sendMessage(event: Event) {
    const text = (event.target as HTMLInputElement).value;
    event.preventDefault();
    this.sendInput.nativeElement.value = null;
    if (!text.length) return;
    const message = new Message({
      chatId: this.chat.id,
      message: text,
      username: this.user.username,
      status: MessageStatus.PENDING,
      hasAudio: false,
    });

    this.chatsService.addOrReplaceMessage(message);

    this.webSocketService.sendMessage({
      action: Actions.SEND_MESSAGE,
      body: message,
    });
  }

  public translateStatus(status: UserStatus) {
    if (status === UserStatus.ONLINE) return "Online";
    if (status === UserStatus.OFFLINE) return "Offline";
    if (status === UserStatus.DISABLED) return "Desabilitado";
  }

  public onShowChatDetails() {
    this.showChatDetails = true;
  }

  public async startRecording() {
    this.recordingAudio = true;
    navigator.mediaDevices
      .getUserMedia({
        audio: true,
      })
      .then((stream) => {
        this.mediaRecorder = new MediaRecorder(stream);
        this.mediaRecorder.start();
        const audioChunks = [];
        let duration: number;
        this.mediaRecorder.addEventListener("start", () => {
          duration = Date.now();
        });
        this.mediaRecorder.addEventListener("dataavailable", (event) => {
          audioChunks.push(event.data);
        });

        this.mediaRecorder.addEventListener("stop", async () => {
          const audioBlob = new Blob(audioChunks);
          duration = Date.now() - duration;

          this.uploadAudio(
            audioBlob.slice(0, audioBlob.size, "audio/wav"),
            duration
          );
        });
      });
  }

  public stopRecording() {
    this.recordingAudio = false;
    this.mediaRecorder.stop();
  }

  private getS3Key = () => {
    return `company/${this.user.companyId}/chat/${this.chat.id}/${
      this.user.username
    }/audios/${nanoid()}.wav`;
  };

  private uploadAudio(blob: Blob, duration: number) {
    const key = this.getS3Key();
    const message = new Message({
      chatId: this.chat.id,
      username: this.user.username,
      status: MessageStatus.PROCESSING_AUDIO,
      message: "",
      hasAudio: true,
      duration,
    });
    this.chatsService.addMessage(message);
    this.http
      .post(
        `https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/files`,
        {
          bucket: "tcc-project-assets",
          key,
        }
      )
      .subscribe(async (response: { getURL: string; putURL: string }) => {
        const { getURL, putURL } = response;
        fetch(putURL, {
          method: "PUT",
          body: blob,
        }).then(() => {
          const messageWithAudio = new Message({
            ...message,
            messageId: message.messageId,
            message: getURL,
            status: MessageStatus.PENDING,
          });
          this.chatsService.addOrReplaceMessage(messageWithAudio);

          this.webSocketService.sendMessage({
            action: Actions.SEND_MESSAGE,
            body: {
              ...messageWithAudio,
              message: key,
            },
          });
        });
      });
  }

  public handleBackToChat() {
    this.showChatDetails = false;
  }

  public startRecordingPTT() {
    this.recordingPTT = true;
    const receivers = this.chat.members
      ? this.chat.members.map((member) => member.username)
      : [this.chat.user.username];
    this.pttService.start(this.chat.id, this.user, receivers);
  }

  public stopRecordingPTT() {
    this.recordingPTT = false;
    this.pttService.stop();
  }

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }
}
