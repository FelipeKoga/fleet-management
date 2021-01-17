import { HttpClient } from "@angular/common/http";
import { ElementRef, SimpleChanges, ViewChild } from "@angular/core";
import { Component, Input, OnInit } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { nanoid } from "nanoid";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { Message, MessageStatus } from "src/app/models/message";
import { User, UserStatus } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import {
  MessagesService,
  MessagesState,
} from "src/app/services/messages/messages.service";
import { PttService } from "src/app/services/ptt/ptt.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";
import { convertDate } from "src/app/utils/date";
declare var MediaRecorder: any;

const mime = ["audio/wav", "audio/mpeg", "audio/webm", "audio/ogg"].filter(
  MediaRecorder.isTypeSupported
)[0];
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
  public state$: Observable<MessagesState>;
  public recordingAudio: boolean;
  public recordingPTT: boolean;

  private mediaRecorder;

  constructor(
    private messagesService: MessagesService,
    private webSocketService: WebsocketService,
    private authService: AuthService,
    private http: HttpClient,
    private pttService: PttService
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    this.state$ = this.messagesService.messageState$;

    this.webSocketService.messages.subscribe((response) => {
      if (
        response.action === Actions.MESSAGE_SENT ||
        response.action === Actions.MESSAGE_RECEIVED
      ) {
        this.viewedMessages();
        this.messagesService.addOrReplaceMessage(response.body);
      }
    });

    this.viewedMessages();
  }

  ngOnChanges(_: SimpleChanges): void {
    this.viewedMessages();
    this.showChatDetails = false;
    this.messagesService.fetch(this.chat.id);
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
      username: this.authService.getUser().username,
      status: MessageStatus.PENDING,
      hasAudio: false,
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
        this.mediaRecorder = new MediaRecorder(stream, {
          mimeType: mime,
        });
        this.mediaRecorder.start();
        const audioChunks = [];
        this.mediaRecorder.addEventListener("dataavailable", (event) => {
          audioChunks.push(event.data);
        });

        this.mediaRecorder.addEventListener("stop", async () => {
          const audioBlob = new Blob(audioChunks);
          this.uploadAudio(audioBlob);
        });
      });
  }

  public stopRecording() {
    this.recordingAudio = false;
    this.mediaRecorder.stop();
  }

  private uploadAudio(blob: Blob) {
    const key = `company/${this.user.companyId}/chat/${this.chat.id}/${
      this.user.username
    }/audios/${nanoid()}.wav`;

    const message = new Message({
      chatId: this.chat.id,
      username: this.user.username,
      status: MessageStatus.PROCESSING_AUDIO,
      message: "",
      hasAudio: true,
    });
    this.messagesService.addMessage(message);
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
          this.messagesService.addOrReplaceMessage(messageWithAudio);

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
    this.pttService.start(this.chat.id, this.chat.user.username, (blob) => {
      console.log(blob);
      this.uploadAudio(blob);
    });
  }

  public stopRecordingPTT() {
    this.recordingPTT = false;
    this.pttService.stop();
  }
}
