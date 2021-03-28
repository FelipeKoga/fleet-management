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

        if (!authStore.isLoggedIn) {
          this.router.navigate(["/login"]);
          observer.next(false);
        } else {
          console.log(_router.data.roles);
          console.log(authStore.user.role);
          if (
            _router.data.roles.find(
              (role: string) => role === authStore.user.role
            )
          ) {
            observer.next(true);
          } else {
            observer.next(false);
          }
        }

        observer.complete();
      });
    });
  }
}
