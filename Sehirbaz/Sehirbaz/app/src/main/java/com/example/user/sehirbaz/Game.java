package com.example.user.sehirbaz;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class Game extends AppCompatActivity {

    FirebaseDatabase theDatase;
    // The Firebase Database
    DatabaseReference refSorular;
    // The Root Reference to the sorular in the database

    ImageView CityAsked;

    ArrayList<Soru> Sorular;
    // All the question are stored here, in this arraylist

    int NumberOfQuestions;
    // How many questions are there?

    Button[] buttons;
    // Array of buttons, to change their colors
    // to reach them everywhere

    TextView textViewSkor;


    int RightAnswer;
    // this show that which button is assigned the right answer
    // TR: hangi buton dogru yaniti tasiyor

    ArrayList<Integer> VirginQuestions;
    // This shows that which questions have not been
    // asked to the user until now - virgin questions
    // it keeps indexes of these kind of questions

    int WhichQuestionIsProgramAt;
    // this will show which question the program is at
    // righ now

    int Score;
    // Score of the user

    int NumberHearts;
    // number of the hearts
    // i.e. the user is allowed be wrong as long as he/she has a heart

    ImageView[] Hearts;


    ArrayList<String> sehirlerOptions;
    // Sehirler arraylist to make sure that the options do not repeat
    // these are the sehirler at the options


    boolean Clickable;
    // if the user has answered a question
    // but are currently waiting for a response
    // LOCK the options

    MediaPlayer mediaPlayer;
    // sound operations -> Game Over, Right Answer, Wrong Answer, Touch Sound Effect

    MediaPlayer mediaPlayerBackgroundMusic;
    // background music

    boolean isConnected;
    // a FLAG that reveals whether the connection between the database and
    // the app has begun or not

    private int THRESHOLD = 320;
    // This is the winning threshold
    // if this score is achived, we'll congratulate the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        SetFullScreenMode();

        //SetStatusBarColor();

        InitializeEverytingInTheGame();



    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayerBackgroundMusic.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayerBackgroundMusic.start();
    }

    private void InitializeEverytingInTheGame(){

        // we have not connected to the database
        isConnected = false;

        /* CONNECTION TO THE DATABASE */

        theDatase = FirebaseDatabase.getInstance();
        // Connect to the Firebase Database
        // Initialize the database

        refSorular = theDatase.getReference().child("sorular");
        // Get the reference of the "sorular"

        /* INITIALIZE THE BUTTONS ARRAY */

        buttons = new Button[4];
        buttons[0] = (Button) findViewById(R.id.butonYanit0);
        buttons[1] = (Button) findViewById(R.id.butonYanit1);
        buttons[2] = (Button) findViewById(R.id.butonYanit2);
        buttons[3] = (Button) findViewById(R.id.butonYanit3);

        /*  CREATE SORULAR */

        Sorular = new ArrayList<Soru>();


        /* INITIALIZE THE SORULAR BEFORE CONNECTION */
        /*
            Getting all the sorular from the firebase real-time database
            might take some time,
            we have to put at least 6 questions in order to avoid this
         */

        InitializeSorularBeforeConnection();


        /* REACH THE CITY ASKED TO THE USER IMAGE */

        CityAsked = (ImageView) findViewById(R.id.imageViewCityAsked);
        // this is the image asked to the user as a question


        RightAnswer = -1 ;

        /* FIND THE SCORE TEXT ON THE SCREEN */
        textViewSkor = (TextView) findViewById(R.id.textViewSkor);


        Score = 0;
        // score is 0 initially

        /* UPDATE THE SCORE TABLE ON THE SCREEN */

        UpdateTheScore(); // initially update the score

        /* INITIALIZE BACKGROUND MUSIC */

        mediaPlayerBackgroundMusic = MediaPlayer.create(Game.this,R.raw.background_music);
        mediaPlayerBackgroundMusic.start();

        Clickable  = true;
        // UNLOCK all the buttons

        NumberHearts = 3;
        // the program gives the user 3 hearts initially

        /* INITIALIZE THE HEARTS ARRAY */
        // we will deal with hearts all the time
        Hearts = new ImageView[3];
        Hearts[0] = (ImageView) findViewById(R.id.heartLeft);
        // LEFT -> INDEX 0
        Hearts[1] = (ImageView) findViewById(R.id.heartMiddle);
        // MIDDLE -> INDEX 1
        Hearts[2] = (ImageView) findViewById(R.id.heartRight);
        // RIGHT -> INDEX 2



        VirginQuestions = new ArrayList<Integer>();
        // initialize and declare the virgin questions arraylist

        MakeOfflineQuestionsVirgin();
        // We added the questions
        // but for the randomization algorithm to work
        // we have to declare them as virgin


        NewQuestion();
        // Randomly choose a new questions
        // update the system based on this

        UpdateTheScreen();

        /* SET THE VALUE EVENT LISTENER TO THE DATABSE */
        /* GET ALL THE SORULAR FROM THE DATABASE */
        refSorular.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // All the questions from the database is here

                if(isConnected) return; // DO NOT LET OVERRIDE EVERYTHING AGAIN AND AGAIN

                int TopIndexSorular = Sorular.size();

                for(DataSnapshot theSnapshot: dataSnapshot.getChildren()){

                    String tempResim = theSnapshot.child("resim").getValue(String.class);
                    // Get the "resim" information for one questions
                    String tempSehir = theSnapshot.child("sehir").getValue(String.class);
                    // Get the "sehir" information for the same question
                    Sorular.add(new Soru(tempResim,tempSehir));
                    // Add this to the Sorular ArrayList

                }

                NumberOfQuestions = Sorular.size();
                // get the current number of questions
                // Update the number of questions

                // Now you have add these questions to the
                // virgin questions , because these are new questions
                for(int i = TopIndexSorular; i < NumberOfQuestions; i++){
                    VirginQuestions.add(Integer.valueOf(i));
                }

                isConnected = true;
                // now we connection is satisfied

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });




    }

    private void UpdateTheScreen(){
        /*
            This method will update the screen
            based on the current question
            Buttons and the image
         */

        // Go get the information which question are we at

        /* UPDATE THE IMAGE BASED ON THIS */

        String ImageRightNow = Sorular.get(WhichQuestionIsProgramAt).getResim();

        Picasso.with(Game.this).load(ImageRightNow).into(CityAsked);

        if(Build.VERSION.SDK_INT < 21) {
            // LOW APIS and SMALL SCREENS
            // So make the images smaller for small screens and lower apis
            CityAsked.animate().scaleY(0.86f);
            CityAsked.animate().scaleX(0.86f);
        }

        /* UPDATE THE BUTTONS FOR THIS QUESTION */

        UpdateTheButtons();

        return;


    }

    private void UpdateTheButtons(){
        /*
            This method will update the buttons based on the question right now
            It gets the right answer and assigns it randomly to a button
            And fills the other buttons randomly as well
         */

        // Go learn which question the program is at

        int QuestionNow = WhichQuestionIsProgramAt;

        // Get the answer to this question

        String theAnswer = Sorular.get(QuestionNow).getSehir();
        // This is the answer

        // Assign the answer to a button RANDOMLY

        Random random = new Random();

        int randomlyAssigned = random.nextInt(4);
        // Get random number between 0 and 3  all-included

        // put the answer to this randomly-assigned button

        buttons[randomlyAssigned].setText(theAnswer);

        // and the program must remember this index
        // because it's where the right answer reside !

        RightAnswer = randomlyAssigned;

        sehirlerOptions = new ArrayList<String>();
        // Create the Arraylist each time

        sehirlerOptions.add(theAnswer);


        /* FILL OUT THE OTHER 3 BUTTONS RANDOMLY */

        // Key Point: The answer can't repeat twice

        for(int i = 0 ; i < 4; i++){
            // Make all th buttons blue REMEMBER
            buttons[i].setBackgroundColor(getResources().getColor(R.color.newGreatBlue));

            if( i == randomlyAssigned){
                continue;
            }
            else{
                int RandomQuestion = random.nextInt(Sorular.size());
                // chose one question randomly, and get the answer of it
                String RandomAnswer = Sorular.get(RandomQuestion).getSehir();

                while(DoesItRepeat(RandomAnswer)){
                    // AS LONG AS THIS RANDOM OPTIONS, REPEATS WITH OTHER OPTIONS
                    RandomQuestion = random.nextInt(Sorular.size());
                    RandomAnswer = Sorular.get(RandomQuestion).getSehir();

                    // find new options randomly
                }

                sehirlerOptions.add(RandomAnswer);

                // and when you guarantee that the answer does not repeat
                // assign this to the corresponding button

                buttons[i].setText(RandomAnswer);
            }
        }

        return;

    }


    private void NewQuestion(){
        /*
            This function will get a random new question
            and update the system based on that
            RANDOM NEW QUESTION
         */

        Random random = new Random();

        int randomlyChosenIndex = random.nextInt(VirginQuestions.size());
        // Choose a random index from the VIRGIN QUESTIONS
        // then get the question at that index

        int randomlyChosenQuestion = VirginQuestions.get(randomlyChosenIndex);


        // Update which question the program is at
        WhichQuestionIsProgramAt = randomlyChosenQuestion;

        // And remove this questions from the virgin questions array list
        // because we are right now asking this question to the user
        // it is not virgin question any more

        VirginQuestions.remove(randomlyChosenIndex);

        if(isConnected && VirginQuestions.size() < 5){
            // This means that we have asked the user a lot of questions
            // and right now, there is very few questions left
            // so we refresh the question
            // basically we ask the same question again, after we're out of questions
            int numberQuestionsLeftRightNow = VirginQuestions.size();
            for(int i = numberQuestionsLeftRightNow - 1; i >= 0; i--){
                VirginQuestions.remove(i);
            }
            for(int i = 0; i < Sorular.size(); i++){
                VirginQuestions.add(Integer.valueOf(i));
            }

            String qs = "";
            for(int i = 0; i < VirginQuestions.size(); i++){qs = qs + " " + String.valueOf(VirginQuestions.get(i));}
            Log.i("YETMEME","EKLENDI");
            Log.i("VIRGIN QUESTIONS", qs);
        }


        return;


    }


    public void Control(View view){
          /*
            This method makes all the control operartions in the game
            Whether the user has answered correctly, or wrong
            Has she/he won?
            Has he/she failed?
            Determines all
         */

        if(!Clickable){
            // this means user has answered a question
            // and waits for the response
            // you , wait for the response
            return;
        }

        // RIGHT AFTER THE USER HAS TOUCHED ONE OF THE OPTIONS
        // LOCK THE BUTTONS
        Clickable = false;

        // Cast this view to a button
        final Button touchedButton = (Button) view;

        // Get its tag to check whether the user is right or wrong
        final int givenAnswer = Integer.valueOf(touchedButton.getTag().toString());

        // PAUSE THE BACKGROUND MUSIC
        mediaPlayerBackgroundMusic.pause();

        // Make the pop sound
        mediaPlayer = MediaPlayer.create(Game.this, R.raw.touch_sound);
        mediaPlayer.start();

        // Make the touched button yellow color
        // to indicate waiting
        touchedButton.setBackgroundColor(getResources().getColor(R.color.waitingYellow));

        // The user should wait for 3 seconds
        new Handler().postDelayed(new Runnable() {
            public void run() {
                // After 3 seconds, the control operation starts
                mediaPlayer.release(); // release the touch sound

                if(givenAnswer == RightAnswer){
                    // IF THE USER IS RIGHT

                    // Make the victory sound
                    mediaPlayer = MediaPlayer.create(Game.this, R.raw.right_answer);
                    mediaPlayer.start();

                    touchedButton.setBackgroundColor(getResources().getColor(R.color.rightAnswerGreen));
                    // MAKE THE TOUCHED BUTTON GREEN

                    Score += 20;

                    UpdateTheScore();

                    // After 3 seconds again, FETCH THE NEW QUESTION
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                            /* WINNING CASE */
                            if(Score == THRESHOLD){
                                // if the user has achived the winning score,
                                Win();

                                return;
                            }

                            // Release the victory (right answer) sound
                            mediaPlayer.release();

                            Clickable = true;
                            NewQuestion();
                            UpdateTheScreen();

                            // resume the background music
                            mediaPlayerBackgroundMusic.start();
                        }
                    }, 2800);


                }
                else{
                    // IF THE USER IS WRONG

                    // Make the failure sound
                    mediaPlayer = MediaPlayer.create(Game.this, R.raw.wrong_answer);
                    mediaPlayer.start();

                    touchedButton.setBackgroundColor(getResources().getColor(R.color.wrongAnswerRed));
                    // MAKE THE TOUCHED BUTTON RED, BECAUSE IT'S WRONG
                    buttons[RightAnswer].setBackgroundColor(getResources().getColor(R.color.rightAnswerGreen));
                    // AND REVEAL THE RIGHT ANSWER TO THE USER
                    // BY MAKING THE BUTTON GREEN

                    NumberHearts--;
                    UpdateHeartScreen();

                    // After 3 seconds again, FETCH THE NEW QUESTION
                    new Handler().postDelayed(new Runnable() {
                        public void run() {

                            // Release the failure sound effect
                            mediaPlayer.release();

                            /* GAME OVER CASE */
                            if(NumberHearts == 0){

                                GameOver();
                                return; // do not forget to return, otherwise bugs will occur
                            }

                            Clickable = true;
                            NewQuestion();
                            UpdateTheScreen();

                            // resume the background music
                            mediaPlayerBackgroundMusic.start();
                        }
                    }, 3700);
                }
            }
        }, 2700);

    }

    private void Win(){
        /*
            This method is responsible for winning operations
         */

        /* STOP THE BACKGROUND MUSIC*/
        mediaPlayerBackgroundMusic.pause();


        // Release the media player here
        // because in onDestroy we do not release it
        mediaPlayer.release();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent myIntent = new Intent(Game.this, Win.class);
                finish();
                startActivity(myIntent);
            }
        }, 1200);

    }

    private void GameOver(){

        mediaPlayer = MediaPlayer.create(Game.this,R.raw.game_over);
        mediaPlayer.start();

        /* STOP THE BACKGROUND MUSIC*/
        mediaPlayerBackgroundMusic.pause();


        Intent myIntent = new Intent(Game.this, Result.class);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                mediaPlayer.release();
            }
        }, 2500);


        finish(); // destroy this activity
        startActivity(myIntent);


    }

    public void FinishTheGame(View view){

        GameOver();

    }


    private void MakeOfflineQuestionsVirgin(){
        /*
            This method will adds the offline questions
            to the virgin questions
         */

        int howManyQuestions = Sorular.size();

        for(int i = 0; i < howManyQuestions ; i++){
            VirginQuestions.add(Integer.valueOf(i));
        }

        return;
    }


    private void InitializeSorularBeforeConnection(){
        /*
            This method puts at least 6 questions to the sorular arraylist
            in case we cannot get the sorular from the database
            immediately
         */

        Soru iterator = null;
        String tempResim;
        String tempSehir;


        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Fankara-1.jpg?alt=media&token=1f65b499-3ed8-4b42-be9f-970cf4aaf99e";
        tempSehir = "Ankara";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);

        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Fkudus-1.jpg?alt=media&token=4fbc384b-e51d-4dc0-ba04-5e3bad95deb6";
        tempSehir = "Kudüs";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);

        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Fmoskova-1.jpg?alt=media&token=c5dc714c-1e37-4f2c-9b68-d56fa105158b";
        tempSehir = "Moskova";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);


        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Frio-1.jpg?alt=media&token=2c371008-b23a-47a6-aae4-2cef9ebdfc1d";
        tempSehir = "Rio de Janeiro";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);

        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Ftokyo-1.jpg?alt=media&token=9bcd65a8-6c4c-43b6-a741-4c883804b4b5";
        tempSehir = "Tokyo";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);

        tempResim = "https://firebasestorage.googleapis.com/v0/b/sehirbaz-bf951.appspot.com/o/sehirler%2Fvenice-1.jpg?alt=media&token=467235f2-ae72-4171-8714-cb7521647ebc";
        tempSehir = "Venedik";
        iterator = new Soru(tempResim, tempSehir);
        Sorular.add(iterator);

        /*
            Initialized the sorular with 6 questions
         */

        NumberOfQuestions = 6;


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

    private void SetStatusBarColor(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.newTrueWhite));
        }
    }


    private boolean DoesItRepeat(String Sehir){
        /*
            This method will determine whether this Sehir has been assigned
            to an option or not
            it is assigned before, the same city, it return true
            else if it is not assigned before
            it returns false
         */


        for(int i = 0 ; i < sehirlerOptions.size(); i++){
            if(sehirlerOptions.get(i).equals(Sehir)){
                return true;
            }
            else{
                continue;
            }
        }

        return false;
    }

    private void UpdateHeartScreen(){
        /*
            This method will update the hearts screen
            based on the current number of hearts
         */

        // here we know the pointers to the hearts

        // INDEX 0 -> LEFT
        // INDEX 1 -> MIDDLE
        // INDEX 2 -> RIGHT

        if(NumberHearts == 3){
            // if there are THREE heats,
            // make ALL VISIBLE
            Hearts[0].setVisibility(View.VISIBLE);
            Hearts[1].setVisibility(View.VISIBLE);
            Hearts[2].setVisibility(View.VISIBLE);
        }
        else if(NumberHearts == 2){
            // if there are TWO HEARTS, means one is gone
            // make the right and middle visible
            // make the left invisible
            Hearts[0].setVisibility(View.INVISIBLE);
            Hearts[1].setVisibility(View.VISIBLE);
            Hearts[2].setVisibility(View.VISIBLE);
        }
        else if(NumberHearts == 1){
            // if there is ONLY ONE HEART, TWO LOST
            // make the right visible
            // make the left and middle invisible
            Hearts[0].setVisibility(View.INVISIBLE);
            Hearts[1].setVisibility(View.INVISIBLE);
            Hearts[2].setVisibility(View.VISIBLE);
        }
        else if(NumberHearts == 0){
            // if there is NO HEART LEFT = i.e. GAME OVER
            // make ALL INVISIBLE
            Hearts[0].setVisibility(View.INVISIBLE);
            Hearts[1].setVisibility(View.INVISIBLE);
            Hearts[2].setVisibility(View.INVISIBLE);
        }

        return;
    }

    private void UpdateTheScore(){
        /*
            This method updates the score screen
            based on the current score
            it is invoked after every right answer
         */

        textViewSkor.setText(String.valueOf(Score));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayerBackgroundMusic.release();
    }
}
