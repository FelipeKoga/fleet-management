import { Component, OnInit } from "@angular/core";
import { UsersService } from "src/app/services/users/users.service";
import { User, UserRole, UserStatus } from "src/app/models/user";
import { AuthService } from "src/app/services/auth/auth.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";
import { FormControl } from "@angular/forms";
import { Message, MessageStatus } from "src/app/models/message";
import { MatSnackBar } from "@angular/material/snack-bar";
import { getAvatar } from "src/app/utils/avatar";
declare var google: any;
@Component({
  selector: "app-map",
  templateUrl: "./map.component.html",
  styleUrls: ["./map.component.scss"],
})
export class MapComponent implements OnInit {
  private currentUserLocation: { latitude: number; longitude: number };

  public currentUser: User;
  public users: User[];
  public activeUsers: User[];
  public selectedUser: User;
  public infoWindowOpened = null;
  public previousInfoWindow = null;
  public markers = [];
  public currentFocus: { latitude: number; longitude: number };
  public messageFormControl: FormControl;

  constructor(
    private usersService: UsersService,
    private authService: AuthService,
    private webSocketService: WebsocketService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.messageFormControl = new FormControl("");

    this.selectedUser = new User({});
    navigator.geolocation.getCurrentPosition((resp) => {
      this.currentUserLocation = {
        latitude: resp.coords.latitude,
        longitude: resp.coords.longitude,
      };
      this.currentFocus = this.currentUserLocation;
    });

    this.currentUser = this.authService.getUser();
    this.usersService.usersState$.subscribe((state) => {
      this.users = state.users
        .filter(
          (user) =>
            user.username !== this.currentUser.username &&
            user.role === UserRole.EMPLOYEE &&
            user.status !== UserStatus.DISABLED
        )
        .sort((a) => {
          if (a.status === UserStatus.ONLINE) return -1;
          return 1;
        });
      this.getActiveUsers();
      if (this.users.length) {
        this.markers = [];
        this.users.forEach((user) => {
          if (user.location) {
            this.markers.push({
              latitude: +user.location.latitude,
              longitude: +user.location.longitude,
              icon: {
                url: user.avatar ? user.avatar : this.getUserAvatar(user),
              },
              user,
            });
          }
        });
      }
    });

    this.webSocketService.messages.subscribe((response) => {
      console.log(response);
      if (
        response.action === Actions.USER_CONNECTED ||
        response.action === Actions.USER_DISCONNECTED ||
        response.action === Actions.USER_NEW_LOCATION
      ) {
        this.usersService.replaceUser(response.body);
      }
    });

    this.usersService.fetch();
  }

  public userClicked(user: User) {
    this.currentFocus = this.currentUserLocation;
    this.currentFocus = {
      latitude: user.location.latitude,
      longitude: user.location.longitude,
    };
  }

  public selectMarker(_, marker, infoWindow) {
    this.messageFormControl.reset();

    this.selectedUser = marker.user;

    const that = this;
    const location = { lat: marker.latitude, lng: marker.longitude };
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ location }, function (results) {
      if (results[0]) {
        that.selectedUser.location.address = results[0].formatted_address;
      } else {
        console.log("No results found");
      }
    });
  }

  public sendMessage() {
    if (!this.messageFormControl.value) return;

    const message = new Message({
      chatId: null,
      message: this.messageFormControl.value,
      status: MessageStatus.PENDING,
      username: this.currentUser.username,
      recipient: this.selectedUser.username,
    });

    this.webSocketService.sendMessage({
      action: Actions.SEND_MESSAGE,
      body: message,
    });

    this.snackBar.open("Mensagem enviada!", null, {
      duration: 2000,
      panelClass: ["snackbar-success"],
      horizontalPosition: "right",
      verticalPosition: "bottom",
    });

    this.messageFormControl.reset();
  }

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }

  public getActiveUsers() {
    this.activeUsers = this.users.filter((user) => {
      return user.status === UserStatus.ONLINE && user.locationUpdate !== 0;
    });
  }
}
