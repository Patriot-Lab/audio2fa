package com.example.audio2fa;


import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity2 extends AppCompatActivity {

    private double mInterval = 0.01;
    private int mSampleRate = 44100;

    private int byteDelay = 10;
    private byte[] generatedSnd;

    private final double mStandardFreq = 2000;

    Handler handler = new Handler();
    private AudioTrack audioTrack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText) findViewById(R.id.editTextText);
        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(view -> {
            String text = editText.getText().toString();
            if(text.length() > 0){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getBinary(text);
                        byte[] tempByte = new byte[0];
                        for (int i = 0; i < 16 ; i++ ){
                            double note = getNoteFrequencies(i);
                            byte[] tonByteNote = getTone(mInterval, mSampleRate, i%2==0?9000:0);
                            tempByte = concat(tonByteNote, tempByte);
                        }
                        generatedSnd = tempByte;

                        handler.post(new Runnable() {
                            public void run() {
                                playTrack(generatedSnd);
                            }
                        });
                    }
                });
            }
        });
    }

    public Integer[] getBinary(String s){
        char[] c = s.toCharArray();
        String[] bits = new String[c.length];
        for(int i = 0 ; i < c.length ; i++){
            bits[i] = Integer.toBinaryString(c[i]);

            Log.d(TAG, "getBinary: "+c[i]+" "+(int)c[i] +" " +bits[i]);
        }
        StringBuilder ds = new StringBuilder();
        for(String st: bits){

            ds.append(st);
        }
        Log.d(TAG, "getBinary: "+ ds);
        return new Integer[0];
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {

                byte[] tempByte = new byte[0];
                for (int i = 0; i < 36 ; i++ ){
                    double note = getNoteFrequencies(i);
                    byte[] tonByteNote = getTone(mInterval, mSampleRate, i%2==0?9000:4500);
                    tempByte = concat(tonByteNote, tempByte);
                }
                generatedSnd = tempByte;

                handler.post(new Runnable() {
                    public void run() {
                        playTrack(generatedSnd);
                    }
                });
            }
        });
        thread.start();
    }

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private double getNoteFrequencies(int index){
        return mStandardFreq * Math.pow(2, (double) index/12.0d);
    }

    private byte[] getTone(double duration, int rate, double frequencies){

        int maxLength = (int)(duration * rate);
        Log.d(TAG, "getTone: "+maxLength);
        byte generatedTone[] = new byte[2 * maxLength];

        double[] sample = new double[maxLength];
        int idx = 0;

        for (int x = 0; x < maxLength; x++){
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

    private AudioTrack getAudioTrack(int length){

        if (audioTrack == null)
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, length,
                    AudioTrack.MODE_STATIC);

        return audioTrack;
    }

    private double sine(int x, double frequencies){
        return Math.sin(  2*Math.PI * x * frequencies);
    }

    void playTrack(byte[] generatedSnd){
        getAudioTrack(generatedSnd.length)
                .write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();

    }
}