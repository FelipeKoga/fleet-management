import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { MaterialModule } from "src/app/material.module";
import { ChatComponent } from "./chat/chat.component";
import { ChatsComponent } from "./chats.component";

@NgModule({
  imports: [BrowserModule, ReactiveFormsModule, FormsModule, MaterialModule],
  declarations: [ChatsComponent, ChatComponent],
  exports: [ChatsComponent, ChatComponent],
})
export class ChatsModule {}
