import { Injectable } from "@angular/core";
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
} from "@angular/router";

import { Observable } from "rxjs";

import { AuthService } from "../../../services/auth/auth.service";

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
            this.router.navigate(["/map"]);
          }
          observer.next(true);
        } else {
          this.router.navigate(["/login"]);
          observer.next(false);
        }

        observer.complete();
      });
    });
  }
}
