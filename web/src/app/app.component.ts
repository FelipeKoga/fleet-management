import { Component, HostBinding, OnInit } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { User, UserStatus } from "./models/user";
import { AuthService } from "./services/auth/auth.service";
import { PttService } from "./services/ptt/ptt.service";
import {
  Actions,
  WebsocketService,
} from "./services/websocket/websocket.service";
import { PttSnackbarComponent } from "./shared/ptt-snackbar/ptt-snackbar.component";
import { getAvatar } from "./utils/avatar";
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
    private pttService: PttService,
    private router: Router,
    private snackbar: MatSnackBar
  ) {}

  async ngOnInit() {
    this.isAppLoading = true;
    await this.authService.getCurrentUser();
    this.authService.authStore().subscribe((store) => {
      if (store.isLoggedIn) {
        this.currentUser = store.user;
        this.currentUser.status = UserStatus.ONLINE;
        this.webSocketService.connect();
        this.webSocketService.messages.subscribe((message) => {
          if (message.action === Actions.USER_CONNECTED) {
            if (message.body.username === this.currentUser.username) {
              this.authService.setUser(this.currentUser);
              this.currentUser = message.body;
            }
          }

          if (message.action === Actions.STARTED_PUSH_TO_TALK) {
            this.pttService.startReceivingPushToTalk(message.body.chatId);
            this.snackbar.openFromComponent(PttSnackbarComponent, {
              data: new User({ name: message.body.user.name }),
              horizontalPosition: "right",
              verticalPosition: "top",
              panelClass: ["snackbar-primary"],
            });
          }

          if (message.action === Actions.RECEIVED_PUSH_TO_TALK) {
            this.pttService.playAudio(message.body.inputData);
          }

          if (message.action === Actions.STOPPED_PUSH_TO_TALK) {
            this.snackbar.dismiss();
            this.pttService.stopReceivingPushToTalk();
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

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }

  @HostBinding("class")
  get theme() {
    return "theme-light";
  }
}
