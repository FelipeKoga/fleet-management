import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";
import { ServiceEndpoint as API } from "../../../stack.json";
import { User } from "src/app/models/user";
import { Message } from "src/app/models/message";

export interface ChatsState {
  chats: Chat[];
  isLoading: boolean;
}

const initialState: ChatsState = {
  chats: [],
  isLoading: false,
};

@Injectable({
  providedIn: "root",
})
export class ChatsService extends StateService<ChatsState> {
  private user: User;
  public chatsState$: Observable<ChatsState>;

  constructor(private http: HttpClient, private authService: AuthService) {
    super(initialState);
    this.chatsState$ = this.select((state) => state);
    this.user = this.authService.getUser();
  }

  public fetch() {
    this.setState({ isLoading: true });
    this.http
      .get<Chat[]>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/chats`
      )
      .subscribe((chats) => {
        this.setState({ chats, isLoading: false });
      });
  }

  public replaceLastMessage(message: Message) {
    const chats = [...this.state.chats];
    console.log("replace", message);
    this.setState({
      ...this.state,
      chats: chats.map((chat) => {
        const newChat = { ...chat };
        if (message.chatId === newChat.id) {
          newChat.lastMessage = message;
        }

        return newChat;
      }),
    });
  }
}
