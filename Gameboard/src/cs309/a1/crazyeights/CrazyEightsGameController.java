package cs309.a1.crazyeights;

import static cs309.a1.crazyeights.Constants.ID;
import static cs309.a1.crazyeights.Constants.NUMBER_OF_CARDS_PER_HAND;
import static cs309.a1.crazyeights.Constants.RESOURCE_ID;
import static cs309.a1.crazyeights.Constants.SUIT;
import static cs309.a1.crazyeights.Constants.VALUE;
import static cs309.a1.shared.CardGame.CRAZY_EIGHTS;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import cs309.a1.gameboard.R;
import cs309.a1.gameboard.activities.ConnectActivity;
import cs309.a1.gameboard.activities.GameResultsActivity;
import cs309.a1.gameboard.activities.GameboardActivity;
import cs309.a1.shared.Card;
import cs309.a1.shared.CardTranslator;
import cs309.a1.shared.Deck;
import cs309.a1.shared.Game;
import cs309.a1.shared.GameController;
import cs309.a1.shared.Player;
import cs309.a1.shared.Rules;
import cs309.a1.shared.Util;
import cs309.a1.shared.bluetooth.BluetoothConstants;
import cs309.a1.shared.bluetooth.BluetoothServer;


public class CrazyEightsGameController implements GameController {

	/**
	 * The Logcat Debug tag
	 */
	private static final String TAG = CrazyEightsGameController.class.getName();

	private static final int DECLARE_WINNER = Math.abs("DECLARE_WINNER".hashCode());

	private static final int CHOOSE_PLAYER = Math.abs("CHOOSE_PLAYER".hashCode());

	private BluetoothServer bts;

	private List<Player> players;

	private static Game game = null;

	private CardTranslator ct = new CrazyEightsCardTranslator();

	private int suitChosen = 0;

	private GameboardActivity gameContext;

	/**
	 * This will be 0 to 3 to indicate the spot in the players array for the player currently taking their turn
	 */
	private int whoseTurn = 0;

	private ImageButton refreshButton;

