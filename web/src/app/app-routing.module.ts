import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MapComponent } from "./components/map/map.component";
import { LoginComponent } from "./components/auth/login/login.component";
import { AuthGuard } from "./services/auth/guards/auth-guard.service";
import { ForgotPasswordComponent } from "./components/auth/forgot-password/forgot-password.component";
import { UsersComponent } from "./components/users/users.component";
import { ChatsComponent } from "./components/chats/chats.component";
import { ProfileComponent } from "./components/profile/profile.component";
import { NotFoundComponent } from "./components/not-found/not-found.component";
import { UserRole } from "./models/user";

const routes: Routes = [
  {
    path: "",
    redirectTo: "map",
    pathMatch: "full",
  },
  { path: "login", component: LoginComponent },
  { path: "forgot-password", component: ForgotPasswordComponent },
  {
    path: "map",
    component: MapComponent,
    data: {
      roles: [UserRole.ADMIN, UserRole.EMPLOYEEE, UserRole.OPERATOR],
    },
    canActivate: [AuthGuard],
  },
  {
    path: "profile",
    component: ProfileComponent,
    data: {
      roles: [UserRole.ADMIN, UserRole.EMPLOYEEE, UserRole.OPERATOR],
    },
    canActivate: [AuthGuard],
  },
  {
    path: "chats",
    component: ChatsComponent,
    data: {
      roles: [UserRole.ADMIN, UserRole.EMPLOYEEE, UserRole.OPERATOR],
    },
    canActivate: [AuthGuard],
  },
  {
    path: "users",
    component: UsersComponent,
    data: {
      roles: [UserRole.ADMIN, UserRole.OPERATOR],
    },
    canActivate: [AuthGuard],
  },

  {
    path: "**",
    component: NotFoundComponent,
    data: {
      roles: [UserRole.ADMIN, UserRole.EMPLOYEEE, UserRole.OPERATOR],
    },
    canActivate: [AuthGuard],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: "legacy" })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
