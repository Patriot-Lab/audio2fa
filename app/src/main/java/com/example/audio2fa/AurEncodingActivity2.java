package com.example.audio2fa;


import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AurEncodingActivity2 extends AppCompatActivity {


    Handler handler;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final String TAG = AurEncodingActivity2.class.getName();

    short[] buffer;

    private static boolean play = false;

    private AudioTrack audioTrack;
    private int sampleRate = 47500; // Sample rate in Hz
    private int frequency = 9500; // Frequency of the square wave in Hz
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aur_encoding2);

        handler = new Handler();

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        int numSamplesPerCycle = sampleRate / frequency;
        buffer = new short[numSamplesPerCycle];

        for (int i = 0; i < buffer.length; i++) {
            if (i % numSamplesPerCycle < numSamplesPerCycle / 2) {
                buffer[i] = Short.MAX_VALUE; // Set to maximum amplitude for the high part of the square wave
            } else {
                buffer[i] = Short.MIN_VALUE; // Set to minimum amplitude for the low part of the square wave
            }
        }

        String data = "123456789";
        handler.post(() -> sendDataOverVoice(data.getBytes()));

        playSound();
    }

    public void playSound(){
        if(play)
        {
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.play();
        }
        executorService.execute(this::playSound);
    }

    public void stopSound(){
        play = false;
    }

    public void sendDataOverVoice(byte[] bytes){

            for(byte b: bytes){
                play = true;
                delay(10);
                play = false;
                for (int i = 7; i >= 0; i--) {
                    int bit = getBit(b, i);
                    play = true;
                    if(bit == 1){
                        delay(2);
                    }else{
                        delay(4);
                    }
                    play = false;
                    delay(11);
                }
            }
//            handler.post(()->sendDataOverVoice(bytes));

    }

    public void delay(int duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public int getBit(byte myByte, int position){
        return (myByte >> position) & 0x01;
    }
}