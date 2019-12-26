package com.example.user.sehirbaz;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Introduction extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    // for future technologies sound

    TextView tvFuture;
    // text view for the FUTURE text
    TextView tvTechnologies;
    // text view for the TECHNOLOGIES text


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        SetFullScreenMode();

        mediaPlayer = MediaPlayer.create(this, R.raw.future_technologies_introduction);
        mediaPlayer.start();

        tvFuture = (TextView) findViewById(R.id.tvFuture);
        tvTechnologies = (TextView) findViewById(R.id.tvTechnologies);

        tvFuture.animate().alphaBy(1).setDuration(2450);
        // first display the FUTURE text

        new Handler().postDelayed(new Runnable() {
            public void run() {
                // after 2.5 seconds
                // displaye TECHNOLOGIES text for 2 seconds
                tvTechnologies.animate().alphaBy(1).setDuration(2000);
            }
        }, 2500);


        new Handler().postDelayed(new Runnable() {
            public void run() {
                // after 5 seconds
                // go to the starting game activity
                Intent intent = new Intent(Introduction.this, StartGame.class);
                startActivity(intent);
            }
        }, 5000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
        // release the media player
    }


    private void SetFullScreenMode(){

        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        }
        else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }

}
