import { HttpClient } from "@angular/common/http";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Chat } from "src/app/models/chat";
import { User } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService } from "src/app/services/chats/chats.service";
import { ConfirmationDialogComponent } from "src/app/shared/confirmation-dialog/confirmation-dialog.component";
import { convertDate } from "src/app/utils/date";
import { roles } from "src/app/utils/role";
import { AddMemberComponent } from "../add-member/add-member.component";

@Component({
  selector: "app-details",
  templateUrl: "./details.component.html",
  styleUrls: ["./details.component.scss"],
})
export class DetailsComponent implements OnInit {
  @Output() onBackToChat: EventEmitter<boolean> = new EventEmitter();
  @Input() chat: Chat;

  public isLoadingAvatar: boolean;
  public file: File;
  public isLoadingMembers: boolean;
  public user: User;
  public convertDate = convertDate;
  public members: User[] = [];

  constructor(
    private chatsService: ChatsService,
    private dialog: MatDialog,
    private authService: AuthService,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.members = [];
    this.user = this.authService.getUser();
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
            .removeMember(this.chat.id, this.user.username)
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

  public uploadAvatar(file) {
    this.file = file.target.files[0];
    this.isLoadingAvatar = true;
    const key = `company/${this.user.companyId}/chat/${this.chat.groupName}/avatar.jpg`;
    this.http
      .post(
        `https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/files`,
        {
          bucket: "tcc-project-assets",
          key,
        }
      )
      .subscribe(async (response: { getURL: string; putURL: string }) => {
        console.log(response);
        const { putURL } = response;
        fetch(putURL, {
          method: "PUT",
          body: this.file,
        }).then((value) => {
          this.file = null;
          if (value.status === 200) {
            this.chatsService.addGroupAvatar(
              this.chat.id,
              key,
              (updatedChat) => {
                this.isLoadingAvatar = false;
                this.chat = updatedChat;
                this.snackBar.open("Avatar adicionado com sucesso!", null, {
                  duration: 2000,
                  panelClass: ["snackbar-success"],
                });
              }
            );
          }
        });
      });
  }

  public updateGroup() {}

  public getRole(role: string) {
    return roles[role];
  }
  public backToChat() {
    this.onBackToChat.emit(true);
  }
}
