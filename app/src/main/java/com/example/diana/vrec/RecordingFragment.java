package com.example.diana.vrec;

import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.waves.util.Horizon;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Created by Diana on 28.12.2016.
 */



public class RecordingFragment extends Fragment {
    private static final String LOG_TAG = "RecordingFragment";
    private static String mFileName = null;
    private static String timeStamp = null;
    private static String ext = null;
    private static LayoutInflater infl = null;
    private static ViewGroup cont = null;
    List<String> recList;
    String[] timeList;
    private Handler mHandler = new Handler();
    private static int timer;

    private ImageButton mRecordButton = null;
    private ImageButton mStopButton = null;
    private ImageButton mPauseButton = null;
    private SeekBar mSeekBar = null;
    private static TextView timp = null;
    private MediaRecorder mRecorder = null;
    private boolean mStartRecording = true;
    private boolean mStartPause = true;
    private static int channel;
    private static int output;
    private static boolean save;
    private static int flag;
    private static float numarTimp;
    private static String textTimp;
    private Horizon mHorizon;
    private GLSurfaceView glSurfaceView;

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final int RECORDER_CHANNELS = 1;
    private static final int RECORDER_ENCODING_BIT = 16;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MAX_DECIBELS = 120;
    private byte[] buffer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recording_fragment, container, false);

        recList = new ArrayList<String>();

        this.infl = inflater;
        this.cont = container;

        mStopButton = (ImageButton) v.findViewById(R.id.imageButton3);
        mPauseButton = (ImageButton) v.findViewById(R.id.imageButton2);

        mStopButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.GONE);


        mRecordButton = (ImageButton) v.findViewById(R.id.imageButton);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mSeekBar.setEnabled(true);
                timer = 0;
                seekBarProgress();
                if (mStartRecording == true) {
                    flag = 0;
                    mStopButton.setVisibility(View.VISIBLE);
                    mPauseButton.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.red);
                    mPauseButton.setImageResource(R.drawable.rrpausehot);
                    mRecordButton.setEnabled(false);
                    MainActivity.setIconVisibility(1);
                }
                mStartRecording = !mStartRecording; // se face false, adica e apasat
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacksAndMessages(null);
                mSeekBar.setEnabled(false);

                if (mStartRecording == false) { //daca recordingul e pornit si butonul de start nu a mai fost apasat
                    onRecord(mStartRecording); //oprim inregistrarea
                    mStartRecording = !mStartRecording; // setam pe true, activam flag-ul de inregistrare
                }

                if(recList.size() == 0 || (recList.size() > 0 && !("REC_"+ timeStamp + ext).equals(recList.get(recList.size()-1))) ){
                    recList.add("REC_"+ timeStamp + ext);
                }
                //concatenare
                if( recList.size() >=2) {
                    try {
                        MergedAudio.mergeAudio(recList, ext);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                recList = new ArrayList<String>();

                mRecordButton.setImageResource(R.drawable.green);  //butonul este acum apasat
                mRecordButton.setEnabled(true);
                mStopButton.setVisibility(View.GONE);
                mPauseButton.setVisibility(View.GONE);
                MainActivity.setIconVisibility(0);
                mStartPause = true;

                if (save == true){
                    timp.setText("00:00");
                    mSeekBar.setProgress(0);
                    Toast toast = Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT );
                    toast.show();
                    ListFragment.setRecList();
                    ListFragment.notifyAd();
                }else if (save == false){
                    ShowMessageBox();
                }
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartPause) {
                    mSeekBar.setEnabled(false);
                    mHandler.removeCallbacksAndMessages(null);
                    mPauseButton.setImageResource(R.drawable.playhot);
                    mRecordButton.setImageResource(R.drawable.gri);
                    onRecord(false);
                    recList.add("REC_"+ timeStamp + ext);  //mStartPause = false apasat
                    mStartRecording = true;                   // mStartRecording = true;
                }else{
                    onRecord(true);
                    mSeekBar.setEnabled(true);
                    seekBarProgress();
                    mPauseButton.setImageResource(R.drawable.rrpausehot); //mStartPause = true neapasat
                    mRecordButton.setImageResource(R.drawable.red);       // mStartRecording = false;
                    mStartRecording = false;
                }
                mStartPause = !mStartPause;
            }
        });

        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        timp = (TextView) v.findViewById(R.id.textView2);
        timp.setText("00:00");

        mSeekBar.setEnabled(false);
        mSeekBar.setMax(6660);
        mSeekBar.setProgress(0);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        glSurfaceView = (GLSurfaceView) v.findViewById(R.id.gl_surface);

        glSurfaceView.setZOrderOnTop(true);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);


        mHorizon = new Horizon(glSurfaceView, getResources().getColor(android.R.color.transparent),
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_ENCODING_BIT);
        mHorizon.setMaxVolumeDb(MAX_DECIBELS);


        return v;
    }



    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        if (channel == 2){
            mRecorder.setAudioChannels(2);
        }else {
            mRecorder.setAudioChannels(1);
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return;
            }
        }
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mFileName = mediaStorageDir.getPath() + File.separator + "REC_"+ timeStamp;


        if (output == 3){
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        }else if (output == 2){
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        }else{
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        }

        if (output == 3){
            ext = ".mp4";
            mFileName += ext;
        }else if (output == 2 || output == 1){
            ext = ".3gp";
            mFileName += ext;
        }
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    // seekbar run
    public void seekBarProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(timer);
                mHandler.postDelayed(this, 100);
                timer ++;
                timp.setText(String.format("%02d",timer/600)+":"+String.format("%02d",(timer/10)%60));

                int r = (int) ( mRecorder.getMaxAmplitude()/ 135.0D);

                buffer= ByteBuffer.allocate(50).putInt(r-40).array();
                mHorizon.updateView(buffer);
            }
        });
    }

    //alert dialog

    public void ShowMessageBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setTitle("Question");
        builder.setMessage("Do you want to save your recording ?");

        View v = infl.inflate(R.layout.alert_layout, cont, false);
        final EditText text = (EditText) v.findViewById(R.id.et);
        text.setText("REC_"+ timeStamp);

        builder.setView(v);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                timp.setText("00:00");
                mSeekBar.setProgress(0);
                String name = text.getText().toString();

                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
                File from = new File(file,  "REC_"+ timeStamp + ext);
                File to = new File(file, name+ext);
                from.renameTo(to);

                Toast toast = Toast.makeText(getContext()," Your record was saved!", Toast.LENGTH_SHORT );
                toast.show();
                ListFragment.setRecList();
                ListFragment.notifyAd();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                timp.setText("00:00");
                mSeekBar.setProgress(0);
                String name = text.getText().toString();

                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
                File mediaFile;
                mediaFile = new File(file.getPath() + File.separator + name + ext);
                if (mediaFile.exists()){
                    mediaFile.delete();
                }

                ListFragment.setRecList();
                ListFragment.notifyAd();
            }


        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();

    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);

        if(mStartRecording == false) {
            onRecord(mStartRecording);
        }

        if (mStartRecording == false || mStartPause == false) {   // salvare neefectuata
            mRecordButton.setImageResource(R.drawable.green);
            mRecordButton.setEnabled(true);
            mStopButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.GONE);
            mStartRecording = true; // inseamna ca e pe inregistrare
            mStartPause = true;
            mStartPause = true;
            MainActivity.setIconVisibility(0);

            if(recList.size() == 0 || (recList.size() > 0 && !("REC_"+ timeStamp + ext).equals(recList.get(recList.size()-1))) ){
                recList.add("REC_"+ timeStamp + ext);
            }
            //concatenare

            if( recList.size() >=2) {
                try {
                    MergedAudio.mergeAudio(recList, ext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (save == true) {
                Toast toast = Toast.makeText(getContext(), mFileName.split("/")[mFileName.split("/").length-1] + " was saved!", Toast.LENGTH_SHORT);
                toast.show();
                timp.setText("00:00");
                mSeekBar.setProgress(0);

            } else if (save == false) {
                ShowMessageBox();
            }

        }



        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void onResume() {
        super.onResume();
        recList = new ArrayList<String>();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    public static RecordingFragment newInstance(int c, int o, boolean s) {

        RecordingFragment f = new RecordingFragment();

        channel = c;
        output = o;
        save = s;

        return f;
    }

    public static void setChannelOutput(int c, int o, boolean s) {
        channel = c;
        output = o;
        save = s;
    }

}

