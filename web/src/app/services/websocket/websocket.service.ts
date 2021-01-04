import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { ServiceEndpointWebsocket } from "../../../stack.json";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";

export enum Actions {
  CONNECTED = "connected",
  DISCONNECTED = "disconnected",
  SEND_MESSAGE = "send-message",
  MESSAGE_RECEIVED = "new_message",
  MESSAGE_SENT = "message_sent",
  CHAT_UPDATED = "chat_updated",
  CHAT_CREATED = "created_chat",
  CHAT_REMOVED = "chat_removed",
  VIEWED_MESSAGES = "open-messages",
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
          const data = response.data as WebSocketPayload;
          if (data) {
            this.messages$.next(data);
          }
        },
        (error) => {
          console.log("SUBSCRIBE ERROR", error);
          console.log(error);
          this.socket$.complete();
          this.connect();
        }
      ),
        () => {
          console.log("ON COMPLETE");
          this.socket$.complete();
          this.connect();
        };
    }
  }

  public sendMessage(message: WebSocketPayload) {
    console.log("sendMessage", message);
    console.log(this.socket$.closed);
    console.log(this.socket$.hasError);
    if (this.socket$.closed) {
      this.connect();
    }
    this.socket$.next(message);
  }

  private getNewWebSocket() {
    const { username } = this.authService.getUser();
    return webSocket({
      url: `${ServiceEndpointWebsocket}?username=${username}`,
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
