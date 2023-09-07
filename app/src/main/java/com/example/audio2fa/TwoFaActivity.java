package com.example.audio2fa;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class TwoFaActivity extends AppCompatActivity {

    private double mInterval = 0.05;
    private int mSampleRate = 44100;

    private byte[] generatedSnd;

    private final double mStandardFreq = 2000;

    private final int zeroFreq = 7000;
    private final int oneFreq = 12000;

    Handler handler = new Handler();
    private AudioTrack audioTrack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_fa);

        Random random = new Random();

        FloatingActionButton refreshFab = (FloatingActionButton) findViewById(R.id.refreshFab);
        FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.soundFab);
        TextView textView = (TextView) findViewById(R.id.numText);
        TextView binaryTextView = (TextView) findViewById(R.id.numTextBinary);

        refreshFab.setOnClickListener(view -> {
            int r = random.nextInt((999999 - 100000) + 1) + 100000;
            textView.setText(String.valueOf(r));

            char[] binaryChar = Integer.toBinaryString(r).toCharArray();

            if(binaryChar.length%4!=0){
                int k = ((binaryChar.length/4)+1)*4-binaryChar.length;
                char[] tChar = new char[binaryChar.length+k];
                for(int i = 0 ; i < tChar.length ; i++){
                    if(i<k){
                        tChar[i]='0';
                    }else{
                        tChar[i]=binaryChar[i-k];
                    }
                }
                binaryChar = tChar;
            }

            binaryTextView.setText(String.valueOf(binaryChar));
        });

        playFab.setOnClickListener(view -> {
            handler.post(() -> playSound(binaryTextView.getText().toString().toCharArray()));
        });

        refreshFab.callOnClick();
    }

    void playSound(char[] binaryCharArr){

        byte[] tempByte = new byte[0];
        byte[] gapByte = getTone(mInterval, mSampleRate, 0);
        for (int i = 0; i < binaryCharArr.length ; i++ ){
            double note = getNoteFrequencies(i);
            byte[] tonByteNote = getTone(mInterval, mSampleRate, binaryCharArr[i]=='1'?oneFreq:zeroFreq);
            tempByte = concat(tonByteNote, tempByte);
            tempByte = concat(gapByte, tempByte);
        }
        generatedSnd = tempByte;

        Log.d(TAG, "playSound: duration: "+generatedSnd.length*mInterval);

        playTrack(generatedSnd);
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
        byte[] generatedTone = new byte[2 * maxLength];

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