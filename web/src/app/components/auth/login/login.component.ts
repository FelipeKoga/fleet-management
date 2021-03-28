import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { UserRole } from "src/app/models/user";
import {
  AuthService,
  AuthServiceStore,
} from "src/app/services/auth/auth.service";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
})
export class LoginComponent implements OnInit {
  public isPasswordHidden: boolean = true;
  public authForm: FormGroup;

  public store: AuthServiceStore;
  public errorMessage: string;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router
  ) {
    this.authForm = this.formBuilder.group({
      username: ["", Validators.required],
      password: ["", Validators.required],
    });
  }

  ngOnInit(): void {
    this.authService.authStore().subscribe((authStore) => {
      this.store = authStore;
      this.errorMessage = "";
      const { errorCode, isLoggedIn } = authStore;

      if (isLoggedIn) {
        if (authStore.user.role === UserRole.EMPLOYEE) {
          this.router.navigate(["/chats"]);
        } else {
          this.router.navigate(["/map"]);
        }
      }

      if (errorCode) {
        if (errorCode === "NotAuthorizedException") {
          this.errorMessage = "Usuário e/ou senha incorretos.";
        }
      }
    });
  }

  public async signIn(): Promise<void> {
    const { username, password } = this.authForm.value;
    await this.authService.signIn(username, password);
  }

  public togglePassword() {
    this.isPasswordHidden = !this.isPasswordHidden;
  }

  public redirectToForgotPassword() {
    this.router.navigate(["/forgot-password"]);
  }

  get password() {
    return this.authForm.get("password");
  }

  get username() {
    return this.authForm.get("username");
  }
}
