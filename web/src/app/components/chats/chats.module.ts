import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { MaterialModule } from "src/app/material.module";
import { ChatComponent } from "./chat/chat.component";
import { ChatsComponent } from "./chats.component";
import { NewChatComponent } from "./new-chat/new-chat.component";
import { DetailsComponent } from "./chat/details/details.component";
import { AddMemberComponent } from "./chat/add-member/add-member.component";
import { MessageComponent } from "./chat/message/message.component";
import { LazyLoadImageModule } from "ng-lazyload-image";

@NgModule({
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialModule,
    LazyLoadImageModule,
  ],
  declarations: [
    ChatsComponent,
    ChatComponent,
    NewChatComponent,
    DetailsComponent,
    AddMemberComponent,
    MessageComponent,
  ],
  exports: [ChatsComponent, ChatComponent],
})
export class ChatsModule {}
