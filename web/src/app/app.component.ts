import { Component, HostBinding, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { User } from "./models/user";
import { AuthService } from "./services/auth/auth.service";
import { WebsocketService } from "./services/websocket/websocket.service";

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
    private webSocketService: WebsocketService
  ) {}

  async ngOnInit() {
    this.isAppLoading = true;
    await this.authService.getCurrentUser();
    this.authService.authStore().subscribe((store) => {
      if (store.isLoggedIn) {
        this.currentUser = store.user;
        this.webSocketService.connect();
      }
      this.isAppLoading = false;
      this.isLoggedIn = store.isLoggedIn;
      // if (!store.isLoggedIn && !store.isLoading) {
      //   this.router.navigate(["/login"]);
      // }
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
