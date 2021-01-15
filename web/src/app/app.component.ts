import { Component, HostBinding, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { User, UserStatus } from "./models/user";
import { AuthService } from "./services/auth/auth.service";
import {
  Actions,
  WebsocketService,
} from "./services/websocket/websocket.service";
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
        this.currentUser.status = UserStatus.ONLINE;
        console.log("CONNECT!");
        this.webSocketService.connect();
        this.webSocketService.messages.subscribe((message) => {
          if (message.action === Actions.USER_CONNECTED) {
            if (message.body.username === this.currentUser.username) {
              this.authService.setUser(this.currentUser);
              this.currentUser = message.body;
            }
          }
        });
      }
      this.isAppLoading = false;
      this.isLoggedIn = store.isLoggedIn;
    });
  }

  public goToProfile() {
    this.router.navigate(["/profile"]);
  }

  public getRole(role: string) {
    return roles[role];
  }
  @HostBinding("class")
  get theme() {
    return "theme-light";
  }
}
