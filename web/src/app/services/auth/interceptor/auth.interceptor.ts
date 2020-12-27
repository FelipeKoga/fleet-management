import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from "@angular/common/http";
import { Injectable, ɵConsole } from "@angular/core";
import { Router } from "@angular/router";

import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

import { AuthService } from "src/app/services/auth/auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    console.log("SHOW DE BOLA");
    console.log(this.authService.isAuthenticated());
    if (this.authService.isAuthenticated()) {
      const authRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${this.authService.getAuthToken()}`,
        },
      });
      console.log(`Bearer ${this.authService.getAuthToken()}`);
      return next.handle(authRequest).pipe(
        catchError((err) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 401) {
              this.authService.signOut();
              this.router.navigate(["/login"]);
            }
          }
          return throwError(err);
        })
      );
    }
    return next.handle(request);
  }
}
