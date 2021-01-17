import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { MaterialModule } from "src/app/material.module";
import { ConfirmationDialogComponent } from "./confirmation-dialog/confirmation-dialog.component";
import { PttSnackbarComponent } from './ptt-snackbar/ptt-snackbar.component';

@NgModule({
  imports: [BrowserModule, ReactiveFormsModule, FormsModule, MaterialModule],
  declarations: [ConfirmationDialogComponent, PttSnackbarComponent],
  exports: [ConfirmationDialogComponent],
})
export class SharedModule {}
