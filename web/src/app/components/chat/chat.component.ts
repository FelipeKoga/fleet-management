import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
} from "@angular/core";
import { ActivatedRoute } from "@angular/router";
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
      message: ``,
      uuid: 1,
    },
    {
      message: ``,
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
