import { Component, Inject, OnInit } from "@angular/core";
import { MAT_SNACK_BAR_DATA } from "@angular/material/snack-bar";
import { User } from "src/app/models/user";

@Component({
  selector: "app-ptt-snackbar",
  templateUrl: "./ptt-snackbar.component.html",
  styleUrls: ["./ptt-snackbar.component.scss"],
})
export class PttSnackbarComponent implements OnInit {
  public user: User;
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: User) {}

  ngOnInit(): void {
    this.user = this.data;
  }
}