	public CrazyEightsGameController(GameboardActivity context, BluetoothServer btsGiven, List<Player> playersGiven, ImageButton refreshGiven) {
		gameContext = context;
		bts = btsGiven;
		players = playersGiven;
		refreshButton = refreshGiven;

		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setEnabled(false);
				refreshPlayers();
				v.setEnabled(true);
			}
		});

		Deck deck = new Deck(CRAZY_EIGHTS);
		Rules rules = new CrazyEightGameRules();
		game = CrazyEightsTabletGame.getInstance(players, deck, rules);
		game.setup();

		for (Player p : players) {
			if (Util.isDebugBuild()) {
				Log.d(TAG, p.getName() + ": " + p);
			}

			bts.write(Constants.SETUP, p, p.getId());
		}

		placeInitialCards();
		Card onDiscard = game.getDiscardPileTop();
		bts.write(Constants.IS_TURN, onDiscard, players.get(whoseTurn).getId());
	}

	private void placeInitialCards() {
		// If it is a debug build, show the cards face up so that we can
		// verify that the tablet has the same cards as the player
		if (Util.isDebugBuild()) {
			int i = 1;
			for (Player p : players) {
				for (Card c : p.getCards()){
					gameContext.placeCard(i, c);
				}

				i++;
			}

			gameContext.placeCard(0, game.getDiscardPileTop());
		} else {
			// Otherwise just show the back of the cards for all players
			for (int i = 1; i < NUMBER_OF_CARDS_PER_HAND * players.size(); i++) {
				gameContext.placeCard(i % 4 + 1, new Card(5, 0, R.drawable.back_blue_1, 54));
			}

			gameContext.placeCard(0, game.getDiscardPileTop());
		}
	}


	@Override
	public void handleBroadcastReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (BluetoothConstants.MESSAGE_RX_INTENT.equals(action)) {
			String object = intent.getStringExtra(BluetoothConstants.KEY_MESSAGE_RX);
			int messageType = intent.getIntExtra(BluetoothConstants.KEY_MESSAGE_TYPE, -1);

			switch(messageType){
			case Constants.PLAY_CARD:
				discardReceivedCard(object);
				advanceTurn();
				break;
			case Constants.PLAY_EIGHT_C:
				suitChosen = Constants.SUIT_CLUBS;
				discardReceivedCard(object);
				advanceTurn();
				break;
			case Constants.PLAY_EIGHT_D:
				suitChosen = Constants.SUIT_DIAMONDS;
				discardReceivedCard(object);
				advanceTurn();
				break;
			case Constants.PLAY_EIGHT_H:
				suitChosen = Constants.SUIT_HEARTS;
				discardReceivedCard(object);
				advanceTurn();
				break;
			case Constants.PLAY_EIGHT_S:
				suitChosen = Constants.SUIT_SPADES;
				discardReceivedCard(object);
				advanceTurn();
				break;
			case Constants.DRAW_CARD:
				drawCard();
				advanceTurn();
				break;
			case Constants.REFRESH:
				refreshPlayers();
				break;
			}
		}
	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GameboardActivity.DISCONNECTED) {
			if (resultCode == Activity.RESULT_CANCELED) {
				// We chose to drop the player, so let the Game know to do that
				String playerId = data.getStringExtra(BluetoothConstants.KEY_DEVICE_ID);
				game.dropPlayer(playerId);

				// TODO: should the UI be updated?
				// TODO: if this was the last player, should we go to the main menu?
			} else if (resultCode == Activity.RESULT_OK){
				// We chose to add a new player, so start the ConnectActivity
				Intent i = new Intent(gameContext, ConnectActivity.class);
				gameContext.startActivityForResult(i, CHOOSE_PLAYER);

				// TODO: we probably want to do something with the broadcast receiver
				// like unregister it so that we don't pop up messages while at the choose player screen...
			}

			return true;
		} else if (requestCode == CHOOSE_PLAYER) {
			// TODO: update the player with the new id
		}

		return false;
	}

	private void advanceTurn(){
		if (game.isGameOver(players.get(whoseTurn))) {
			declareWinner(whoseTurn);
		}

		if (Util.isDebugBuild()) {
			//for debugging to see number of cards
			Toast.makeText(gameContext.getApplicationContext(), "player "+game.getPlayers().get(whoseTurn).getCards().size()+" cards.", Toast.LENGTH_SHORT).show();
		}

		if (whoseTurn < game.getNumPlayers() - 1) {
			whoseTurn++;
		} else {
			whoseTurn = 0;
		}

		Card onDiscard = game.getDiscardPileTop();
		if (onDiscard.getValue() == 7) {
			onDiscard = new Card(suitChosen, onDiscard.getValue(), onDiscard.getResourceId(), onDiscard.getIdNum());
		}
		bts.write(Constants.IS_TURN, onDiscard, players.get(whoseTurn).getId());
	}

	private void declareWinner(int whoWon){
		if (Util.isDebugBuild()) {
			Toast.makeText(gameContext, "Sending winner and loser info", Toast.LENGTH_SHORT);
		}

		for(int i = 0; i < game.getNumPlayers(); i++) {
			if (i == whoWon) {
				bts.write(Constants.WINNER, null, players.get(i).getId());
			} else {
				bts.write(Constants.LOSER, null, players.get(i).getId());
			}
		}

		Intent gameResults = new Intent(gameContext, GameResultsActivity.class);
		gameResults.putExtra(GameResultsActivity.WINNER_NUMBER, whoWon + 1);
		gameContext.startActivityForResult(gameResults, DECLARE_WINNER);
		//TODO start ending activity
	}

	/**
	 * This draws a card in the tablet game instance and sends that card to the player
	 */
	private void drawCard() {
		Card tmpCard = game.draw(players.get(whoseTurn));
		
		//TODO may need to make this a generic back of card
		gameContext.placeCard(players.get(whoseTurn).getPosition(), tmpCard);
		bts.write(Constants.CARD_DRAWN, tmpCard, players.get(whoseTurn).getId());
	}

	/**
	 * This will take in the received card and discard it
	 * @param object
	 */
	private void discardReceivedCard(String object) {
		Card tmpCard = new Card(0, 0, 0, 0);
		try {
			JSONObject obj = new JSONObject(object);
			int suit = obj.getInt(SUIT);
			int value = obj.getInt(VALUE);
			int id = obj.getInt(ID);
			tmpCard = new Card(suit, value, ct.getResourceForCardWithId(id), id);

			gameContext.removeCard(players.get(whoseTurn).getPosition());
			game.discard(players.get(whoseTurn), tmpCard);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		gameContext.placeCard(0, tmpCard);
	}

	private void refreshPlayers() {
		Player pTurn = players.get(whoseTurn);

		for (Player p : players) {
			if (Util.isDebugBuild()) {
				Log.d(TAG, p.getName() + " refreshed : " + p);
			}

			try {
				JSONArray arr = new JSONArray();
				JSONObject refreshInfo = new JSONObject();
				refreshInfo.put(Constants.TURN, pTurn.equals(p));
				//Maybe add more refresh info here

				arr.put(refreshInfo);

				//TODO BRIAN do I need to do this if I am going to have an array with
				//     a refresh object then an array of cards
				//     or can I just do arr.put(players)   ? doing arr.put(arr2) essentially
				//FROM BRIAN: I don't understand the question...
				for (Card c : p.getCards()) {
					JSONObject obj = new JSONObject();
					obj.put(SUIT, c.getSuit());
					obj.put(VALUE, c.getValue());
					obj.put(RESOURCE_ID, c.getResourceId());
					obj.put(ID, c.getIdNum());

					arr.put(obj);
				}

				arr.toString();
				bts.write(Constants.REFRESH, arr.toString(), p.getId());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}

