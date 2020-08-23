import { Component, HostBinding } from "@angular/core";
import { CoreService } from "./services/core/core.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent {
  private isDarkMode: boolean = true;

  constructor(private coreService: CoreService) {
    this.coreService.getTheme().subscribe((theme) => {
      this.isDarkMode = theme === "dark";
    });
  }

  public switchMode = (isDark: boolean) => {
    this.isDarkMode = isDark;
  };

  @HostBinding("class")
  get theme() {
    return this.isDarkMode ? "theme-dark" : "theme-light";
  }
}
