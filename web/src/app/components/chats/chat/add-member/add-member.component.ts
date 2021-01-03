import { Component, Inject, OnInit } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Chat } from "src/app/models/chat";
import { User } from "src/app/models/user";
import { ChatsService } from "src/app/services/chats/chats.service";
import { UsersService } from "src/app/services/users/users.service";

@Component({
  selector: "app-add-member",
  templateUrl: "./add-member.component.html",
  styleUrls: ["./add-member.component.scss"],
})
export class AddMemberComponent implements OnInit {
  public users: User[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { members: User[]; chat: Chat },
    private dialogRef: MatDialogRef<AddMemberComponent>,
    private chatsService: ChatsService,
    private userServices: UsersService
  ) {}

  ngOnInit(): void {
    this.userServices.usersState$.subscribe((state) => {
      this.users = state.users.filter((user) => {
        return !this.data.members.find(
          (member) => member.username === user.username
        );
      });
    });

    this.userServices.fetch();
  }

  public addMember(member: User) {
    this.chatsService
      .addMember(this.data.chat.id, member.username)
      .subscribe((response) => {
        console.log(response);
        if (response) {
          this.dialogRef.close(member);
        }
      });
  }
}
