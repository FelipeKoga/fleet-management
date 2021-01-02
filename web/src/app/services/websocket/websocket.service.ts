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
          console.log(data);
          if (data) {
            this.messages$.next(data);
          }
        },
        (error) => {}
      ),
        () => {
          this.socket$.complete();
          this.connect();
        };
    }
  }

  public sendMessage(message: WebSocketPayload) {
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
