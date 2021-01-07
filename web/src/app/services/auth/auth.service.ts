import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Auth } from "aws-amplify";
import { BehaviorSubject, Observable } from "rxjs";
import { User } from "src/app/models/user";

import { ServiceEndpoint } from "../../../stack.json";

export interface AuthServiceStore {
  user?: User;
  cognitoUser?: any;
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
    this.setStoreValue({ isLoggedIn: true, user: currentUser });
  }

  public async signIn(username: string, password: string): Promise<void> {
    this.setStoreValue({ isLoading: true, isLoggedIn: false });
    try {
      await Auth.signIn(username, password);
      await this.getCurrentUser();
    } catch (e) {
      this.setStoreValue({ isLoading: false, errorCode: e.code });
    }
  }

  public async getCurrentUser(): Promise<void> {
    try {
      this.setStoreValue({ isLoading: true });
      this.subAuthStore.next({ isLoading: true });
      const cognitoUser = await Auth.currentAuthenticatedUser();
      this.setStoreValue({ cognitoUser });
      const token = await Auth.currentSession();
      this.authToken = token.getIdToken().getJwtToken();
      const companyId = cognitoUser.attributes["custom:companyId"];
      await this.getUserFromRemote(cognitoUser.username, companyId);
      this.setStoreValue({ isLoading: false, isLoggedIn: true });
    } catch {
      this.setStoreValue({ isLoading: false, isLoggedIn: false });
    }
  }

  public async sendVerificationCode(username: string) {
    try {
      this.setStoreValue({ isLoading: true, errorCode: "" });
      await Auth.forgotPassword(username);
      this.setStoreValue({ isLoading: false });
    } catch (e) {
      this.setStoreValue({ isLoading: false, errorCode: e.code });
    }
  }

  public async changePassword(
    username: string,
    code: string,
    password: string
  ) {
    try {
      this.setStoreValue({ isLoading: true, errorCode: "" });
      await Auth.forgotPasswordSubmit(username, code, password);
      this.setStoreValue({ isLoading: false });
    } catch (e) {
      this.setStoreValue({ isLoading: false, errorCode: e.code });
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

  public getUser(): User {
    return this.subAuthStore.getValue().user;
  }

  public setUser(user: User) {
    this.subAuthStore.next({ ...this.subAuthStore.value, user });
  }

  public async userChangePassword(oldPassword, password) {
    console.log(this.subAuthStore.value.cognitoUser);
    return Auth.changePassword(
      this.subAuthStore.value.cognitoUser,
      oldPassword,
      password
    );
  }

  private setStoreValue(store: AuthServiceStore) {
    const value = { ...this.subAuthStore.getValue() };
    this.subAuthStore.next({
      ...value,
      ...store,
    });
  }
}
