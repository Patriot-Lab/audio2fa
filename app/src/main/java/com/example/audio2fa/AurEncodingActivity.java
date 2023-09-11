package com.example.audio2fa;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;

public class AurEncodingActivity extends AppCompatActivity {

    private AudioTrack audioTrack;
    private int sampleRate = 44100; // Sample rate in Hz
    private int frequency = 2300; // Frequency of the square wave in Hz

    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aur_encoding);

        handler = new Handler();

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        int numSamplesPerCycle = sampleRate / frequency;
        short[] buffer = new short[numSamplesPerCycle];

        handler.post(() -> {

            // Infinite loop to generate and play the square wave
            while (true) {
                for (int i = 0; i < buffer.length; i++) {
                    if (i % numSamplesPerCycle < numSamplesPerCycle / 2) {
                        buffer[i] = Short.MAX_VALUE; // Set to maximum amplitude for the high part of the square wave
                    } else {
                        buffer[i] = Short.MIN_VALUE; // Set to minimum amplitude for the low part of the square wave
                    }
                }

                audioTrack.play();
                audioTrack.write(buffer, 0, buffer.length);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }
}