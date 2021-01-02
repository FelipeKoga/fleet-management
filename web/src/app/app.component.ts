import { Component, HostBinding, OnInit } from "@angular/core";
import { Router } from "@angular/router";
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

  constructor(
    private authService: AuthService,
    private webSocketService: WebsocketService
  ) {}

  async ngOnInit() {
    this.isAppLoading = true;
    await this.authService.getCurrentUser();
    this.authService.authStore().subscribe((store) => {
      if (store.isLoggedIn) {
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
