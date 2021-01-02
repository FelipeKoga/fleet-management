import { HttpClient } from "@angular/common/http";
import { Message } from "@angular/compiler/src/i18n/i18n_ast";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { User } from "src/app/models/user";
import {
  ServiceEndpoint as API,
  ServiceEndpointWebsocket,
} from "../../../stack.json";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";
import { webSocket } from "rxjs/webSocket";

export interface MessagesState {
  messages: Message[];
  isLoading: boolean;
}

const initialState: MessagesState = {
  messages: [],
  isLoading: false,
};

@Injectable({
  providedIn: "root",
})
export class MessagesService extends StateService<MessagesState> {
  private user: User;
  public messageState$: Observable<MessagesState>;

  constructor(private http: HttpClient, private authService: AuthService) {
    super(initialState);
    this.messageState$ = this.select((state) => state);
    this.user = this.authService.getUser();
  }

  public fetch(chatId: string) {
    this.setState({ isLoading: true });
    this.http
      .get<Message[]>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/chats/${chatId}/messages`
      )
      .subscribe((messages) => {
        this.setState({ messages, isLoading: false });
      });
  }

  public send(message: string, chatId: string) {
    this.http
      .post<Message>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/chats/${chatId}/messages`,
        message
      )
      .subscribe((response) => {
        this.setState({
          messages: [...this.state.messages, response],
          isLoading: false,
        });
      });
  }

  public wsAddMessage(message: Message) {
    this.setState({
      messages: [...this.state.messages, message],
    });
  }
}
