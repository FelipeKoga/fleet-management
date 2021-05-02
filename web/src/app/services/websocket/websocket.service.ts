import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { ServiceEndpointWebsocket } from "../../../stack.json";
import { AuthService } from "../auth/auth.service";

export enum Actions {
  CHAT_UPDATED = "CHAT_UPDATED",
  CHAT_CREATED = "CHAT_CREATED",
  CHAT_REMOVED = "CHAT_REMOVED",

  SEND_MESSAGE = "SEND_MESSAGE",
  MESSAGE_RECEIVED = "NEW_MESSAGE",
  MESSAGE_SENT = "MESSAGE_SENT",
  VIEWED_MESSAGES = "OPEN_MESSAGES",

  USER_CONNECTED = "USER_CONNECTED",
  USER_DISCONNECTED = "USER_DISCONNECTED",

  USER_NEW_LOCATION = "USER_NEW_LOCATION",

  START_PUSH_TO_TALK = "START_PUSH_TO_TALK",
  STOP_PUSH_TO_TALK = "STOP_PUSH_TO_TALK",
  PUSH_TO_TALK = "PUSH_TO_TALK",

  STARTED_PUSH_TO_TALK = "STARTED_PUSH_TO_TALK",
  STOPPED_PUSH_TO_TALK = "STOPPED_PUSH_TO_TALK",
  RECEIVED_PUSH_TO_TALK = "RECEIVED_PUSH_TO_TALK",
}

interface WebSocketPayload {
  action?: Actions;
  body: any;
}

@Injectable({
  providedIn: "root",
})
export class WebsocketService {
  private socket$: WebSocketSubject<any>;
  private messages$: BehaviorSubject<WebSocketPayload> = new BehaviorSubject<WebSocketPayload>(
    { action: null, body: null }
  );

  constructor(private authService: AuthService) {}

  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      this.socket$ = this.getNewWebSocket();
      this.socket$.subscribe(
        (response) => {
          const data = response as WebSocketPayload;
          if (data) {
            this.messages$.next(data);
          }
        },
        () => {
          this.socket$.complete();
          this.connect();
        }
      ),
        () => {
          this.socket$.complete();
          this.connect();
        };
    }
  }

  public sendMessage(message: WebSocketPayload) {
    if (this.socket$.closed) {
      this.connect();
    }
    this.socket$.next(message);
  }

  private getNewWebSocket() {
    const token = this.authService.getAuthToken();
    return webSocket({
      url: `${ServiceEndpointWebsocket}?token=${token}`,
      deserializer: (msg) => {
        return msg.data ? JSON.parse(msg.data) : "";
      },
    });
  }

  public close() {
    this.socket$.complete();
  }

  public get messages() {
    return this.messages$.asObservable();
  }
}
