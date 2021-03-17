import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { nanoid } from "nanoid";
import { BehaviorSubject } from "rxjs";
import { User } from "src/app/models/user";
import { Actions, WebsocketService } from "../websocket/websocket.service";
declare var MediaRecorder: any;

const mime = ["audio/wav", "audio/mpeg", "audio/webm", "audio/ogg"].filter(
  MediaRecorder.isTypeSupported
)[0];
@Injectable({
  providedIn: "root",
})
export class PttService {
  private context: AudioContext;
  private processor: ScriptProcessorNode;
  private source: MediaStreamAudioSourceNode;
  private chatId: string;
  private user: User;
  private members: string[];
  private receiver: User;
  private mediaRecorder;
  private variable: AudioWorkletNode;

  private audioProcessingListener: (
    this: ScriptProcessorNode,
    ev: AudioProcessingEvent
  ) => any;

  public recording$ = new BehaviorSubject<boolean>(false);
  public receiving$ = new BehaviorSubject<boolean>(false);

  constructor(
    private webSocketService: WebsocketService,
    private http: HttpClient
  ) {}

  public async start(
    chatId: string,
    user: User,
    receiver: User,
    members: string[],
    onStop: (blob: Blob) => void
  ) {
    console.log("START PUSH TO TALK...");
    this.user = user;
    this.chatId = chatId;
    this.members = members;
    this.receiver = receiver;
    this.context = new AudioContext({
      sampleRate: 8000,
    });
    this.processor = this.context.createScriptProcessor(0, 1, 1);

    this.recording$.next(true);
    this.webSocketService.sendMessage({
      action: Actions.PUSH_TO_TALK,
      body: {
        type: Actions.START_PUSH_TO_TALK,
        chatId,
        username: this.user.username,
        members,
        receiver: this.receiver.username,
      },
    });

    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
    });
    this.audioProcessingListener = (e) => {
      console.log("AUDIO PROCESSING", e);
      this.webSocketService.sendMessage({
        action: Actions.PUSH_TO_TALK,
        body: {
          chatId,
          username: this.user.username,
          inputData: e.inputBuffer.getChannelData(0).toString(),
          length: e.inputBuffer.length,
          members,
          receiver: receiver.username,
        },
      });
    };

    this.source = this.context.createMediaStreamSource(stream);
    this.source.connect(this.processor);
    this.processor.addEventListener(
      "audioprocess",
      this.audioProcessingListener
    );
    this.processor.connect(this.context.destination);

    // this.startMediaRecorder(stream, onStop);
  }

  public stop() {
    console.log("STOP!");
    if (this.recording$.value) {
      this.recording$.next(false);
      // this.mediaRecorder.stop();
      this.processor.removeEventListener(
        "audioprocess",
        this.audioProcessingListener,
        true
      );
      this.processor.disconnect(this.context.destination);
      this.source.disconnect();
      this.webSocketService.sendMessage({
        action: Actions.PUSH_TO_TALK,
        body: {
          type: Actions.STOP_PUSH_TO_TALK,
          chatId: this.chatId,
          username: this.user.username,
          members: this.members,
          receiver: this.receiver.username,
        },
      });
    }
  }

  public playAudio(audioBufferString: string, length: number) {
    const parsedAudioBuffer = audioBufferString
      .split(",")
      .map((value: string) => +value);

    const buf = this.context.createBuffer(1, length, 8000);
    console.log(parsedAudioBuffer);
    buf.copyToChannel(new Float32Array(parsedAudioBuffer), 0);
    const player = this.context.createBufferSource();
    player.buffer = buf;
    player.connect(this.context.destination);
    console.log("START!");
    player.start(0);
  }

  public busy(chatId: string) {
    console.log(this.chatId !== chatId && this.receiving$.value);
    return this.chatId && this.chatId !== chatId && this.receiving$.value;
  }

  public uploadAudio(blob: Blob) {
    const key = `company/${this.user.companyId}/chat/${this.chatId}/${
      this.user.username
    }/audios/${nanoid()}.wav`;
    this.http
      .post(
        `https://2p8b6trvua.execute-api.us-east-1.amazonaws.com/dev/files`,
        {
          bucket: "tcc-project-assets",
          key,
        }
      )
      .subscribe(async (response: { getURL: string; putURL: string }) => {
        const { getURL, putURL } = response;
        fetch(putURL, {
          method: "PUT",
          body: blob,
        }).then(() => {});
      });
  }

  private startMediaRecorder(
    stream: MediaStream,
    onStop: (blob: Blob) => void
  ) {
    this.mediaRecorder = new MediaRecorder(stream, {
      mimeType: mime,
    });
    this.mediaRecorder.start();
    const audioChunks = [];
    this.mediaRecorder.addEventListener("dataavailable", (event) => {
      audioChunks.push(event.data);
    });

    this.mediaRecorder.addEventListener("stop", async () => {
      const audioBlob = new Blob(audioChunks);
      onStop(audioBlob);
    });
  }

  public startReceivingPushToTalk(chatId: string) {
    if (this.busy(chatId)) return;
    this.context = new AudioContext({
      sampleRate: 8000,
    });
    this.chatId = chatId;
    this.receiving$.next(true);
  }

  public stopReceivingPushToTalk() {
    this.receiving$.next(false);
  }
}
