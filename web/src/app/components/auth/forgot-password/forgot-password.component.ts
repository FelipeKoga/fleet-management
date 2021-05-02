import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { Auth } from "aws-amplify";
import { AuthService } from "src/app/services/auth/auth.service";

@Component({
  selector: "app-forgot-password",
  templateUrl: "./forgot-password.component.html",
  styleUrls: ["./forgot-password.component.scss"],
})
export class ForgotPasswordComponent implements OnInit {
  public step: number = 0;
  public hiddenPasswordInputs: boolean[] = [true, true];
  public usernameForm: FormGroup;
  public verificationForm: FormGroup;
  public isLoading = false;
  public errorMessage = "";

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.usernameForm = this.formBuilder.group({
      username: ["", Validators.required],
    });

    this.verificationForm = this.formBuilder.group({
      code: ["", Validators.required],
      newPassword: ["", Validators.required],
      confirmNewPassword: ["", Validators.required],
    });
  }

  ngOnInit(): void {
    this.authService.authStore().subscribe((authStore) => {
      this.isLoading = authStore.isLoading;
    });
  }

  public async sendVerificationCode() {
    const { username } = this.usernameForm.value;
    this.errorMessage = "";
    await this.authService.sendVerificationCode(username);
    this.step = 1;
    this.snackBar.open(
      "O código de verificação foi enviado para seu e-mail.",
      null,
      {
        duration: 2000,
        panelClass: ["snackbar-info"],
      }
    );
  }

  public async changePassword() {
    const {
      code,
      newPassword,
      confirmNewPassword,
    } = this.verificationForm.value;
    const { username } = this.usernameForm.value;

    if (newPassword !== confirmNewPassword) {
      this.errorMessage = "As senhas estão diferentes.";
      return;
    }

    await this.authService.changePassword(username, code, newPassword);
    this.step = 0;
    this.snackBar.open("Sua senha foi trocada com sucesso!.", null, {
      duration: 2000,
      panelClass: ["snackbar-success"],
    });
    this.back();
  }

  public back() {
    this.router.navigate(["/login"]);
  }

  get username() {
    return this.usernameForm.get("username");
  }

  get code() {
    return this.verificationForm.get("code");
  }

  get newPassword() {
    return this.verificationForm.get("newPassword");
  }

  get confirmNewPassword() {
    return this.verificationForm.get("confirmNewPassword");
  }
}
