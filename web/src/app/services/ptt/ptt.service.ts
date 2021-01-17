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
  private mediaRecorder;

  private audioProcessingListener: (
    this: ScriptProcessorNode,
    ev: AudioProcessingEvent
  ) => any;

  public recording$ = new BehaviorSubject<boolean>(false);
  public receiving$ = new BehaviorSubject<boolean>(false);

  constructor(
    private webSocketService: WebsocketService,
    private http: HttpClient
  ) {
    this.context = new AudioContext({ sampleRate: 8000 });
  }

  public async start(
    chatId: string,
    username: string,
    onStop: (blob: Blob) => void
  ) {
    this.user = new User({ username });
    this.chatId = chatId;
    this.recording$.next(true);
    this.webSocketService.sendMessage({
      action: Actions.PUSH_TO_TALK,
      body: {
        type: Actions.START_PUSH_TO_TALK,
        chatId,
        username: this.user.username,
      },
    });

    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
    });

    this.source = this.context.createMediaStreamSource(stream);
    this.audioProcessingListener = (e) => {
      this.webSocketService.sendMessage({
        action: Actions.PUSH_TO_TALK,
        body: {
          chatId,
          username: this.user.username,
          inputData: e.inputBuffer.getChannelData(0).toString(),
          length: e.inputBuffer.length,
        },
      });
    };
    this.processor = this.context.createScriptProcessor(1024, 1, 1);
    this.source.connect(this.processor);
    this.processor.connect(this.context.destination);
    this.processor.addEventListener(
      "audioprocess",
      this.audioProcessingListener
    );
    this.startMediaRecorder(stream, onStop);
  }

  public stop() {
    this.recording$.next(false);
    this.mediaRecorder.stop();
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
      },
    });
  }

  public playAudio(chatId: string, audioBufferString, length: number) {
    if (this.busy(chatId)) return;
    const parsedAudioBuffer = audioBufferString
      .split(",")
      .map((value: string) => +value);

    const buf = this.context.createBuffer(1, length, 8000);
    buf.getChannelData(0).set(new Float32Array(parsedAudioBuffer));

    // buf.copyToChannel(new Float32Array(parsedAudioBuffer), 0);
    const player = this.context.createBufferSource();
    player.buffer = buf;
    player.connect(this.context.destination);
    player.start(0);
  }

  public busy(chatId: string) {
    return this.chatId !== chatId && this.receiving$.value;
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
}
