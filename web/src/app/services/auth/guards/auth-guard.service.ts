import { Injectable } from "@angular/core";
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
} from "@angular/router";

import { Observable } from "rxjs";

import { AuthService } from "../../../services/auth/auth.service";

enum AuthStack {
  LOGIN = "/login",
  FORGOT_PASSWORD = "/forgot-password",
}
@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    _router: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    return new Observable<boolean>((observer) => {
      this.authService.authStore().subscribe((authStore) => {
        if (authStore.isLoading) return;

        if (authStore.isLoggedIn) {
          if (_state.url === "/login") {
            console.log("rediret map");
            this.router.navigate(["/map"]);
          }
          observer.next(true);
        } else {
          console.log(_state);
          if (
            _state.url !== AuthStack.LOGIN &&
            _state.url !== AuthStack.FORGOT_PASSWORD
          ) {
            this.router.navigate(["/login"]);
          }
          observer.next(false);
        }

        observer.complete();
      });
    });
  }
}
