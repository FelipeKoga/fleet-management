import { HttpClient } from "@angular/common/http";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Chat } from "src/app/models/chat";
import { User } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { ChatsService } from "src/app/services/chats/chats.service";
import { ConfirmationDialogComponent } from "src/app/shared/confirmation-dialog/confirmation-dialog.component";
import { getAvatar } from "src/app/utils/avatar";
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
  public editGroupName: boolean;
  public editAdmin: boolean;

  public isLoadingAvatar: boolean;
  public file: File;
  public isLoadingMembers: boolean;
  public user: User;
  public convertDate = convertDate;
  public members: User[];
  public groupNameForm: FormGroup;
  public loadingForm: boolean;
  public administratorForm: FormGroup;

  constructor(
    private chatsService: ChatsService,
    private dialog: MatDialog,
    private authService: AuthService,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();

    if (!this.chat.private) {
      this.groupNameForm = this.formBuilder.group({
        groupName: [this.chat.groupName, Validators.required],
      });

      this.administratorForm = this.formBuilder.group({
        admin: [this.chat.admin, Validators.required],
      });
    }

    if (!this.chat.private) {
      this.members = [...this.chat.members, this.user];
      this.administratorForm
        .get("admin")
        .setValue(
          this.members.find((member) => this.chat.admin === member.username)
        );
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

  public getRole(role: string) {
    return roles[role];
  }
  public backToChat() {
    this.onBackToChat.emit(true);
  }

  public onChangeAdministrador() {
    console.log("change");
    const { admin } = this.administratorForm.value;
    console.log(admin);
    this.updateGroup(this.chat.groupName, admin.username);
  }

  public onChangeGroupName() {
    const { groupName } = this.groupNameForm.value;
    this.loadingForm = true;
    this.updateGroup(groupName, this.chat.admin);
  }

  private updateGroup(groupName: string, admin: string) {
    this.loadingForm = true;

    this.chatsService
      .updateGroup(this.chat.id, {
        groupName,
        admin,
      })
      .subscribe((updatedChat) => {
        this.loadingForm = false;
        this.editGroupName = false;
        this.chat = updatedChat;
        this.snackBar.open("Grupo atualizado com sucesso!", null, {
          duration: 2000,
          panelClass: ["snackbar-success"],
        });
      });
  }

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }
}
