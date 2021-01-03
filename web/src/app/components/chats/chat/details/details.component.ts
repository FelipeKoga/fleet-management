import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { Chat } from "src/app/models/chat";
import { User } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService } from "src/app/services/chats/chats.service";
import { ConfirmationDialogComponent } from "src/app/shared/confirmation-dialog/confirmation-dialog.component";
import { convertDate } from "src/app/utils/date";
import { AddMemberComponent } from "../add-member/add-member.component";

@Component({
  selector: "app-details",
  templateUrl: "./details.component.html",
  styleUrls: ["./details.component.scss"],
})
export class DetailsComponent implements OnInit {
  @Output() onBackToChat: EventEmitter<boolean> = new EventEmitter();
  @Input() chat: Chat;

  public isLoadingMembers: boolean;
  public username: string;
  public convertDate = convertDate;

  public members: User[] = [];
  constructor(
    private chatsService: ChatsService,
    private dialog: MatDialog,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUser().username;
    if (!this.chat.private) {
      this.isLoadingMembers = true;
      this.chatsService.getChatMembers(this.chat.id).subscribe((members) => {
        this.isLoadingMembers = false;
        this.members = members;
      });
    }
  }

  public openAddMember() {
    const dialogRef = this.dialog.open(AddMemberComponent, {
      data: {
        chat: this.chat,
        members: this.members,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.members.push(result);
      }
    });
  }

  public openExitGroup() {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: "Sair do grupo",
        body: "Tem certeza que deseja sair do grupo?",
        confirmationLabel: "Sim",
        onConfirm: () => {
          this.chatsService
            .removeMember(this.chat.id, this.username)
            .subscribe((response) => {
              console.log(response);
            });
        },
      },
    });
  }

  public openRemoveMember(member: User) {
    this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: "Remover membro",
        body: `Tem certeza que deseja remover o membro ${member.name} do grupo?`,
        confirmationLabel: "Sim",
        onConfirm: () => {
          this.chatsService
            .removeMember(this.chat.id, member.username)
            .subscribe((response) => {
              this.members = this.members.filter(
                (m) => m.username !== member.username
              );
              console.log(response);
            });
        },
      },
    });
  }

  public backToChat() {
    this.onBackToChat.emit(true);
  }
}
