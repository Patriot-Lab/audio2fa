package com.example.audio2fa;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToneGenActivity extends AppCompatActivity {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final int frequency = 9500;

    private final int sampleRate = 44100;

    private byte[] generatedSnd;
    private AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone_gen);

        generateSnd("123456789");

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);


        executorService.execute(()->{

            while(true)
            {
                playTrack();
            }


        });
    }

    private void generateSnd(String data) {

        byte[] dataArr = data.getBytes();


        byte[] tempByte = new byte[0];

        for (byte dataItem : dataArr) {

            double bitDuration;
            byte[] byteNote;

            bitDuration = 0.01;
            byteNote = getTone(bitDuration, sampleRate, 0);
            tempByte = concat(byteNote, tempByte);

            for (int i = 7; i >= 0; i--) {

                int bit = getBit(dataItem, i);

                if (bit == 1) {
                    bitDuration = 0.002;
                } else {
                    bitDuration = 0.004;
                }

                byteNote = getTone(bitDuration, sampleRate, frequency);
                tempByte = concat(byteNote, tempByte);

                bitDuration = 0.011;
                byteNote = getTone(bitDuration, sampleRate, 0);
                tempByte = concat(byteNote, tempByte);

            }
        }

        generatedSnd = tempByte;

    }

    public int getBit(byte myByte, int position) {
        return (myByte >> position) & 0x01;
    }

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private byte[] getTone(double duration, int rate, double frequencies) {

        int maxLength = (int) (duration * rate);
        Log.d(TAG, "getTone: " + maxLength);
        byte[] generatedTone = new byte[2 * maxLength];

        double[] sample = new double[maxLength];
        int idx = 0;

        for (int x = 0; x < maxLength; x++) {
            sample[x] = sine(x, frequencies / rate);
        }


        for (final double dVal : sample) {

            final short val = (short) ((dVal * 32767));

            // in 16 bit wav PCM, first byte is the low order byte
            generatedTone[idx++] = (byte) (val & 0x00ff);
            generatedTone[idx++] = (byte) ((val & 0xff00) >>> 8);

        }

        return generatedTone;
    }

    private double sine(int x, double frequencies) {
        double sineValue = Math.sin(2 * Math.PI * x * frequencies);

        //convert sine to square
        return sineValue >= 0 ? 1.0 : -1.0;

    }

    void playTrack() {

        audioTrack.play();
        audioTrack.write(generatedSnd, 0, generatedSnd.length);


    }
}