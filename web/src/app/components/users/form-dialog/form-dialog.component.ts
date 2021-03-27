import { Component, Inject, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Observable } from "rxjs";
import { User, UserStatus } from "src/app/models/user";
import { UsersService, UsersState } from "src/app/services/users/users.service";
import { roles } from "src/app/utils/role";

export enum FormType {
  INSERT = "INSERT",
  UPDATE = "UPDATE",
  DISABLE = "DISABLE",
  ENABLE = "ENABLE",
}

interface FormDialogData {
  type: FormType;
  user?: User;
}

@Component({
  selector: "app-form-dialog",
  templateUrl: "./form-dialog.component.html",
  styleUrls: ["./form-dialog.component.scss"],
})
export class FormDialogComponent implements OnInit {
  private form: FormGroup;
  private user: User = new User({});
  public state$: Observable<UsersState>;
  public userRoles: string[] = ["ADMIN", "OPERATOR", "EMPLOYEE"];
  public isLoading: boolean;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: FormDialogData,
    public dialogRef: MatDialogRef<FormDialogComponent>,
    private usersService: UsersService,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.state$ = usersService.usersState$;

    this.form = this.formBuilder.group({
      name: ["", Validators.required],
      email: ["", Validators.required],
      phone: ["", Validators.required],
      role: ["", Validators.required],
    });
  }

  ngOnInit(): void {
    const { user } = this.data;
    if (user) {
      this.user = user;
      this.form.setValue({
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role,
      });
    }
  }

  public submit() {
    const user = new User({
      ...this.user,
      ...this.form.value,
    });

    this.isLoading = true;

    if (this.data.type === FormType.INSERT) {
      this.usersService.add(
        { ...user, username: user.email },
        () => {
          this.isLoading = false;

          this.showSuccess("Usuário criado com sucesso!");
          this.dialogRef.close();
        },
        () => {
          this.isLoading = false;
          this.showError("Ocorreu um erro interno");
          this.dialogRef.close();
        }
      );
    } else {
      this.usersService.update(user, () => {
        this.isLoading = false;

        this.dialogRef.close();
        this.showSuccess("Usuário atualizado com sucesso!");
      });
    }
  }

  public changeUserStatus() {
    this.isLoading = true;
    console.log(this.data.type);
    console.log(FormType.ENABLE);
    if (this.data.type === FormType.ENABLE) {
      this.usersService.update(
        { ...this.user, status: UserStatus.OFFLINE },
        () => {
          this.isLoading = false;

          this.dialogRef.close();
          this.showSuccess("Usuário habilitado com sucesso!");
        }
      );
    } else {
      this.usersService.disable(this.user.username, () => {
        this.isLoading = false;

        this.dialogRef.close();
        this.showSuccess("Usuário desabilitado com sucesso!");
      });
    }
  }

  private showSuccess(message: string) {
    this.snackBar.open(message, null, {
      duration: 2000,
      panelClass: ["snackbar-success"],
      horizontalPosition: "right",
      verticalPosition: "bottom",
    });
  }

  private showError(message: string) {
    this.snackBar.open(message, null, {
      duration: 2000,
      panelClass: ["snackbar-error"],
      horizontalPosition: "right",
      verticalPosition: "bottom",
    });
  }

  public translateRole(role: string) {
    return roles[role];
  }

  get name() {
    return this.form.get("name");
  }
  get email() {
    return this.form.get("email");
  }
  get phone() {
    return this.form.get("phone");
  }
}
