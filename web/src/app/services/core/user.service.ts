import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: "root",
})
export class UserService {
  private readonly AVATAR_URL = "https://ui-avatars.com/api/";

  constructor(private http: HttpClient) {}

  public getAvatar = (name) => {
    return this.http.get(`${this.AVATAR_URL}?name=Felipe+Koga`);
  };
}
