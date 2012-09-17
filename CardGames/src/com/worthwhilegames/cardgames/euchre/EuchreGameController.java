package com.worthwhilegames.cardgames.euchre;

import static com.worthwhilegames.cardgames.euchre.EuchreConstants.BET;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.FIRST_ROUND_BETTING;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.GO_ALONE;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.LEAD_TRICK;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PICK_IT_UP;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.PLAY_LEAD_CARD;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.ROUND_OVER;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.SECOND_ROUND_BETTING;
import static com.worthwhilegames.cardgames.euchre.EuchreConstants.TRUMP;
import static com.worthwhilegames.cardgames.shared.Constants.ID;
import static com.worthwhilegames.cardgames.shared.Constants.IS_TURN;
import static com.worthwhilegames.cardgames.shared.Constants.SUIT;
import static com.worthwhilegames.cardgames.shared.Constants.VALUE;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.worthwhilegames.cardgames.gameboard.activities.ConnectActivity;
import com.worthwhilegames.cardgames.gameboard.activities.GameboardActivity;
import com.worthwhilegames.cardgames.shared.Card;
import com.worthwhilegames.cardgames.shared.CardTranslator;
import com.worthwhilegames.cardgames.shared.Constants;
import com.worthwhilegames.cardgames.shared.GameController;
import com.worthwhilegames.cardgames.shared.Player;
import com.worthwhilegames.cardgames.shared.SoundManager;
import com.worthwhilegames.cardgames.shared.Util;
import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
import com.worthwhilegames.cardgames.shared.connection.ConnectionServer;

public class EuchreGameController implements GameController{

	/**
	 * The Logcat Debug tag
	 */
	private static final String TAG = EuchreGameController.class.getName();

	/**
	 * The request code to keep track of the "Player N Won!" activity
	 */
	private static final int DECLARE_WINNER = Math.abs("DECLARE_WINNER".hashCode());

	/**
	 * request code to allow the gameboard to choose which player to connect
	 */
	private static final int CHOOSE_PLAYER = Math.abs("CHOOSE_PLAYER".hashCode());

	/**
	 * The ConnectionServer that sends and receives messages from other devices
	 */
	private ConnectionServer server;

	/**
	 * the list of current active players
	 */
	private List<Player> players;

	/**
	 * This game object will keep track of the current state of the game and be
	 * used to manage player hands and draw and discard piles
	 */
	private static EuchreTabletGame game = null;

	/**
	 * This will be 0 to 3 to indicate the spot in the players array for the
	 * player currently taking their turn
	 */
	private int whoseTurn = 0;


	/**
	 * This is the current state of the game
	 * Corresponds to the first round of betting second round of betting
	 * trick leading etc constants in euchre constants and shared constants
	 */
	private int currentState;

	/**
	 * This allows the resource ids of cards to be correct
	 */
	private CardTranslator ct = new EuchreCardTranslator();

	/**
	 * This is the context of the GameBoardActivity this allows this class to
	 * call methods and activities as if it were in the GameBoardActivity
	 */
	private GameboardActivity gameContext;

	/**
	 * This is the refresh button that is on the GameBoard The GameController
	 * will handle any button presses
	 */
	private ImageView refreshButton;

	/**
	 * The implementation of the Game Rules
	 */
	private EuchreGameRules gameRules = new EuchreGameRules();

	/**
	 * The sound manager
	 */
	private SoundManager mySM;

	/**
	 * This is how to tell if a play computer turn activity is currently running
	 */
	private boolean isComputerPlaying = false;

	/**
	 * Represents whether the game is paused or not
	 */
	private boolean isPaused = false;

	/**
	 * Handler to handle a computer's turn
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (Util.isDebugBuild()) {
				Log.d(TAG, "handleMessage: about to play a card");
			}

			if (!isPaused) {
				isComputerPlaying = false;
				//This is the turnCode for the computer
				playComputerTurn(msg.arg1);
			} else {
				if (Util.isDebugBuild()) {
					Log.d(TAG, "handleMessage: game paused. not going to play now");
				}
			}
		}
	};

	/**
	 * @param context
	 * @param connectionGiven
	 * @param playersGiven
	 * @param refreshGiven
	 */
	public EuchreGameController(GameboardActivity context,	ConnectionServer connectionGiven, ImageView refreshGiven) {
		gameContext = context;
		server = connectionGiven;
		refreshButton = refreshGiven;
		mySM = SoundManager.getInstance(context);

		refreshButton.setOnClickListener(new OnClickListener() {
			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				v.setEnabled(false);
				refreshPlayers();
				v.setEnabled(true);
			}
		});

