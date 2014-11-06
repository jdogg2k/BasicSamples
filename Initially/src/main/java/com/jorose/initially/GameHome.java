package com.jorose.initially;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.Players;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;


public class GameHome extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
        View.OnClickListener, FinalScoreDialogFragment.FinalScoreDialogListener{

public static Properties properties = new Properties();
    PersonSearch pSearch;
    boolean personTrue = false;
    char initial1;
    char initial2;
    private ListView list;
    private ArrayList<Guess> arrayOfGuesses;
    private GuessesAdapter adapter;
    private MediaPlayer clockSound;
    private MediaPlayer correctSound;
    private MediaPlayer wrongSound;
    private MediaPlayer endSound;
    private MediaPlayer highSound;
    private ArrayList<String> correctGuesses;
    private String opponentGuesses;
    private int turnTime;
    private CountDownTimer timeTrack;
    private boolean lastTurn;

    public static final String TAG = "InitialActivity";

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // Current turn-based match
    private TurnBasedMatch mTurnBasedMatch;

    // Local convenience pointers
    public TextView mDataView;
    public TextView mTurnTextView;
    public TextView mInitialsView;
    public String myID;
    public String opponentID;
    public String matchResult;
    public int pFinalScore = 0;
    public int oFinalScore = 0;
    private boolean timerIsRunning = false;

    private AlertDialog mAlertDialog;

    // For our intents
    private static final int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;
    final static int RC_VIEW_ACHIEVEMENTS = 10010;
    private static final int RC_UNUSED = 5001;

    // How long to show toasts.
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;

    // Should I be showing the turn API?
    public boolean isDoingTurn = false;

    // This is the current match we're in; null if not loaded
    public TurnBasedMatch mMatch;

    // This is the current match data after being unpersisted.
    // Do not retain references to match data once you have
    // taken an action on the match, such as takeTurn()
    public GameTurn mTurnData;

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        endMatch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_home);

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // Setup signin and signout buttons
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        lastTurn = false;

       mInitialsView = ((TextView) findViewById(R.id.textInitials));
        //mTurnTextView = ((TextView) findViewById(R.id.turn_counter_view));

        arrayOfGuesses = new ArrayList<Guess>();
        // Create the adapter to convert the array to views
        adapter = new GuessesAdapter(this, arrayOfGuesses);

        list=(ListView)this.findViewById(R.id.mainListView);
        list.setAdapter(adapter);

        Button search = (Button) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText uText = (EditText) findViewById(R.id.searchName);
                pSearch = new PersonSearch();
                pSearch.execute(uText.getText().toString());
            }
        });

        Button giveup = (Button) findViewById(R.id.giveupButton);
        giveup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (timerIsRunning) {
                    turnOver();
                }
            }
        });

        clockSound = MediaPlayer.create(this, R.raw.ticktock);
        clockSound.setLooping(true);

        correctSound = MediaPlayer.create(this, R.raw.right);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);
        endSound = MediaPlayer.create(this, R.raw.end);
        highSound = MediaPlayer.create(this, R.raw.triumph);

        turnTime = getApplicationContext().getResources().getInteger(R.integer.turn_time);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): Connecting to Google APIs");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): Disconnecting from Google APIs");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected(): Connection successful");

        // Retrieve the TurnBasedMatch from the connectionHint
        if (connectionHint != null) {
            mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (mTurnBasedMatch != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(mTurnBasedMatch);
                return;
            }
        }

        setViewVisibility();

        // As a demonstration, we are registering this activity as a handler for
        // invitation and match events.

        // This is *NOT* required; if you do not register a handler for
        // invitation events, you will get standard notifications instead.
        // Standard notifications may be preferable behavior in many cases.
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        // Likewise, we are registering the optional MatchUpdateListener, which
        // will replace notifications you would get otherwise. You do *NOT* have
        // to register a MatchUpdateListener.
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
        mGoogleApiClient.connect();
        setViewVisibility();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;

            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult, RC_SIGN_IN,
                    getString(R.string.signin_other_error));
        }

        setViewVisibility();
    }

    public void setInitials(String initialData){
        if (initialData.equals("new")){
            initialData = getRandomLetters();
        } else {
            //set Initials from saved data
            String initialRaw = initialData.replace(".","").replace(" ","");
            initial1 = initialRaw.charAt(0);
            initial2 = initialRaw.charAt(1);
        }
        adapter.clear();
        EditText uInput = (EditText) findViewById(R.id.searchName);
        uInput.setText("");
        TextView uText = (TextView) findViewById(R.id.textInitials);
        uText.setText(initialData);
    }

    // Displays your inbox. You will get back onActivityResult where
    // you will need to figure out what you clicked on.
    public void onCheckGamesClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }

    public void onViewLeadersClicked(View view) {
        Intent intent = Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getResources().getString(R.string.leaderboard_number_of_wins));
        startActivityForResult(intent, RC_UNUSED);
    }

    public void onViewAchieveClicked(View view) {
        Intent intent = Games.Achievements.getAchievementsIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_VIEW_ACHIEVEMENTS);
    }


    // Open the create-game UI. You will get back an onActivityResult
    // and figure out what to do.
    public void onStartMatchClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
                1, 7, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    // Create a one-on-one automatch game.
    public void onQuickMatchClicked(View view) {

        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                1, 1, 0);

        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                .setAutoMatchCriteria(autoMatchCriteria).build();

        showSpinner();

        // Start the match
        ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                processResult(result);
            }
        };
        Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
    }

    // In-game controls

    // Cancel the game. Should possibly wait until the game is canceled before
    // giving up on the view.
    public void onCancelClicked(View view) {
        showSpinner();
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        setViewVisibility();
    }

    // Leave the game during your turn. Note that there is a separate
    // Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
    public void onLeaveClicked(View view) {
        showSpinner();
        String nextParticipantId = getNextParticipantId();

        Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                        processResult(result);
                    }
                });
        setViewVisibility();
    }

    // Finish the game. Sometimes, this is your only choice.
    public void onFinishClicked(View view) {
        isDoingTurn = true;
        ArrayList<String> oppArray = new ArrayList<String>(Arrays.asList(opponentGuesses.split(",")));
        showScore(Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName(), getOpponentName(), correctGuesses, oppArray);
    }

    public void showScore(String pName, String oName, ArrayList<String> pGuesses, ArrayList<String> oGuesses){

        pFinalScore = 0;
        oFinalScore = 0;
        matchResult = "";

        DialogFragment newFragment = new FinalScoreDialogFragment();
        FragmentManager fm = getFragmentManager();

        Bundle args = new Bundle();
        args.putString("pName", pName);
        args.putString("oName", oName);
        args.putStringArrayList("pGuesses", pGuesses);
        args.putStringArrayList("oGuesses", oGuesses);


        if (pGuesses.size() > 0) {
            for (String s : pGuesses) {
                if (!oGuesses.contains(s)) {
                    pFinalScore++;
                }
            }
        }

        if (oGuesses.size() > 0) {
            for (String s : oGuesses) {
                if (!pGuesses.contains(s)) {
                    oFinalScore++;
                }
            }
        }

        if (pFinalScore > oFinalScore) {
            matchResult = "WIN";
        } else if (oFinalScore > pFinalScore) {
            matchResult = "LOSS";
        } else {
            matchResult = "TIE";
        }

        args.putInt("pFinal", pFinalScore);
        args.putInt("oFinal", oFinalScore);

        newFragment.setArguments(args);
        newFragment.show(fm, "finalscore");
    }

    public void endMatch(){

        if (isDoingTurn) {
            showSpinner();

            String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
            String myParticipantId = mMatch.getParticipantId(playerId);

            int pResult = 0;
            int oResult = 0;
            int pPosition = 0;
            int oPosition = 0;

            if (matchResult.equals("WIN")) {
                pResult = ParticipantResult.MATCH_RESULT_WIN;
                pPosition = 1;
                oResult = ParticipantResult.MATCH_RESULT_LOSS;
                oPosition = 2;
                mTurnData.winScore = pFinalScore;
                mTurnData.loseScore = oFinalScore;
                if (myParticipantId.equals("p_1")) {
                    mTurnData.winner = "p_1";
                } else {
                    mTurnData.winner = "p_2";
                }

                //update win count for current player
                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_number_of_wins), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                        .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult result) {
                                processWin(result);
                            }
                        });
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_first_win));
            } else if (matchResult.equals("LOSS")) {
                pResult = ParticipantResult.MATCH_RESULT_LOSS;
                pPosition = 2;
                oResult = ParticipantResult.MATCH_RESULT_WIN;
                oPosition = 1;
                mTurnData.winScore = oFinalScore;
                mTurnData.loseScore = pFinalScore;
                if (myParticipantId.equals("p_1")) {
                    mTurnData.winner = "p_2";
                } else {
                    mTurnData.winner = "p_1";
                }
            } else {
                pResult = ParticipantResult.MATCH_RESULT_TIE;
                pPosition = 1;
                oResult = ParticipantResult.MATCH_RESULT_TIE;
                oPosition = 1;
                mTurnData.winScore = oFinalScore;
                mTurnData.loseScore = pFinalScore;
                mTurnData.winner = "tie";
            }

            ParticipantResult opponentResult = new ParticipantResult(opponentID, oResult, oPosition);
            ParticipantResult playerResult = new ParticipantResult(myID, pResult, pPosition);

            if (myParticipantId.equals("p_2")) {
                mTurnData.p2_guesses = correctGuesses.toString().replace("[", "").replace("]", "").replace(", ", ",");
                mTurnData.p2_name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
            } else {
                mTurnData.p1_guesses = correctGuesses.toString().replace("[", "").replace("]", "").replace(", ", ",");
                mTurnData.p1_name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
            }

            Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId(), mTurnData.persist(), playerResult, opponentResult)
                    .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                            processResult(result);
                        }
                    });

        }

        lastTurn = false;
        opponentGuesses = "";
        isDoingTurn = false;
        setViewVisibility();
    }


    // Upload your new gamestate, then take a turn, and pass it on to the next
    // player.
    public void onDoneClicked(View view) {
        showSpinner();

        String nextParticipantId = getNextParticipantId();
        // Create the next turn
        mTurnData.turnCounter += 1;

        mTurnData.initials = mInitialsView.getText().toString();
        if (nextParticipantId.equals("p_2")) {
            mTurnData.p1_guesses = correctGuesses.toString().replace("[", "").replace("]", "").replace(", ", ",");
            mTurnData.p1_name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
        } else {
            mTurnData.p2_guesses = correctGuesses.toString().replace("[", "").replace("]", "").replace(", ", ",");
            mTurnData.p2_name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
        }

        showSpinner();
        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                mTurnData.persist(), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        Toast.makeText(this, "Names Submitted - Your opponent will finish the match", TOAST_DELAY).show();
        mTurnData = null;
    }



    // Switch to gameplay view.
    public void setGameplayUI() {
        isDoingTurn = true;
        setViewVisibility();
        //mDataView.setText(mTurnData.data);
        //mTurnTextView.setText("Turn " + mTurnData.turnCounter);
    }

    // Helpful dialogs

    public void showSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.GONE);
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                isDoingTurn = false;

                GameTurn finalData = GameTurn.unpersist(mMatch.getData());

                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {



                } else {
                    //award win if game creator wins
                    if (myParticipantId.equals("p_1") && finalData.winner.equals("p_1")){
                        //update win count for current player
                        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_number_of_wins), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                                .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                                    @Override
                                    public void onResult(Leaderboards.LoadPlayerScoreResult result) {
                                        processWin(result);
                                    }
                                });
                        Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_first_win));
                    }

                    //update unfinished match
                    Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
                            .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                    processResult(result);
                                }
                            });

                }

                ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(finalData.p1_guesses.split(",")));
                ArrayList<String> oppArray = new ArrayList<String>(Arrays.asList(finalData.p2_guesses.split(",")));

                showScore(finalData.p1_name, finalData.p2_name, playerArray, oppArray);

                break;
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = GameTurn.unpersist(mMatch.getData());
                setGameplayUI();
                if (mTurnData.turnCounter == 0){
                    setInitials("new");
                } else {
                    setInitials(mTurnData.initials);
                    if (myParticipantId.equals("p_2")) {
                        opponentGuesses = mTurnData.p1_guesses;
                    } else {
                        opponentGuesses = mTurnData.p2_guesses;
                    }
                    lastTurn = true;
                }
                if (isDoingTurn) {
                    turnReset();
                }
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;

        setViewVisibility();
    }

    private class PersonSearch extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            try {
                //check the proper initials

                String[] names = params[0].split("\\s+");

                if ((names[0].charAt(0) == initial1) && (names[1].charAt(0) == initial2)) {

                    HttpTransport httpTransport = new NetHttpTransport();
                    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                    JSONParser parser = new JSONParser();
                    GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
                    url.put("query", params[0]);
                    url.put("filter", "(all type:/people/person)");
                    url.put("limit", "3");
                    url.put("indent", "true");

                    HttpRequest request = requestFactory.buildGetRequest(url);
                    HttpResponse httpResponse = request.execute();
                    JSONObject response = (JSONObject) parser.parse(httpResponse.parseAsString());
                    JSONArray results = (JSONArray) response.get("result");

                    if (results.size() > 0) {
                        personTrue = false;
                        for (int j = 0; j < results.size(); j++) {
                            JSONObject obj = (JSONObject) results.get(j);
                            String name = (String) obj.get("name");
                            if (name.equals(params[0])) { //valid name
                                if (!correctGuesses.contains(name)) {  //did we already guess it?
                                    personTrue = true;
                                }
                            }
                        }
                    } else {
                        personTrue = false;
                    }
                } else {
                    personTrue = false;
                }

                //for (Object result : results) {
                    //resultText = resultText + JsonPath.read(result,"$.name").toString();
                //}
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return "done";
        }

        protected void onPostExecute(String Result){
            try{

                EditText uText = (EditText) findViewById(R.id.searchName);

                if (personTrue) {
                    correctSound.start();
                    correctGuesses.add(uText.getText().toString());
                } else {
                    wrongSound.start();
                }

                Guess newGuess = new Guess(uText.getText().toString(), personTrue);
                adapter.insert(newGuess, 0);
                uText.setText("");

            }catch(Exception E){
                Toast.makeText(getApplicationContext(), "Error:"+E.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatTime(long millis){

        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private String getRandomLetters() {
        Random rnd = new Random();
        int numLetters = 2;

        String randomLetters = "ABCDEFGHIJKLMNOPRSTWY";
        String initials = "";

        for (int n=0; n<numLetters; n++) {
            char newChar = randomLetters.charAt(rnd.nextInt(randomLetters.length()));
            initials += newChar + ". ";
            if (n == 0){
                initial1 = newChar;
            } else {
                initial2 = newChar;
            }
        }

        return initials;
    }

    public void turnReset(){

        correctGuesses = new ArrayList<String>();
        pFinalScore = 0;
        oFinalScore = 0;

        Button search = (Button) findViewById(R.id.searchButton);
        search.setEnabled(true);

        RelativeLayout completionPanel = (RelativeLayout) findViewById(R.id.completePanel);
        completionPanel.setVisibility(View.INVISIBLE);

        RelativeLayout finishPanel = (RelativeLayout) findViewById(R.id.finishPanel);
        finishPanel.setVisibility(View.INVISIBLE);

        clockSound.start();

        if (timeTrack != null) {
            timeTrack.cancel();
        }

        timeTrack = new CountDownTimer(turnTime, 1000) {

            TextView tText = (TextView) findViewById(R.id.timeBox);

            public void onTick(long millisUntilFinished) {
                timerIsRunning = true;
                tText.setText(formatTime(millisUntilFinished));
            }

            public void onFinish() {
                timerIsRunning = false;
                turnOver();
            }
        }.start();
    }

    public void turnOver() {
        timeTrack.cancel();
        TextView tText = (TextView) findViewById(R.id.timeBox);
        tText.setText("Time's Up!");
        Button search = (Button) findViewById(R.id.searchButton);
        search.setEnabled(false);
        clockSound.pause();
        clockSound.seekTo(0);
        endSound.start();
        hideKeyboard();

        //check guesses for achievement
        if (correctGuesses.size() >= 5){
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_five_spot));
        }

        if (correctGuesses.size() >= 10){
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_titanic_ten));
        }


        if (lastTurn) {
            findViewById(R.id.finishPanel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.completePanel).setVisibility(View.VISIBLE);
        }
    }

    // Update the visibility based on what state we're in.
    public void setViewVisibility() {
        boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());

        if (!isSignedIn) {
            findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);

            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            return;
        }


        ((TextView) findViewById(R.id.name_field)).setText(Games.Players.getCurrentPlayer(
                mGoogleApiClient).getDisplayName());
        findViewById(R.id.login_layout).setVisibility(View.GONE);

        if (isDoingTurn) {
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.matchup_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);
        }
    }

    // Rematch dialog
    public void askForRematch() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want a rematch?");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rematch();
                            }
                        })
                .setNegativeButton("No.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

        alertDialogBuilder.show();
    }

    // This function is what gets called when you return from either the Play
    // Games built-in inbox, or else the create game built-in interface.
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (request == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (response == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, request, response,
                        R.string.signin_failure, R.string.signin_other_error);
            }
        } else if (request == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            TurnBasedMatch match = data
                    .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                updateMatch(match);
            }

            Log.d(TAG, "Match = " + match);
        } else if (request == RC_SELECT_PLAYERS) {
            // Returned from 'Select players to Invite' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees = data
                    .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();

            // Start the match
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            processResult(result);
                        }
                    });
            showSpinner();
        }
    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(TurnBasedMatch match) {
        mTurnData = new GameTurn();
        // Some basic turn data
        mTurnData.initials = "";

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        showSpinner();

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }

    // If you choose to rematch, then call it and wait for a response.
    public void rematch() {
        showSpinner();
        Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                });
        mMatch = null;
        isDoingTurn = false;
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }

    public String getOpponentName() {

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);

        String myParticipantId = mMatch.getParticipantId(playerId);
        myID = myParticipantId;

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        String targID = "";

        for(String s : participantIds)  {
            if (!s.equals(myParticipantId)) {
                targID = s;
            }
        }

        Participant player = mMatch.getParticipant(targID);
        opponentID = player.getParticipantId();

        return player.getDisplayName();

    }

    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        dismissSpinner();

        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;

        showWarning("Match",
                "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }

        startMatch(match);
    }


    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        showWarning("Left", "You've left this match.");
    }


    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.canRematch()) {
            askForRematch();
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    public void processWin(Leaderboards.LoadPlayerScoreResult result) {
        long curScore = 0;
        if (isScoreResultValid(result)) curScore = result.getScore().getRawScore();
        curScore++;
        //update win count for current player
        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_number_of_wins), curScore);
    }

    // Handle notification events.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), TOAST_DELAY)
                .show();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        Toast.makeText(this, "An invitation was removed.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        Toast.makeText(this, "A match was updated.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchRemoved(String matchId) {
        Toast.makeText(this, "A match was removed.", TOAST_DELAY).show();

    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
                                 int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        this,
                        "Stored action for later.  (Please remove this toast before release.)",
                        TOAST_DELAY).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                mSignInClicked = true;
                mTurnBasedMatch = null;
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                mGoogleApiClient.connect();
                break;
            case R.id.sign_out_button:
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                setViewVisibility();
                break;
        }
    }
}
