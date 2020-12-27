import { Injectable } from "@angular/core";
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from "@angular/router";

import { Observable } from "rxjs";

import { AuthService } from "../../../services/auth/auth.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService) {}

  canActivate(
    _router: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    return this.authService.isAuthenticated();
  }
}
