import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MapComponent } from "./components/map/map.component";
import { LoginComponent } from "./components/auth/login/login.component";
import { AuthGuard } from "./services/auth/guards/auth-guard.service";
import { ForgotPasswordComponent } from "./components/auth/forgot-password/forgot-password.component";
import { UsersComponent } from "./components/users/users.component";
import { ChatsComponent } from "./components/chats/chats.component";

const routes: Routes = [
  {
    path: "",
    children: [
      {
        path: "",
        pathMatch: "full",
        redirectTo: "/map",
      },
      { path: "map", component: MapComponent, canActivate: [AuthGuard] },
      { path: "login", component: LoginComponent },
      { path: "forgot-password", component: ForgotPasswordComponent },
      { path: "chats", component: ChatsComponent, canActivate: [AuthGuard] },
      {
        path: "users",
        component: UsersComponent,
        canActivate: [AuthGuard],
      },
    ],
  },

  {
    path: "**",
    pathMatch: "full",
    redirectTo: "map",
    canActivate: [AuthGuard],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: "legacy" })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
