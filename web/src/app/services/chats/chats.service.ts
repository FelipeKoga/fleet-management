import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";
import { ServiceEndpoint as API } from "../../../stack.json";
import { User } from "src/app/models/user";

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

  public newPrivateChat(
    withUsername: string,
    onComplete: (chat: Chat) => void
  ) {
    this.http
      .post<Chat>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/chats`,
        { withUsername }
      )
      .subscribe((newChat) => {
        onComplete(newChat);
      });
  }

  public newGroup(
    groupName: string,
    members: string[],
    onComplete: (chat: Chat) => void
  ) {
    this.http
      .post<Chat>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/group`,
        { groupName, members }
      )
      .subscribe((newChat) => {
        onComplete(newChat);
      });
  }

  public addGroupAvatar(
    chatId: string,
    avatar: string,
    onComplete: (chat: Chat) => void
  ) {
    this.http
      .post<Chat>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}/avatar`,
        { avatar }
      )
      .subscribe((updatedChat) => {
        console.log(updatedChat);
        onComplete(updatedChat);
      });
  }

  public addOrReplaceChat(newChat: Chat) {
    const chats = [...this.state.chats];
    if (chats.find((chat) => chat.id === newChat.id)) {
      this.setState({
        chats: chats
          .map((chat) => {
            if (chat.id === newChat.id) return newChat;
            return chat;
          })
          .filter((chat) => {
            return !chat.private || chat.lastMessage;
          })
          .sort(this.sort),
      });
    } else {
      chats.unshift(newChat);
      this.setState({
        chats: chats
          .filter((chat) => !chat.private || chat.lastMessage)
          .sort(this.sort),
      });
    }
  }

  public removeChat({ chatId }) {
    this.setState({
      ...this.state,
      chats: this.state.chats.filter((chat) => chat.id !== chatId),
    });
  }

  public getChatMembers(chatId: string) {
    return this.http.get<User[]>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}`
    );
  }

  public addMember(chatId: string, member: string) {
    return this.http.put<boolean>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}`,
      { member, chatId }
    );
  }

  public removeMember(chatId: string, member: string) {
    return this.http.put<boolean>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}/remove`,
      { member, chatId }
    );
  }

  private sort(c1: Chat, c2: Chat) {
    if (!c1.lastMessage) return 1;

    if (c1.lastMessage && !c2.lastMessage) return -1;

    return c2.lastMessage.createdAt - c1.lastMessage.createdAt;
  }
}
