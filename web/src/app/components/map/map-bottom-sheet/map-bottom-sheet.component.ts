import { Component, OnInit } from "@angular/core";
import { MatBottomSheetRef } from "@angular/material/bottom-sheet";

@Component({
  selector: "app-map-bottom-sheet",
  templateUrl: "./map-bottom-sheet.component.html",
  styleUrls: ["./map-bottom-sheet.component.scss"],
})
export class MapBottomSheetComponent implements OnInit {
  latitude = -25.088082;
  longitude = -50.1605277;
  markers = [
    {
      lat: -25.088082,
      lng: -50.1605277,
      uuid: 1,
      label: "Felipe Koga",
      icon: {
        url:
          "https://instagram.fpgz1-1.fna.fbcdn.net/v/t51.2885-19/s150x150/106459975_719099685544296_4948485348446780484_n.jpg?_nc_ht=instagram.fpgz1-1.fna.fbcdn.net&_nc_ohc=9aPN9rZiR4QAX-6kRbO&oh=f604e9394ac4e051c9f730704677ae86&oe=5F4761A3",
        scaledSize: { height: 35, width: 35 },
      },
    },
  ];

  constructor(
    private _bottomSheetRef: MatBottomSheetRef<MapBottomSheetComponent>
  ) {}

  ngOnInit(): void {}

  openLink(event: MouseEvent) {
    this._bottomSheetRef.dismiss();
    event.preventDefault();
  }
}
