import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { User } from "src/app/models/user";

import { ServiceEndpoint as API } from "../../../stack.json";
import { AuthService } from "../auth/auth.service";
import { StateService } from "../state.service";

export interface UsersState {
  users: User[];
  isLoading: boolean;
}

const initialState: UsersState = {
  users: [],
  isLoading: false,
};

@Injectable({
  providedIn: "root",
})
export class UsersService extends StateService<UsersState> {
  private companyId: string;

  public usersState$: Observable<UsersState>;

  constructor(private http: HttpClient, private authService: AuthService) {
    super(initialState);
    this.usersState$ = this.select((state) => state);
    this.companyId = this.authService.getUser().companyId;
  }

  public fetch() {
    this.setState({ isLoading: true });
    this.http
      .get<User[]>(`${API}/company/${this.companyId}/users`)
      .subscribe((users) => {
        this.setState({ users, isLoading: false });
      });
  }

  public add(user: User, completed?: () => void, error?: () => void) {
    this.setState({ isLoading: true });
    this.http
      .post<User>(`${API}/company/${this.companyId}/users`, user)
      .subscribe(
        (response) => {
          this.setState({
            users: [...this.state.users, response],
            isLoading: false,
          });
          completed();
        },
        () => {
          error();
        }
      );
  }

  public update(user: User, completed?: (user: User) => void) {
    this.setState({ isLoading: true });
    this.http
      .put<User>(
        `${API}/company/${this.companyId}/users/${user.username}`,
        user
      )
      .subscribe((response) => {
        this.setState({
          users: this.state.users.map((item) => {
            if (item.username === response.username) {
              return response;
            }
            return item;
          }),
          isLoading: false,
        });
        completed(response);
      });
  }

  public disable(username: string, completed?: () => void) {
    this.setState({ isLoading: true });
    return this.http
      .put<User>(
        `${API}/company/${this.companyId}/users/${username}/disable`,
        {}
      )
      .subscribe((updatedUser) => {
        completed();
        this.replaceUser(updatedUser);
      });
  }

  public replaceUser(newUser: User) {
    this.setState({
      ...this.state,
      users: this.state.users.map((item) => {
        if (item.username === newUser.username) {
          return newUser;
        }
        return item;
      }),
      isLoading: false,
    });
  }

  public get(username: string) {
    return this.http.get<User>(
      `${API}/company/${this.companyId}/users/${username}`
    );
  }
}
