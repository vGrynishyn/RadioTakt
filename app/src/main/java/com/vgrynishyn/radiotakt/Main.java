package com.vgrynishyn.radiotakt;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener{

    final String DATA_HTTP="http://radiotakt.com.ua:8000/takt.mp3";
    final String DATA_PLAY_LIST="http://radiotakt.com.ua/player";
    final String lastSongId = "id=\"last_song";

    ImageButton btnPlay;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    TextView tvCurrentSong;
    TextView tvLastSongs;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);

    }

    public void clickPlayButton(View view) {
        if (progressBar.getVisibility() == progressBar.INVISIBLE && !mediaPlayer.isPlaying())
        {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
       if(!mediaPlayer.isPlaying()){
            btnPlay.setImageResource(R.drawable.player_stop);}
        else{
            btnPlay.setImageResource(R.drawable.player_play);
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mTimer = new Timer();
                mMyTimerTask = new MyTimerTask();
                if(!mediaPlayer.isPlaying()){
                    mTimer.schedule(mMyTimerTask, 30000, 5000);
                    try {
                        mediaPlayer.setDataSource(DATA_HTTP);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=new MediaPlayer();
                    mTimer.cancel();
                }
            }
        });
        t.start();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
           new RetrieveListOfStrings().execute();
        }
    }

    private class RetrieveListOfStrings extends AsyncTask<String, Void, List<String>> implements RetrieveListOfStringsInterface {
      @Override
      protected List<String> doInBackground(String ... params) {
          List<String> pageStrings = new ArrayList<>();
         try {
              URL url = new URL(DATA_PLAY_LIST);

              HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
              InputStream in = new BufferedInputStream(urlConnection.getInputStream());
              BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
             while(br.read()!=-1)
             {
                 String line = br.readLine();
                 if (line.contains(lastSongId))
                     pageStrings.add(line);
             }
          } catch (Exception e) {
              //do something
          }
          return pageStrings;
      }

      @Override
      public void onPostExecute(List<String> ls) {
          tvCurrentSong = (TextView) findViewById(R.id.CurrentSong);
          tvLastSongs = (TextView) findViewById(R.id.LastSongs);
          String currentSong = getListOfLastSongsFromHTMLString(ls.get(0), ">", "<" ,"</span><span>", 1);
          String lastSongs = getListOfLastSongsFromHTMLString(ls.get(1), "<span>", "</span>" ,"</span><span>", 6);
          tvCurrentSong.setText(currentSong);
          //tvLastSongs.setInputType(InputType.TYPE_CLASS_TEXT);
          tvLastSongs.setText("- "+lastSongs);
          if (progressBar.getVisibility() == progressBar.VISIBLE  && mediaPlayer.isPlaying())
          {
             progressBar.setVisibility(ProgressBar.INVISIBLE);
          }
      }
      private String getListOfLastSongsFromHTMLString(String str, String firstCut, String LastCut ,String delimiter, int cut){
          return str.substring(str.indexOf(firstCut)+cut, str.lastIndexOf(LastCut)).replace(delimiter, "\n- ");
      }
  }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

}
