import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Chat } from "src/app/models/chat";
import { User } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService } from "src/app/services/chats/chats.service";
import { UsersService } from "src/app/services/users/users.service";
import { getAvatar } from "src/app/utils/avatar";

export enum NewChatType {
  GROUP = "GROUP",
  PRIVATE = "PRIVATE",
}

@Component({
  selector: "app-new-chat",
  templateUrl: "./new-chat.component.html",
  styleUrls: ["./new-chat.component.scss"],
})
export class NewChatComponent implements OnInit {
  public newGroupForm: FormGroup;
  public isLoading: boolean;
  public isLoadingNewChat: boolean;
  public users: User[] = [];

  @Input() newChatType: NewChatType;
  @Output() onChatCreated: EventEmitter<Chat> = new EventEmitter();

  constructor(
    private usersService: UsersService,
    private chatsService: ChatsService,
    private authService: AuthService,
    private formBuilder: FormBuilder
  ) {
    this.newGroupForm = this.formBuilder.group({
      groupName: ["", Validators.required],
      members: [[], Validators.required],
    });
  }

  ngOnInit(): void {
    this.isLoadingNewChat = false;
    const username = this.authService.getUser().username;
    this.usersService.usersState$.subscribe((state) => {
      this.isLoading = state.isLoading && !state.users.length;
      this.users = state.users.filter((user) => user.username !== username);
    });

    this.usersService.fetch();
  }

  public newPrivateChat(user: User) {
    this.isLoadingNewChat = true;
    this.chatsService.newPrivateChat(user.username, (chat) => {
      this.isLoadingNewChat = false;
      this.onChatCreated.next(chat);
    });
  }

  public newGroup() {
    const { groupName, members } = this.newGroupForm.value;
    if (!members.length) return;
    this.isLoadingNewChat = true;
    this.chatsService.newGroup(
      groupName,
      members.map((member: User) => member.username),
      (chat) => {
        this.onChatCreated.next(chat);
        this.isLoadingNewChat = false;
      }
    );
  }

  get members() {
    return this.newGroupForm.get("members");
  }

  getUserAvatar(user: User) {
    return getAvatar(user);
  }
}
