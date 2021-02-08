import {
  Component,
  Input,
  OnInit,
  SimpleChanges,
  ViewChild,
} from "@angular/core";
import { Chat } from "src/app/models/chat";
import { Message, MessageStatus } from "src/app/models/message";
import { User } from "src/app/models/user";
import {
  AudioService,
  StreamState,
} from "src/app/services/audio/audio.service";
import { AuthService } from "src/app/services/auth/auth.service";
import { convertDate } from "src/app/utils/date";

@Component({
  selector: "app-message",
  templateUrl: "./message.component.html",
  styleUrls: ["./message.component.scss"],
})
export class MessageComponent implements OnInit {
  @Input() message: Message;
  @Input() chat: Chat;
  public state: StreamState;
  public user: User = new User({});

  constructor(
    private authService: AuthService,
    private audioService: AudioService
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    this.loadAudio();
    this.audioService.audioState$.subscribe((state) => {
      if (state.messageId === this.message.messageId) {
        this.state = state;
      }
    });
  }

  private loadAudio() {
    if (
      this.message.hasAudio &&
      this.message.status !== MessageStatus.PROCESSING_AUDIO
    ) {
      this.audioService.loadAudio(this.message.message);
    }
  }

  public playAudioStream() {}

  public pause() {
    this.audioService.pause();
  }

  public play() {
    this.audioService.play(this.message);
  }

  public stop() {
    this.audioService.stop();
  }

  public onSliderChangeEnd(change) {
    this.audioService.seekTo(change.value);
  }

  public convertTimestamp(timestamp: number, format: string) {
    return convertDate(timestamp, format);
  }

  public getName(username: string) {
    return this.chat.members.find((user) => user.username === username).name;
  }
}
