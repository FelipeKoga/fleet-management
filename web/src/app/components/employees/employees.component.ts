import { Component, OnInit } from "@angular/core";
import {
  trigger,
  state,
  transition,
  animate,
  style,
} from "@angular/animations";

export interface User {
  position: number;
  name: string;
  email: string;
  role: string;
  phone: string;
}

const ELEMENT_DATA: User[] = [
  {
    position: 1,
    name: "Usuário 1",
    email: "usuario1@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 2,
    name: "Usuário 2",
    email: "usuario2@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 3,
    name: "Usuário 3",
    email: "usuario3@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 4,
    name: "Usuário 4",
    email: "usuario4@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 5,
    name: "Usuário 5",
    email: "usuario5@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 6,
    name: "Usuário 6",
    email: "usuario6@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
  {
    position: 7,
    name: "Usuário 7",
    email: "usuario7@email.com",
    role: "Funcionário",
    phone: `(42) 999991093`,
  },
];

@Component({
  selector: "app-employees",
  templateUrl: "./employees.component.html",
  styleUrls: ["./employees.component.scss"],
  animations: [
    trigger("detailExpand", [
      state("collapsed", style({ height: "0px", minHeight: "0" })),
      state("expanded", style({ height: "*" })),
      transition(
        "expanded <=> collapsed",
        animate("225ms cubic-bezier(0.4, 0.0, 0.2, 1)")
      ),
    ]),
  ],
})
export class EmployeesComponent implements OnInit {
  dataSource = ELEMENT_DATA;
  columnsToDisplay = ["name", "email", "phone", "actions"];
  constructor() {}

  ngOnInit(): void {
    console.log("Employees");
  }
}
