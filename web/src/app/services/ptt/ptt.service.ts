import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { nanoid } from "nanoid";
import { BehaviorSubject } from "rxjs";
import { User } from "src/app/models/user";
import { Actions, WebsocketService } from "../websocket/websocket.service";
@Injectable({
  providedIn: "root",
})
export class PttService {
  private context: AudioContext;
  private processor: ScriptProcessorNode;
  private source: MediaStreamAudioSourceNode;
  private chatId: string;
  private user: User;
  private receivers: string[];

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

  private setupVariables = ({ user, chatId, receivers }) => {
    this.user = user;
    this.chatId = chatId;
    this.receivers = receivers;
  };

  private setupStreaming = async () => {
    this.context = new AudioContext({
      sampleRate: 8000,
    });
    this.processor = this.context.createScriptProcessor(1024, 1, 1);
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
    });
    this.source = this.context.createMediaStreamSource(stream);
    this.source.connect(this.processor);
    this.processor.addEventListener(
      "audioprocess",
      this.audioProcessingListener
    );
    this.processor.connect(this.context.destination);
  };

  private audioStreamListener = (payload) => {
    this.audioProcessingListener = (e) => {
      this.webSocketService.sendMessage({
        action: Actions.PUSH_TO_TALK,
        body: {
          ...payload,
          inputData: e.inputBuffer.getChannelData(0).toString(),
        },
      });
    };
  };

  public async start(chatId: string, user: User, receivers: string[]) {
    this.recording$.next(true);
    this.setupVariables({ user, chatId, receivers });
    this.setupStreaming();
    const payload = {
      user: this.user,
      receivers,
      chatId,
    };
    this.webSocketService.sendMessage({
      action: Actions.PUSH_TO_TALK,
      body: {
        ...payload,
        type: Actions.START_PUSH_TO_TALK,
      },
    });

    this.audioStreamListener(payload);
  }

  private disconnectStreaming = () => {
    this.processor.removeEventListener(
      "audioprocess",
      this.audioProcessingListener,
      true
    );
    this.processor.disconnect(this.context.destination);
    this.source.disconnect();
  };

  public stop() {
    this.recording$.next(false);
    this.disconnectStreaming();
    this.webSocketService.sendMessage({
      action: Actions.PUSH_TO_TALK,
      body: {
        type: Actions.STOP_PUSH_TO_TALK,
        chatId: this.chatId,
        user: this.user,
        receivers: this.receivers,
      },
    });
  }

  public playAudio(audioBufferString: string) {
    const parsedAudioBuffer = audioBufferString
      .split(",")
      .map((value: string) => +value);
    const buf = this.context.createBuffer(1, 1024, 8000);
    const player = this.context.createBufferSource();
    buf.copyToChannel(new Float32Array(parsedAudioBuffer), 0);
    player.buffer = buf;
    player.connect(this.context.destination);
    player.start(0);
  }

  public busy(chatId: string) {
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
