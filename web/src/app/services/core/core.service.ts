import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class CoreService {
  private subTheme: BehaviorSubject<string> = new BehaviorSubject<string>(
    "light"
  );

  constructor() {
    const preferenceTheme = localStorage.getItem("theme");
    if (preferenceTheme === "dark" || preferenceTheme === "light") {
      this.subTheme.next(preferenceTheme);
    }
  }

  public getTheme = (): Observable<string> => {
    return this.subTheme.asObservable();
  };

  public setTheme = (theme: string): void => {
    this.subTheme.next(theme);
  };
}
