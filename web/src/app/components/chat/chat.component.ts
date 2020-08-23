import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  ViewChildren,
  QueryList,
} from "@angular/core";
import { Route } from "@angular/compiler/src/core";
import { Router, ActivatedRoute } from "@angular/router";
import { map } from "rxjs/operators";
import { ContactsService } from "src/app/services/core/contacts.service";
import { MatBottomSheet } from "@angular/material/bottom-sheet";
import { MapBottomSheetComponent } from "../map/map-bottom-sheet/map-bottom-sheet.component";

@Component({
  selector: "app-chat",
  templateUrl: "./chat.component.html",
  styleUrls: ["./chat.component.scss"],
})
export class ChatComponent implements OnInit {
  @ViewChild("messageList") private myScrollContainer: ElementRef;
  contacts = [];
  public uuid = 0;
  public messages = [
    {
      message: `          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec varius
    tincidunt iaculis. Nunc erat massa, venenatis at ex sit amet, pulvinar
    suscipit orci. In id dui efficitur, dictum tortor ut, mattis neque.
    Nam pellentesque blandit tortor. Aenean viverra lobortis rhoncus.
    Etiam convallis bibendum magna dapibus lacinia. Pellentesque ac felis
    eu enim mattis fringilla. Pellentesque hendrerit suscipit nulla, id
    iaculis nunc tempus pharetra. Vivamus pharetra euismod neque, accumsan
    fermentum nunc fringilla sit amet. Cras ut tellus a massa pretium
    gravida eget ac ante. Nulla cursus non lectus sed commodo. Suspendisse
    tincidunt rhoncus risus, id facilisis libero accumsan et. Etiam vitae
    risus convallis, malesuada risus et, scelerisque nunc. Integer
    vehicula nec risus sit amet lacinia. Ut ultrices leo ligula, in
    fringilla leo feugiat at.`,
      uuid: 1,
    },
    {
      message: `          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec varius
    tincidunt iaculis. Nunc erat massa, venenatis at ex sit amet, pulvinar
    suscipit orci. In id dui efficitur, dictum tortor ut, mattis neque.
    Nam pellentesque blandit tortor. Aenean viverra lobortis rhoncus.
    Etiam convallis bibendum magna dapibus lacinia. Pellentesque ac felis
    eu enim mattis fringilla. Pellentesque hendrerit suscipit nulla, id
    iaculis nunc tempus pharetra. Vivamus pharetra euismod neque, accumsan
    fermentum nunc fringilla sit amet. Cras ut tellus a massa pretium
    gravida eget ac ante. Nulla cursus non lectus sed commodo. Suspendisse
    tincidunt rhoncus risus, id facilisis libero accumsan et. Etiam vitae
    risus convallis, malesuada risus et, scelerisque nunc. Integer
    vehicula nec risus sit amet lacinia. Ut ultrices leo ligula, in
    fringilla leo feugiat at.`,
      uuid: 2,
    },
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private contactsService: ContactsService,
    private _bottomSheet: MatBottomSheet
  ) {
    this.contacts = this.contactsService.getContacts();
  }

  openBottomSheetMap() {
    console.log("Open");
    this._bottomSheet.open(MapBottomSheetComponent);
  }

  ngOnInit() {
    this.uuid = 2;

    this.scrollToBottom();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  public sendMessage = (message) => {
    console.log("MESSAGE");
    this.messages.push({
      message,
      uuid: this.uuid,
    });
  };
}
