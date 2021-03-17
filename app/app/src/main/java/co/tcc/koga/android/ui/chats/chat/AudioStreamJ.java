package co.tcc.koga.android.ui.chats.chat;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import okhttp3.WebSocket;

public class AudioStream {

    private AudioStreamMetadata metadata;
    private AudioRecord recorder;
    private volatile boolean hasStopped;

    public AudioStream() {
        metadata = AudioStreamMetadata.getDefault();
    }

    public void start() {
        hasStopped = false;
        initRecorder();
        recorder.startRecording();
        System.out.println("START RECORDING");

        new Thread(() -> {
            while (!hasStopped) {
                float[] data = new float[metadata.getBufferSize()];
                System.out.println(metadata.getBufferSize());
                System.out.println(data.length);
                recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                System.out.println(data[0]);
            }

        }).start();

    }

    public void stop() {
        hasStopped = true;
        recorder.stop();
    }

    private void initRecorder() {
        System.out.println(metadata.toString());
        int min = AudioRecord.getMinBufferSize(metadata.getSampleRate(), metadata.getChannels(true), metadata.getEncoding());
        metadata.setBufferSize(min);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, metadata.getSampleRate(),
                metadata.getChannels(true), metadata.getEncoding(), min);
    }
}
