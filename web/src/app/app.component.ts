import { Component, HostBinding, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService } from "./services/auth/auth.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent implements OnInit {
  public isAppLoading: boolean = true;
  public isLoggedIn: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  async ngOnInit() {
    this.isAppLoading = true;
    await this.authService.getCurrentUser();
    this.authService.authStore().subscribe((store) => {
      this.isAppLoading = false;
      this.isLoggedIn = store.isLoggedIn;
      if (!store.isLoggedIn && !store.isLoading) {
        this.router.navigate(["/login"]);
      }
    });
  }
  public async signOut() {
    await this.authService.signOut();
  }

  @HostBinding("class")
  get theme() {
    return "theme-light";
  }
}
