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
import { AuthService } from "src/app/services/auth/auth.service";
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
  public user: User;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private usersService: UsersService,
    private dialog: MatDialog,
    private authService: AuthService
  ) {
    this.state$ = usersService.usersState$;
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit() {
    this.user = this.authService.getUser();
    this.usersService.usersState$.subscribe((state) => {
      this.dataSource.data = state.users.filter(
        (user) => user.username !== this.user.username
      );
    });

    this.usersService.fetch();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  public openInsert() {
    this.openDialog(FormType.INSERT);
  }
  public openUpdate(user: User) {
    this.selectedUser = user;
    this.openDialog(FormType.UPDATE);
  }

  public openEnable(user: User) {
    this.selectedUser = user;
    this.openDialog(FormType.ENABLE);
  }

  public openDisable(user: User) {
    this.selectedUser = user;
    this.openDialog(FormType.DISABLE);
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
    this.selectedUser = null;
  }
}
