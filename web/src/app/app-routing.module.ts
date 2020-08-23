import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MapComponent } from "./components/map/map.component";
import { ChatComponent } from "./components/chat/chat.component";
import { EmployeesComponent } from "./components/employees/employees.component";

const routes: Routes = [
  { path: "messages", component: ChatComponent },
  { path: "employees", component: EmployeesComponent },
  { path: "map", component: MapComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
