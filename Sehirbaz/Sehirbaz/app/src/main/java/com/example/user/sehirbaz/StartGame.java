package com.example.user.sehirbaz;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class StartGame extends AppCompatActivity {

    ConstraintLayout constraintLayout; // for animations

    MediaPlayer mediaPlayer; // for introduction sound effect

    ImageView theStartButton;

    MediaPlayer touchSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        SetFullScreenMode();

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutIncludesAll);
        // find the constraint layout to change its background for animation purposes

        if(Build.VERSION.SDK_INT > 21) {
            // HIGH APIs
            IntroductionWithAnimation();

        }
        else{
            // LOW APIs
            // DON'T DO any animation
            IntroductionWithoutAnimation();
        }


        theStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Make the touch sound effect
                touchSound = MediaPlayer.create(StartGame.this,R.raw.touch_sound);
                touchSound.start(); // make the touch sound

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Intent myIntent = new Intent(StartGame.this, Game.class);

                        finish(); // Finish this activity

                        startActivity(myIntent); // go play the game
                    }
                }, 400);


            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
        touchSound.release();

    }


    public void IntroductionWithAnimation(){

        if(Build.VERSION.SDK_INT > 21) {
            // HIGH APIS
            constraintLayout.setBackground(getDrawable(R.drawable.background_without));
            // initialize constraint layout without 81 logo

            theStartButton = (ImageView) findViewById(R.id.startButton);
            theStartButton.setVisibility(View.INVISIBLE);

            // make the introduction sound effect

            mediaPlayer = MediaPlayer.create(StartGame.this,R.raw.awesome_introduction);
            mediaPlayer.start();

              /* REVEAL THE PLAY BUTTON AND 81 LOGO */
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if(Build.VERSION.SDK_INT > 21) {
                        constraintLayout.setBackground(getDrawable(R.drawable.background_with));}
                        constraintLayout.setAlpha(0.5f);
                        constraintLayout.animate().alphaBy(1).setDuration(1700);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            theStartButton.setVisibility(View.VISIBLE);
                        }
                    }, 2600);
                }
            }, 690);

        }

    }
    public void IntroductionWithoutAnimation(){
        /*
            Introduction to the game without background animations
         */

        theStartButton = (ImageView) findViewById(R.id.startButton);
        theStartButton.setVisibility(View.INVISIBLE);

        // make the introduction sound effect

        mediaPlayer = MediaPlayer.create(StartGame.this,R.raw.awesome_introduction);
        mediaPlayer.start();

        /* REVEAL JUST THE PLAY BUTTON  */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                theStartButton.setVisibility(View.VISIBLE);
            }
        }, 3000);


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