		game = EuchreTabletGame.getInstance();
		game.setup();

		players = game.getPlayers();

		startRound();

	}

	private void startRound(){
		game.startRound();
		whoseTurn = game.getTrickLeader();

		gameContext.highlightPlayer(whoseTurn);

		for (Player p : players) {
			if (Util.isDebugBuild()) {
				Log.d(TAG, p.getName() + ": " + p);
			}

			server.write(Constants.SETUP, p, p.getId());
		}

		placeInitialCards();

		// If this is a computer, start having the computer play
		currentState = FIRST_ROUND_BETTING;

		//tell first person to bet
		sendNextTurn(currentState, game.getTopCard());

	}

	/**
	 * This will place the initial cards of each player
	 */
	private void placeInitialCards() {
		mySM.shuffleCardsSound();

		gameContext.updateUi();

		// Update the indicator on the gameboard with the current suit
		gameContext.updateSuit(game.getDiscardPileTop().getSuit());
	}

	/* (non-Javadoc)
	 * @see com.worthwhilegames.cardgames.shared.GameController#handleBroadcastReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void handleBroadcastReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (ConnectionConstants.MESSAGE_RX_INTENT.equals(action)) {
			String object = intent.getStringExtra(ConnectionConstants.KEY_MESSAGE_RX);
			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, -1);


			if(whoseTurn != getWhoSentMessage(context, intent)){
				//TODO the wrong person just sent a message
				//additional security should have had in original
				refreshPlayers();
			} else {
				//the right person responded.
				switch (messageType) {
				case FIRST_ROUND_BETTING:
					isComputerPlaying = false;
					handleBetting(FIRST_ROUND_BETTING, object);
					break;
				case SECOND_ROUND_BETTING:
					isComputerPlaying = false;
					handleBetting(SECOND_ROUND_BETTING, object);
					break;
				case PLAY_LEAD_CARD:
					isComputerPlaying = false;
					Card tmpCard = playReceivedCard(object);
					game.setCardLead(tmpCard);
					advanceTurn();
					break;
				case PICK_IT_UP:
					isComputerPlaying = false;
					//Get which card they discarded and discard it.
					currentState = LEAD_TRICK;
					playReceivedCard(object);
					game.clearCardsPlayed();

					//start the first turn of the round
					whoseTurn = game.getTrickLeader();
					server.write(LEAD_TRICK, null, players.get(whoseTurn).getId());
					break;
				case Constants.PLAY_CARD:
					isComputerPlaying = false;
					playReceivedCard(object);
					advanceTurn();
					break;
				case Constants.REFRESH:
					refreshPlayers();
					break;
				}
			}

		}

	}

	/**
	 * This method will return the player number that sent the message to the gameboard
	 * @param context
	 * @param intent
	 * @return
	 * index of the player that sent the message
	 */
	private int getWhoSentMessage(Context context, Intent intent){
		//TODO this whole method
		return whoseTurn;
	}

	@Override
	public boolean handleActivityResult(int requestCode, int resultCode,
			Intent data) { //TODO make in abstract class
		if (requestCode == GameboardActivity.DISCONNECTED) {
			if (resultCode == Activity.RESULT_CANCELED) {
				// We chose to drop the player, so let the Game know to do that
				String playerId = data.getStringExtra(ConnectionConstants.KEY_DEVICE_ID);
				game.dropPlayer(playerId);
				refreshPlayers();
			} else if (resultCode == Activity.RESULT_OK) {
				// We chose to add a new player, so start the ConnectActivity
				// with the deviceId and isReconnect parameters
				Intent i = new Intent(gameContext, ConnectActivity.class);
				i.putExtra(ConnectionConstants.KEY_DEVICE_ID, data.getStringExtra(ConnectionConstants.KEY_DEVICE_ID));
				gameContext.startActivityForResult(i, CHOOSE_PLAYER);

				// We will initially drop the player, to handle the case where
				// they don't actually reconnect a player in the Connect Screen.
				game.dropPlayer(data.getStringExtra(ConnectionConstants.KEY_DEVICE_ID));

				// Unregister the receiver so that we don't get an annoying
				// popup when we are on the activity
				gameContext.unregisterReceiver();
			}

			return true;
		} else if (requestCode == CHOOSE_PLAYER) {
			// We are coming back from the reconnect player screen
			if (Util.isDebugBuild()) {
				Log.d(TAG, "onActivityResult: CHOOSE_PLAYER");
			}

			// Send the refresh signal to all players just to make
			// sure everyone has the latest information
			refreshPlayers();

			// Pause the players
			unpause();

			// Re-register the broadcast receivers
			gameContext.registerReceiver();

			// Update the gameboard with the correct player names
			gameContext.updateNamesOnGameboard();

			// Send the refresh signal (again) to all players just to make
			// sure everyone has the latest information
			refreshPlayers();
		}


		return false;
	}

	/* (non-Javadoc)
	 * @see cs309.a1.shared.GameController#pause()
	 */
	@Override
	public void pause() {//TODO make in abstract class
		isPaused = true;

		for (int i = 0; i < game.getNumPlayers(); i++) {
			server.write(Constants.PAUSE, null, players.get(i).getId());
		}
	}

	/* (non-Javadoc)
	 * @see cs309.a1.shared.GameController#unpause()
	 */
	@Override
	public void unpause() {//TODO make in abstract class
		for (int i = 0; i < game.getNumPlayers(); i++) {
			server.write(Constants.UNPAUSE, null, players.get(i).getId());
		}

		isPaused = false;

		// If a computer was playing before the game was paused
		// let them know that they can play now
		if (isComputerPlaying) {
			handler.sendEmptyMessage(0);
		}
	}

	/* (non-Javadoc)
	 * @see cs309.a1.shared.GameController#sendGameEnd()
	 */
	@Override
	public void sendGameEnd() {//TODO make in abstract class
		for (int i = 0; i < game.getNumPlayers(); i++) {
			server.write(Constants.END_GAME, null, players.get(i).getId());
		}
	}

	/**
	 * This function will be called when a players turn is over It will change
	 * whoseTurn to the next player and send them the message that it is their
	 * turn
	 */
	private void advanceTurn() {
		isComputerPlaying = false;

		incrementWhoseTurn();

		if(whoseTurn == game.getTrickLeader()){
			//The round is over
			game.determineTrickWinner();

			//TODO better way to determine if round is over
			if(players.get(0).getCards().size() == 0){
				game.endRound();
				if(game.isGameOver(players.get(0))){
					//TODO end game.
				}
				declareRoundScores();
				startRound();
			} else {
				//round not over just the current trick

				//TODO update scores on gameboard
				gameContext.updateUi();

				//make the trick leader to play next
				whoseTurn = game.getTrickLeader();
				currentState = PLAY_LEAD_CARD;

				// Highlight the name of the current player
				gameContext.highlightPlayer(whoseTurn);

				sendNextTurn(currentState, game.getCardLead());
			}

			return;
		}

		// Highlight the name of the current player
		gameContext.highlightPlayer(whoseTurn);

		currentState = IS_TURN;

		//tell the next person to play
		sendNextTurn(currentState, game.getCardLead());


		// Update the UI
		gameContext.updateUi();
	}



	/**
	 * This method will send players info about the score
	 */
	private void declareRoundScores(){

		//TODO remove toast
		Toast.makeText(gameContext, "Scores team 1:" + game.getMatchScores()[0] + " Team 2:" + game.getMatchScores()[1], Toast.LENGTH_SHORT);

		for (int i = 0; i < game.getNumPlayers(); i++) {
			server.write(ROUND_OVER, null, players.get(i).getId());
		}
	}


	/**
	 * This will take in the received card and play it
	 * 
	 * @param object
	 *            This object is a JSON object that has been received as a
	 *            played card
	 */
	private Card playReceivedCard(String object) {
		Card tmpCard = new Card(0, 0, 0, 0);
		try {
			JSONObject obj = new JSONObject(object);
			int suit = obj.getInt(SUIT);
			int value = obj.getInt(VALUE);
			int id = obj.getInt(ID);
			tmpCard = new Card(suit, value, ct.getResourceForCardWithId(id), id);

			//tell the game what was played
			game.discard(players.get(whoseTurn), tmpCard);

			//update UI
			gameContext.updateUi();
		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		mySM.playCardSound();
		return tmpCard;
	}

	/**
	 * This will refresh the state of all the players by sending them their
	 * cards and if it is their turn
	 */
	private void refreshPlayers() {
		Player pTurn = players.get(whoseTurn);

		// TODO get the card lead with
		Card discard = game.getDiscardPileTop();

		JSONObject discardObj = discard.toJSONObject();

		for (Player p : players) {
			if (Util.isDebugBuild()) {
				Log.d(TAG, p.getName() + " refreshed : " + p);
			}

			try {
				JSONArray arr = new JSONArray();

				// Create the base refresh info object
				JSONObject refreshInfo = new JSONObject();
				refreshInfo.put(Constants.TURN, pTurn.equals(p));
				refreshInfo.put(Constants.PLAYER_NAME, p.getName());
				refreshInfo.put(TRUMP, game.getTrump());
				arr.put(refreshInfo);

				// send the card on the discard pile
				arr.put(discardObj);

				// send all the cards in the players hand
				for (Card c : p.getCards()) {
					arr.put(c.toJSONObject());
				}

				server.write(Constants.REFRESH, arr.toString(), p.getId());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// If the next player is a computer, and the computer isn't currently
		// playing,
		// have the computer initiate a move
		if (players.get(whoseTurn).getIsComputer() && !isComputerPlaying) {
			startComputerTurn(currentState);
		}
	}


	/**
	 * Start a computer's turn.
	 * 
	 * Starts another thread that waits, and then posts a message to the
	 * mHandler letting it know it can play.
	 */
	private void startComputerTurn(final int turnCode) {
		isComputerPlaying = true;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(Constants.COMPUTER_WAIT_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (Util.isDebugBuild()) {
					Log.d(TAG, "startComputerTurn: letting computer know it can play");
				}

				Message msg = new Message();
				msg.arg1 = turnCode;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void playComputerTurn(int turnCode) {
		//TODO update this to get the card lead with
		Card onDiscard = game.getDiscardPileTop();

		List<Card> cards = players.get(whoseTurn).getCards();
		Card cardSelected = null;
		EuchreBet compBet = new EuchreBet(game.getCardLead().getSuit(), false, false);

		//computer with difficulty
		//TODO right now only has easy
		if (true || players.get(whoseTurn).getComputerDifficulty().equals(Constants.EASY)) {
			switch(turnCode){
			case FIRST_ROUND_BETTING:
				break;
			case SECOND_ROUND_BETTING:
				break;
			case LEAD_TRICK:
				cardSelected = cards.get(0);
				break;
			case IS_TURN:
				//TODO
				for (Card c : cards) {
					if (gameRules.checkCard(c, game.getTrump(), game.getCardLead().getSuit(), cards)) {
						cardSelected = c;
						break;
					}
				}
				break;
			}

			//TODO computer difficulty Medium
		} else if (players.get(whoseTurn).getComputerDifficulty().equals(Constants.MEDIUM) ) {
			switch(turnCode){
			case FIRST_ROUND_BETTING:

				break;
			case SECOND_ROUND_BETTING:

				break;
			case LEAD_TRICK:
				break;
			case IS_TURN:
				for (Card c : cards) {
					if (gameRules.checkCard(c, onDiscard)) {
						cardSelected = c;
						break;
					}
				}
				break;
			}

			//TODO computer difficulty Hard
		} else if (players.get(whoseTurn).getComputerDifficulty().equals(Constants.HARD)) {
			switch(turnCode){
			case FIRST_ROUND_BETTING:

				break;
			case SECOND_ROUND_BETTING:

				break;
			case LEAD_TRICK:

				break;
			case IS_TURN:

				for (Card c : cards) {
					if (gameRules.checkCard(c, onDiscard)) {
						cardSelected = c;
						break;
					}
				}
				break;
			}
		}

		if( currentState == FIRST_ROUND_BETTING || currentState == SECOND_ROUND_BETTING){
			this.handleBetting(currentState, compBet.toString());
		} else if (cardSelected != null) {
			//Play Card
			mySM.playCardSound();
			game.discard(players.get(whoseTurn), cardSelected);
			advanceTurn();
		} else {
			//TODO something broke.
			Toast.makeText(gameContext, "FAILURE BY COMPUTER", Toast.LENGTH_LONG);
			// If card is null then there are no cards to draw so just move on and allow the turn to advance
			mySM.drawCardSound();
		}
	}

	private void handleBetting(int round, String object) {
		isComputerPlaying = false;

		EuchreBet bet = null;
		try {
			JSONObject obj = new JSONObject(object);
			int trumpSuit = obj.getInt(TRUMP);
			boolean placeBet = obj.getBoolean(BET);
			boolean goAlone = obj.getBoolean(GO_ALONE);

			bet = new EuchreBet(trumpSuit, placeBet, goAlone);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		//first round of betting
		if(round == FIRST_ROUND_BETTING){
			if(bet.getPlaceBet()){
				if(bet.getTrumpSuit() == game.getTopCard().getSuit()){
					//set trump and trump caller
					game.setTrump(bet.getTrumpSuit());
					game.pickItUp(players.get(whoseTurn));
					// Update the Game board display with an indication of the current suit
					gameContext.updateSuit(game.getTrump());

					whoseTurn = game.getDealer();
					if(bet.getGoAlone()){
						//TODO go alone stuff
					}
					whoseTurn = game.getDealer();
					currentState = PICK_IT_UP;

					//tell the dealer to "pick it up"
					players.get(game.getDealer()).addCard(game.getTopCard());
					sendNextTurn(currentState, game.getTopCard());
					return;
				} else {
					//TODO error should not get here.
				}
				return;
			} else {
				if(whoseTurn == game.getDealer()){
					//start betting round 2
					currentState = SECOND_ROUND_BETTING;
				}
				incrementWhoseTurn();

				sendNextTurn(currentState, game.getTopCard());
			}

			//second round of betting
		} else if(round == SECOND_ROUND_BETTING) {
			if(bet.getPlaceBet()){
				game.setTrump(bet.getTrumpSuit());
				game.pickItUp(players.get(whoseTurn));
				gameContext.updateSuit(game.getTrump());


				if(bet.getGoAlone()){
					//TODO go alone stuff
				}
				//set the turn to the first player to play and go
				whoseTurn = game.getTrickLeader();

				game.clearCardsPlayed();

				currentState = PLAY_LEAD_CARD;
				sendNextTurn(currentState, null);
				return;
			} else {
				if(whoseTurn == game.getDealer()){
					//should never get here, but if we do. force the dealer to choose the trump
					server.write(currentState, game.getTopCard(), players.get(whoseTurn).getId());
				}
				incrementWhoseTurn();

				sendNextTurn(currentState, game.getTopCard());
			}
		}


	}


	/**
	 * Sends the next turn information to either the computer or player.
	 * @param state the message type to send to the player or computer
	 * @param card the card to give the player or computer
	 */
	private void sendNextTurn(int state, Card card){
		if (players.get(whoseTurn).getIsComputer()) {
			if (!isComputerPlaying) {
				isComputerPlaying = true;
				startComputerTurn(state);
			}
		} else {
			server.write(state, card, players.get(whoseTurn).getId());
		}
	}

	/**
	 * This function will change whose turn it is to the next player.
	 */
	private void incrementWhoseTurn(){
		//TODO make stuff for go alone.
		if (whoseTurn < game.getNumPlayers() - 1) {
			whoseTurn++;
		} else {
			whoseTurn = 0;
		}
	}


}
