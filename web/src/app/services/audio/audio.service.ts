import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, Subject } from "rxjs";
import { Message } from "src/app/models/message";
import { Stream } from "stream";
import { StateService } from "../state.service";

export interface StreamState {
  messageId: string;
  playing: boolean;
  readableCurrentTime: string;
  readableDuration: string;
  duration: number | undefined;
  currentTime: number | undefined;
  canplay: boolean;
  error: boolean;
}

const initialState: StreamState = {
  playing: false,
  readableCurrentTime: "",
  readableDuration: "",
  duration: undefined,
  currentTime: undefined,
  canplay: false,
  error: false,
  messageId: "",
};

@Injectable({
  providedIn: "root",
})
export class AudioService extends StateService<StreamState> {
  private audio = new Audio();
  public audioState$: Observable<StreamState>;

  constructor() {
    super(initialState);
    this.audioState$ = this.select((state) => state);
  }

  public loadAudio(url: string) {
    this.audio.src = url;
    this.audio.load();

    this.audio.addEventListener("canplay", (e) => {
      this.setState({
        ...this.state,
        duration: this.audio.duration,
        readableDuration: this.formatTime(this.audio.duration),
      });
    });

    this.audio.addEventListener("loadedmetadata", (e) => {
      this.setState({
        ...this.state,
        duration: this.audio.duration,
        readableDuration: this.formatTime(this.audio.duration),
      });
    });

    this.audio.addEventListener(
      "durationchange",
      () => {
        this.setState({
          ...this.state,
          duration: this.audio.duration,
          readableDuration: this.formatTime(this.audio.duration),
        });
      },
      false
    );

    this.audio.addEventListener("timeupdate", (e) => {
      this.setState({
        ...this.state,
        currentTime: this.audio.currentTime,
        readableCurrentTime: this.formatTime(this.state.currentTime),
      });
    });

    this.audio.addEventListener("pause", (e) => {
      this.setState({
        ...this.state,
        playing: false,
      });
    });
  }

  public play({ messageId, message }: Message) {
    this.setState({
      ...this.state,
      playing: false,
    });

    if (messageId !== this.state.messageId) {
      this.stop();
    }

    if (this.audio.src !== message) {
      this.loadAudio(message);
    }

    this.setState({
      ...this.state,
      messageId,
    });

    let time = 0;
    if (this.audio.currentTime !== this.audio.duration) {
      time = this.audio.currentTime;
    }

    this.audio.play();
    this.setState({ ...this.state, playing: true });
  }

  public pause() {
    this.setState({
      ...this.state,
      playing: false,
    });
    this.audio.pause();
  }

  public stop() {
    this.pause();
    this.audio.currentTime = 0;

    this.setState({
      ...this.state,
      messageId: "",
    });
  }

  public seekTo(time: number) {
    this.audio.currentTime = time;
  }

  private formatTime(time: number) {
    if (time === Infinity || time === NaN) {
      return "";
    }
    const minutes = (time % 3600) / 60;

    return [minutes, time % 60]
      .map((value) => `0${Math.floor(value)}`.slice(-2))
      .join(":");
  }
}
