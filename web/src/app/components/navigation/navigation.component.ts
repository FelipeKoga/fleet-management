import {
  Component,
  ChangeDetectionStrategy,
  EventEmitter,
  Output,
} from "@angular/core";
import { CoreService } from "src/app/services/core/core.service";
import { ActivatedRoute, Router } from "@angular/router";
import { UserService } from "src/app/services/core/user.service";
import { ContactsService } from "src/app/services/core/contacts.service";

@Component({
  selector: "app-navigation",
  templateUrl: "./navigation.component.html",
  styleUrls: ["./navigation.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavigationComponent {
  public isDarkTheme: boolean;
  public isMenuOpen: boolean = true;

  folders = [
    {
      name: "Mapa",
      updated: new Date("1/1/16"),
    },
  ];

  users = [];

  constructor(
    private coreService: CoreService,
    private router: Router,
    private userService: UserService,
    private contactsService: ContactsService
  ) {
    this.coreService.getTheme().subscribe((theme) => {
      this.isDarkTheme = theme === "dark";
    });

    this.users = this.contactsService.getContacts();
  }

  public onThemeChange = ({ checked }): void => {
    localStorage.setItem("theme", checked ? "dark" : "light");
    this.coreService.setTheme(checked ? "dark" : "light");
  };

  public toggleMenu = () => {
    this.isMenuOpen = !this.isMenuOpen;
  };

  public navToChat = (id) => {
    const user = this.users.find((u) => {
      return u.uuid === id;
    });
    this.router.navigate(["/chat"], { queryParams: { id } });
  };

  public getAvatar = async (user) => {
    console.log(await this.userService.getAvatar(user.name).toPromise());
  };
}
