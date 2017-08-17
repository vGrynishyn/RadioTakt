package com.vgrynishyn.radiotakt;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Main extends AppCompatActivity{

    final String DATA_HTTP="http://radiotakt.com.ua:8000/takt.mp3";
    final String DATA_OLAY_LIST="http://radiotakt.com.ua/player";
    final String lastSongId = "id=\"last_song";
    ImageButton playBtn;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    TextView tvCurrentSong;
    TextView tvLastSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        playBtn = (ImageButton) findViewById(R.id.playBtn);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
    }

    public void clickPlayButton(View view) {
        new RetrieveListOfStrings().execute();
    }

  private class RetrieveListOfStrings extends AsyncTask<String, Void, List<String>> implements RetrieveListOfStringsInterface {
      @Override
      protected List<String> doInBackground(String ... params) {
          List<String> pageStrings = new ArrayList<>();
         try {
              URL url = new URL(DATA_OLAY_LIST);

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
          tvLastSongs.setText("- "+lastSongs);
      }
      private String getListOfLastSongsFromHTMLString(String str, String firstCut, String LastCut ,String delimiter, int cut){
          return str.substring(str.indexOf(firstCut)+cut, str.lastIndexOf(LastCut)).replace(delimiter, "\n- ");
      }
  }

}
