import { HttpClient } from "@angular/common/http";
import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Auth } from "aws-amplify";
import { User } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import { UsersService } from "src/app/services/users/users.service";
import { roles } from "src/app/utils/role";

@Component({
  selector: "app-profile",
  templateUrl: "./profile.component.html",
  styleUrls: ["./profile.component.scss"],
})
export class ProfileComponent implements OnInit {
  public user: User;
  public dataFormGroup: FormGroup;
  public passwordFormGroup: FormGroup;
  public isLoadingAvatar: boolean;
  public isLoadingDataForm: boolean;
  public isLoadingPasswordForm: boolean;
  public hiddenPasswordInputs: boolean[] = [true, true, true];

  public file: File;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private usersService: UsersService,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {
    this.user = this.authService.getUser();

    this.dataFormGroup = this.formBuilder.group({
      name: [this.user.name, Validators.required],
      email: [this.user.email, Validators.required],
      phone: [this.user.phone, Validators.required],
      customName: [this.user.customName],
    });
    this.passwordFormGroup = this.formBuilder.group({
      oldPassword: ["", Validators.required],
      password: ["", Validators.required],
      confirmPassword: ["", Validators.required],
    });
  }

  ngOnInit(): void {}

  public updateUser() {
    const { name, email, phone, customName } = this.dataFormGroup.value;

    const user = {
      ...this.user,
      name,
      email,
      phone,
      customName,
    };

    this.isLoadingDataForm = true;
    this.usersService.update(user, (updatedUser) => {
      this.isLoadingDataForm = false;
      this.user = updatedUser;
    });
  }

  fileChange(file) {
    this.file = file.target.files[0];
    if (!this.file) return;
    this.isLoadingAvatar = true;
    const key = `company/${this.user.companyId}/user/${this.user.username}/avatar.jpg`;
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
            this.usersService.update(
              { ...this.user, avatar: key },
              (updatedUser: User) => {
                this.isLoadingAvatar = false;
                this.user = updatedUser;

                this.snackBar.open("Avatar adicionado com sucesso!", null, {
                  duration: 2000,
                  panelClass: ["snackbar-success"],
                });
                this.authService.setUser(updatedUser);
              }
            );
          }
        });
      });
  }

  public changePassword() {
    const {
      password,
      confirmPassword,
      oldPassword,
    } = this.passwordFormGroup.value;

    if (password !== confirmPassword) {
      this.snackBar.open("As senhas estão diferentes!", null, {
        duration: 2000,
        panelClass: ["snackbar-error"],
      });
      return;
    }

    console.log("OPA");

    this.isLoadingPasswordForm = true;
    this.authService
      .userChangePassword(oldPassword, password)
      .then((response) => {
        this.isLoadingPasswordForm = false;
        this.snackBar.open("Senha trocada com sucesso!", null, {
          duration: 2000,
          panelClass: ["snackbar-success"],
        });
        console.log(response);
      })
      .catch((e) => {
        console.log(e);
        this.isLoadingPasswordForm = false;

        this.snackBar.open(e.message, null, {
          duration: 2000,
          panelClass: ["snackbar-error"],
        });
      });
  }

  public getRole(role: string) {
    return roles[role];
  }

  public signOut() {
    this.authService.signOut();
  }

  get oldPassword() {
    return this.passwordFormGroup.get("oldPassword");
  }
  get password() {
    return this.passwordFormGroup.get("password");
  }
  get confirmPassword() {
    return this.passwordFormGroup.get("confirmPassword");
  }
}
