import { Component, Inject, OnInit } from "@angular/core";
import {
  MatBottomSheetRef,
  MAT_BOTTOM_SHEET_DATA,
} from "@angular/material/bottom-sheet";
import { User } from "src/app/models/user";
import { UsersService } from "src/app/services/users/users.service";
import {
  Actions,
  WebsocketService,
} from "src/app/services/websocket/websocket.service";
import { getAvatar } from "src/app/utils/avatar";
declare var google: any;

@Component({
  selector: "app-map-bottom-sheet",
  templateUrl: "./map-bottom-sheet.component.html",
  styleUrls: ["./map-bottom-sheet.component.scss"],
})
export class MapBottomSheetComponent implements OnInit {
  latitude = -24.1069931;
  longitude = -51.3214141;
  avatar;
  loading = true;

  user;

  constructor(
    private _bottomSheetRef: MatBottomSheetRef<MapBottomSheetComponent>,
    private usersService: UsersService,
    private webSocketService: WebsocketService,
    @Inject(MAT_BOTTOM_SHEET_DATA) public data: User
  ) {
    this.usersService.get(this.data.username).subscribe((user) => {
      this.user = user;
      this.loading = false;
    });

    this.webSocketService.messages.subscribe((response) => {
      console.log(response);
      if (response.action === Actions.USER_NEW_LOCATION) {
        this.user = response.body;
      }
    });
  }

  ngOnInit(): void {}

  openLink(event: MouseEvent) {
    this._bottomSheetRef.dismiss();
    event.preventDefault();
  }

  public getUserAvatar(user: User) {
    return getAvatar(user);
  }

  public selectMarker() {
    const that = this;
    const location = {
      lat: this.user.location.latitude,
      lng: this.user.location.longitude,
    };
    const geocoder = new google.maps.Geocoder();
    console.log("opa");
    geocoder.geocode({ location }, function (results) {
      console.log(results);
      if (results[0]) {
        that.user.location.address = results[0].formatted_address;
      } else {
        console.log("No results found");
      }
    });
  }
}
