package com.example.mplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
Button btnPlay,btnNext, btnPrevious, btnFastForward, btnFastBackWard;
TextView txtSongName, txtSongStart, txtSongEnd;
SeekBar seekkMusicBar;
BarVisualizer barVisualizer;
ImageView imageView;
String songName;
public static  final String EXTRA_NAME = "song_name";
static MediaPlayer mediaPlayer;
int position;
ArrayList<File> mySongs;
Thread updateSeekbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnFastBackWard = findViewById(R.id.btnfastBackward);
        btnFastForward = findViewById(R.id.btnfastForward);

        txtSongName = findViewById(R.id.txtSong);
        txtSongEnd = findViewById(R.id.txtSongEnd);
        txtSongStart = findViewById(R.id.txtSongStart);
        barVisualizer = findViewById(R.id.blaster);
        seekkMusicBar = findViewById(R.id.seekBar);

        imageView = findViewById(R.id.imgView);
        if (mediaPlayer!=null){
            mediaPlayer.start();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

       mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
       String sName = intent.getStringExtra("songname");
       position = bundle.getInt("pos",0);
       txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        updateSeekbar = new Thread(){
            @Override
            public  void run(){
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                super.run();
                while (currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekkMusicBar.setProgress(currentPosition);

                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }

                }
            }
        };
        seekkMusicBar.setMax(mediaPlayer.getCurrentPosition());
        updateSeekbar.start();
        seekkMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.MULTIPLY);
        seekkMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_700),PorterDuff.Mode.SRC_IN);
        seekkMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this,delay);

            }
        },delay);

        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()){
                btnPlay.setBackgroundResource(R.drawable.baseline_play_arrow_24);
                mediaPlayer.pause();
            }
            else {
                btnPlay.setBackgroundResource(R.drawable.baseline_pause);
                mediaPlayer.start();
                TranslateAnimation animation = new TranslateAnimation(-25,25,-25,25);
                animation.setInterpolator(new AccelerateInterpolator());
                animation.setDuration(600);
                animation.setFillEnabled(true);
                animation.setFillAfter(true);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(1);
                imageView.startAnimation(animation);
            }
        });

        btnNext.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position+1)%mySongs.size());
            Uri uri1 = Uri.parse(mySongs.get(position).toString());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
            songName = mySongs.get(position).getName();
            txtSongName.setText(songName);
            mediaPlayer.start();
            btnPlay.setBackgroundResource(R.drawable.baseline_pause);
         startAnimation(imageView,360f);
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId!=-1){
                barVisualizer.setAudioSessionId(audioSessionId);
            }
        });
        btnPrevious.setOnClickListener(v -> {
            mediaPlayer.start();
            mediaPlayer.release();
            position = ((position-1)<0)?(mySongs.size()-1):position-1;
            Uri uri12 = Uri.parse(mySongs.get(position).toString());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri12);
            songName = mySongs.get(position).getName();
            txtSongName.setText(songName);
            mediaPlayer.start();

            startAnimation(imageView,-360f);
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId!=-1){
                barVisualizer.setAudioSessionId(audioSessionId);
            }
        });
        mediaPlayer.setOnCompletionListener(mp -> btnNext.performClick());
        btnFastForward.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
            }

        });
        btnFastForward.setOnClickListener(v -> {

            if (mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
            }
        });
    }
    public void startAnimation(View view, Float degree){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }
    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time = time+min+":";
        if (sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }

}