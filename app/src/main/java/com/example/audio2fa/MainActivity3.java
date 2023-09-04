package com.example.audio2fa;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;

public class MainActivity3 extends AppCompatActivity {

    private AudioTrack audioTrack;

    private boolean isPlaying = false;
    private Thread playbackThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);






        // Initialize the AudioTrack
        int sampleRate = 44100; // standard sample rate
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        if (!isPlaying) {
            int frequency = 60000;
            int duration = 4000;

            playTone(frequency, duration);
        }
    }

    private void playTone(final int frequency, final int duration) {
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isPlaying = true;
                int numSamples = duration * audioTrack.getSampleRate() / 1000;
                double[] sample = new double[numSamples];
                byte[] buffer = new byte[2 * numSamples];

                for (int i = 0; i < numSamples; ++i) {
                    sample[i] = i%2==0?0:Math.sin(2 * Math.PI * i / (audioTrack.getSampleRate() / (double) frequency));
                    short val = (short) (sample[i] * Short.MAX_VALUE);
                    buffer[2 * i] = (byte) (val & 0x00FF);
                    buffer[2 * i + 1] = (byte) ((val & 0xFF00) >> 8);
                }

                audioTrack.write(buffer, 0, buffer.length);
                audioTrack.play();

                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                audioTrack.stop();
                audioTrack.flush();
                audioTrack.release();
                isPlaying = false;

            }
        });

        playbackThread.start();
    }

    private void stopTone() {
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
            playbackThread = null;
        }
        audioTrack.stop();
        audioTrack.flush();
        audioTrack.release();
        isPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTone();
    }
}