import {
  AfterViewInit,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { Observable } from "rxjs";
import { User } from "src/app/models/user";
import { UsersService, UsersState } from "src/app/services/users/users.service";
import {
  FormDialogComponent,
  FormType,
} from "./form-dialog/form-dialog.component";

@Component({
  selector: "app-users",
  templateUrl: "./users.component.html",
  styleUrls: ["./users.component.scss"],
})
export class UsersComponent implements OnInit, AfterViewInit {
  public columnsToDisplay = ["name", "email", "phone", "role", "actions"];
  public selectedUser: User;
  public state$: Observable<UsersState>;
  public dataSource = new MatTableDataSource<User>([]);
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private usersService: UsersService, private dialog: MatDialog) {
    this.state$ = usersService.usersState$;
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit() {
    this.usersService.usersState$.subscribe((state) => {
      this.dataSource.data = state.users;
    });

    this.usersService.fetch();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  public openInsert() {
    this.openDialog(FormType.INSERT);
  }

  public openUpdate() {
    this.openDialog(FormType.UPDATE);
    this.selectedUser = null;
  }

  public openDelete() {
    this.openDialog(FormType.DELETE);
    this.selectedUser = null;
  }

  public applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  private openDialog(type: FormType) {
    this.dialog.open(FormDialogComponent, {
      width: "500px",
      data: {
        type: type,
        user: this.selectedUser,
      },
    });
  }
}
