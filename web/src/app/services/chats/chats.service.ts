import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Chat } from "src/app/models/chat";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";
import { ServiceEndpoint as API } from "../../../stack.json";
import { User } from "src/app/models/user";
import { format, parseISO } from "date-fns";
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
        onComplete(updatedChat);
      });
  }

  public getChatMembers(chatId: string) {
    return this.http.get<User[]>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}`
    );
  }

  public updateGroup(
    chatId: string,
    body: { groupName: string; admin: string }
  ) {
    return this.http.put<Chat>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}`,
      body
    );
  }

  public addMember(chatId: string, member: string) {
    return this.http.put<boolean>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}/add`,
      { member, chatId }
    );
  }

  public removeMember(chatId: string, member: string) {
    return this.http.put<boolean>(
      `${API}/company/${this.user.companyId}/users/${this.user.username}/group/${chatId}/remove`,
      { member, chatId }
    );
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
          .sort(this.sort),
      });
    } else {
      chats.unshift(newChat);
      this.setState({
        chats: chats.sort(this.sort),
      });
    }
  }

  public removeChat({ chatId }) {
    this.setState({
      ...this.state,
      chats: this.state.chats.filter((chat) => chat.id !== chatId),
    });
  }

  public replaceUserChat(user: User) {
    this.setState({
      ...this.state,
      chats: this.state.chats.map((chat) => {
        if (chat.private && chat.user.username === user.username) {
          return {
            ...chat,
            user,
          };
        }

        return chat;
      }),
    });
  }

  private sort(c1: Chat, c2: Chat) {
    if (!c1.messages.length) return 1;

    if (c1.messages.length && !c2.messages.length) return -1;

    return (
      c2.messages[c2.messages.length - 1].createdAt -
      c1.messages[c1.messages.length - 1].createdAt
    );
  }

  public fetchMessages(chatId: string) {
    this.http
      .get<Message[]>(
        `${API}/company/${this.user.companyId}/users/${this.user.username}/chats/${chatId}/messages`
      )
      .subscribe((messages) => {
        const chat = this.findChat(chatId);
        this.addOrReplaceChat({ ...chat, messages });
      });
  }

  public addMessage(message: Message) {
    const chat = this.findChat(message.chatId);
    this.addOrReplaceChat({ ...chat, messages: [...chat.messages, message] });
  }

  public addOrReplaceMessage(message: Message) {
    const chat = this.findChat(message.chatId);
    if (chat.messages.find((msg) => msg.messageId === message.messageId)) {
      this.addOrReplaceChat({
        ...chat,
        messages: chat.messages.map((msg) => {
          if (msg.messageId === message.messageId) {
            return message;
          }
          return msg;
        }),
      });
    } else {
      this.addOrReplaceChat({
        ...chat,
        messages: [...chat.messages, message],
      });
    }
  }

  public findChat(chatId: string) {
    return this.state.chats.find((chat) => chat.id === chatId);
  }
}
