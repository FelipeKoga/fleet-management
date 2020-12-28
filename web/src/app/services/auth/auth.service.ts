import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Auth } from "aws-amplify";
import { BehaviorSubject, Observable } from "rxjs";

import { ServiceEndpoint } from "../../../stack.json";

import { User } from "../../interfaces/user";

export interface AuthServiceStore {
  user?: User;
  isLoggedIn?: boolean;
  isLoading?: boolean;
  errorCode?: string;
}

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private subAuthStore: BehaviorSubject<AuthServiceStore> = new BehaviorSubject<AuthServiceStore>(
    {}
  );
  private authToken: string = "";

  constructor(private http: HttpClient) {}

  public async getUserFromRemote(
    username: string,
    companyId: string
  ): Promise<void> {
    const currentUser = await this.http
      .get<User>(`${ServiceEndpoint}/company/${companyId}/users/${username}`)
      .toPromise();
    console.log(currentUser);
    this.subAuthStore.next({ isLoggedIn: true, user: currentUser });
  }

  public async signIn(username: string, password: string): Promise<void> {
    this.subAuthStore.next({ isLoading: true, isLoggedIn: false });
    try {
      await Auth.signIn(username, password);
      await this.getCurrentUser();
    } catch (e) {
      this.subAuthStore.next({
        isLoading: false,
        errorCode: e.code,
      });
    }
  }

  public async getCurrentUser(): Promise<void> {
    try {
      this.subAuthStore.next({ isLoading: true });
      const cognitoUser = await Auth.currentAuthenticatedUser();
      const token = await Auth.currentSession();
      this.authToken = token.getIdToken().getJwtToken();
      const companyId = cognitoUser.attributes["custom:companyId"];
      await this.getUserFromRemote(cognitoUser.username, companyId);
      this.subAuthStore.next({
        isLoading: false,
        isLoggedIn: true,
      });
    } catch {
      this.subAuthStore.next({
        isLoading: false,
        isLoggedIn: false,
      });
    }
  }

  public async signOut() {
    await Auth.signOut();
    this.authToken = "";
    this.subAuthStore.next({ user: null });
  }

  public getAuthToken(): string {
    return this.authToken;
  }

  public authStore(): Observable<AuthServiceStore> {
    return this.subAuthStore.asObservable();
  }
}
