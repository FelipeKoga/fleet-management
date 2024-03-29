import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";

import { AppComponent } from "./app.component";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MaterialModule } from "./material.module";
import { LayoutModule } from "@angular/cdk/layout";
import { AppRoutingModule } from "./app-routing.module";
import { MapComponent } from "./components/map/map.component";
import { AgmCoreModule } from "@agm/core";
import { AgmDirectionModule } from "agm-direction";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { MapBottomSheetComponent } from "./components/map/map-bottom-sheet/map-bottom-sheet.component";
import { MatPaginatorIntl } from "@angular/material/paginator";
import { getDutchPaginatorIntl } from "./utils/getPortuguesePaginatorIntl";
import { LoginComponent } from "./components/auth/login/login.component";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AuthInterceptor } from "./services/auth/interceptor/auth.interceptor";
import { ForgotPasswordComponent } from "./components/auth/forgot-password/forgot-password.component";
import { AuthGuard } from "./services/auth/guards/auth-guard.service";
import { UsersComponent } from "./components/users/users.component";
import { FormDialogComponent } from "./components/users/form-dialog/form-dialog.component";
import { ChatsModule } from "./components/chats/chats.module";
import { SharedModule } from "./shared/shared.module";
import { ProfileComponent } from "./components/profile/profile.component";
import { NotFoundComponent } from "./components/not-found/not-found.component";
import { AgmOverlays } from "agm-overlays";

@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    MapBottomSheetComponent,
    LoginComponent,
    ForgotPasswordComponent,
    UsersComponent,
    FormDialogComponent,
    ProfileComponent,
    NotFoundComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    LayoutModule,
    ChatsModule,
    SharedModule,
    MaterialModule,
    AppRoutingModule,
    HttpClientModule,
    AgmCoreModule.forRoot({
      apiKey: "",
    }),
    AgmOverlays,

    AgmDirectionModule,
  ],
  providers: [
    AuthGuard,
    { provide: MatPaginatorIntl, useValue: getDutchPaginatorIntl() },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
