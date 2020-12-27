import { Component, OnInit, ViewChild } from "@angular/core";
import { AgmMap } from "@agm/core";
import { silverMap } from "./constants";
import { CoreService } from "src/app/services/core/core.service";
import { Observable } from "rxjs";
import { trigger, transition, style, animate } from "@angular/animations";
import { ContactsService } from "src/app/services/core/contacts.service";
import { Contact } from "src/app/interfaces/user";

@Component({
  selector: "app-map",
  animations: [
    trigger("enterAnimation", [
      transition(":enter", [
        // :enter is alias to 'void => *'
        style({ opacity: 0 }),
        animate(100, style({ opacity: 1 })),
      ]),
      transition(":leave", [
        // :leave is alias to '* => void'
        animate(100, style({ opacity: 0 })),
      ]),
    ]),
  ],
  templateUrl: "./map.component.html",
  styleUrls: ["./map.component.scss"],
})
export class MapComponent implements OnInit {
  public mapStyle: Array<any>;
  public showUserDetails: boolean = false;
  private contacts: Contact[] = [];
  infoWindowOpened = null;
  previousInfoWindow = null;
  latitude = -25.4242909;
  longitude = -50.1640366;

  markers = [];

  constructor(
    private coreService: CoreService,
    private contactsService: ContactsService
  ) {
    this.contacts = this.contactsService.getContacts();
    this.contacts.forEach((contact) => {
      this.markers.push({
        lat: +contact.latitude,
        lng: +contact.longitude,
        uuid: 1,
        label: contact.name,
        icon: {
          url: contact.photo,
          scaledSize: { height: 35, width: 35 },
        },
      });
    });
  }

  close_window() {
    if (this.previousInfoWindow != null) {
      this.previousInfoWindow.close();
    }
  }

  ngOnInit() {
    setTimeout(() => {
      console.log("ENTREI");
      const contact = {
        name: "Usuário 6",
        latitude: -25.6966422,
        longitude: -48.4858606,
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+6&background=2c4ebd&color=fff`,
      };
      this.markers.push({
        lat: +contact.latitude,
        lng: +contact.longitude,
        uuid: 1,
        icon: {
          url: contact.photo,
          scaledSize: { height: 35, width: 35 },
        },
      });
    }, 3000);
  }

  selectMarker(event, marker, infoWindow) {
    if (this.previousInfoWindow == null) this.previousInfoWindow = infoWindow;
    else {
      this.infoWindowOpened = infoWindow;
      this.previousInfoWindow.close();
    }
    this.previousInfoWindow = infoWindow;
    // this.showUserDetails = !this.showUserDetails;
  }
}
