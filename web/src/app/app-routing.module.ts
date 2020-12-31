import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MapComponent } from "./components/map/map.component";
import { ChatComponent } from "./components/chat/chat.component";
import { EmployeesComponent } from "./components/employees/employees.component";
import { LoginComponent } from "./components/auth/login/login.component";
import { AuthGuard } from "./services/auth/guards/auth-guard.service";
import { ForgotPasswordComponent } from "./components/auth/forgot-password/forgot-password.component";
import { UsersComponent } from "./components/users/users.component";

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
      { path: "messages", component: ChatComponent, canActivate: [AuthGuard] },
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
