import { Component, HostBinding, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { User } from "./models/user";
import { AuthService } from "./services/auth/auth.service";
import { WebsocketService } from "./services/websocket/websocket.service";
import { roles } from "./utils/role";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent implements OnInit {
  public isAppLoading: boolean = true;
  public isLoggedIn: boolean = false;
  public currentUser: User = new User({});

  constructor(
    private authService: AuthService,
    private webSocketService: WebsocketService,
    private router: Router
  ) {}

  async ngOnInit() {
    this.isAppLoading = true;
    await this.authService.getCurrentUser();
    this.authService.authStore().subscribe((store) => {
      if (store.isLoggedIn) {
        this.currentUser = store.user;
        console.log(this.currentUser);
        this.webSocketService.connect();
      }
      this.isAppLoading = false;
      this.isLoggedIn = store.isLoggedIn;
      // if (!store.isLoggedIn && !store.isLoading) {
      //   this.router.navigate(["/login"]);
      // }
    });
  }

  public goToProfile() {
    this.router.navigate(["/profile"]);
  }

  public async signOut() {
    await this.authService.signOut();
  }

  public getRole(role: string) {
    return roles[role];
  }
  @HostBinding("class")
  get theme() {
    return "theme-light";
  }
}
