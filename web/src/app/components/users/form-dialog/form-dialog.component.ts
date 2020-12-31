import { Component, Inject, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Observable } from "rxjs";
import { User } from "src/app/models/user";
import { UsersService, UsersState } from "src/app/services/users/users.service";

export enum FormType {
  INSERT = "INSERT",
  UPDATE = "UPDATE",
  DELETE = "DELETE",
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

  constructor(
    public dialogRef: MatDialogRef<FormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FormDialogData,
    private usersService: UsersService,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.state$ = usersService.usersState$;

    this.form = this.formBuilder.group({
      name: ["", Validators.required],
      email: ["", Validators.required],
      phone: ["", Validators.required],
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
      });
    }
  }

  public submit() {
    const user = new User({
      ...this.user,
      ...this.form.value,
    });

    if (this.data.type === FormType.INSERT) {
      this.usersService.add(user, () => {
        this.showSuccess("Usuário criado com sucesso!");
        this.dialogRef.close();
      });
    } else {
      this.usersService.update(user, () => {
        this.dialogRef.close();
        this.showSuccess("Usuário atualizado com sucesso!");
      });
    }
  }

  public delete() {
    this.usersService.delete(this.user.username, () => {
      this.dialogRef.close();
      this.showSuccess("Usuário excluído com sucesso!");
    });
  }

  private showSuccess(message: string) {
    this.snackBar.open(message, null, {
      duration: 2000,
      panelClass: ["snackbar-success"],
      horizontalPosition: "right",
      verticalPosition: "bottom",
    });
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
