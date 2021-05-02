import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { Message } from "src/app/models/message";

export interface StreamState {
  playing: boolean;
  readableCurrentTime: string;
  readableDuration: string;
  duration: number | undefined;
  currentTime: number | undefined;
  canplay: boolean;
  error: boolean;
  id: string;
}

@Injectable({
  providedIn: "root",
})
export class AudioService {
  private stop$ = new Subject();
  private audioObj = new Audio();
  audioEvents = [
    "ended",
    "error",
    "play",
    "playing",
    "pause",
    "timeupdate",
    "canplay",
    "loadedmetadata",
    "loadstart",
    "durationchange",
  ];
  private state: StreamState = {
    playing: false,
    readableCurrentTime: "",
    readableDuration: "",
    duration: undefined,
    currentTime: undefined,
    canplay: false,
    error: false,
    id: "",
  };

  private streamObservable(url: string, id: string) {
    return new Observable((observer) => {
      this.audioObj.src = url;
      this.audioObj.load();
      this.audioObj.play();

      const handler = (event: Event) => {
        this.updateStateEvents(event, id);
        observer.next(event);
      };

      this.addEvents(this.audioObj, this.audioEvents, handler);
      return () => {
        this.audioObj.pause();
        this.audioObj.currentTime = 0;
        this.removeEvents(this.audioObj, this.audioEvents, handler);
        this.resetState();
      };
    });
  }

  private addEvents(obj, events, handler) {
    events.forEach((event) => {
      obj.addEventListener(event, handler);
    });
  }

  private removeEvents(obj, events, handler) {
    events.forEach((event) => {
      obj.removeEventListener(event, handler);
    });
  }

  playStream(url: string, id: string) {
    return this.streamObservable(url, id).pipe(takeUntil(this.stop$));
  }

  play(id: string) {
    this.state = { ...this.state, id: "" };
    this.stateChange.next(this.state);
    this.state = { ...this.state, id };
    this.stateChange.next(this.state);
    this.audioObj.play();
  }

  pause() {
    this.audioObj.pause();
  }

  stop() {
    this.state = { ...this.state, id: "" };
    this.stop$.next();
  }

  seekTo(seconds) {
    this.audioObj.currentTime = seconds;
  }

  formatTime(time: number) {
    if (time === Infinity || time === NaN) {
      return "";
    }
    const minutes = (time % 3600) / 60;

    return [minutes, time % 60]
      .map((value) => `0${Math.floor(value)}`.slice(-2))
      .join(":");
  }

  private stateChange: BehaviorSubject<StreamState> = new BehaviorSubject(
    this.state
  );

  private updateStateEvents(event: Event, id: string): void {
    this.state.id = id;
    switch (event.type) {
      case "canplay":
        this.state.duration = this.audioObj.duration;
        this.state.readableDuration = this.formatTime(this.state.duration);
        this.state.canplay = true;
        break;
      case "playing":
        this.state.playing = true;
        break;
      case "pause":
        this.state.id = "";
        this.state.playing = false;
        break;
      case "timeupdate":
        this.state.currentTime = this.audioObj.currentTime;
        this.state.readableCurrentTime = this.formatTime(
          this.state.currentTime
        );
        break;
      case "loadedmetadata":
        this.state.duration = this.audioObj.duration;
        this.state.readableDuration = this.formatTime(this.state.duration);
        break;
      case "durationchange":
        if (
          this.audioObj.duration !== Infinity ||
          this.audioObj.duration !== NaN
        ) {
          this.state.duration = this.audioObj.duration;
          this.state.readableDuration = this.formatTime(this.state.duration);
        }

        break;
      case "error":
        this.resetState();
        this.state.error = true;
        break;
    }
    this.stateChange.next(this.state);
  }

  private resetState() {
    this.state = {
      ...this.state,
      playing: false,
      canplay: false,
      error: false,
      id: "",
    };
  }

  getState(): Observable<StreamState> {
    return this.stateChange.asObservable();
  }
}
