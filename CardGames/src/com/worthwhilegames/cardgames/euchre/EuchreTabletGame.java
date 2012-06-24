package com.worthwhilegames.cardgames.euchre;

import static com.worthwhilegames.cardgames.euchre.EuchreConstants.EUCHRE_SCORE_LIMIT;
import static com.worthwhilegames.cardgames.shared.Constants.JACK_VALUE;
import static com.worthwhilegames.cardgames.shared.Constants.SUIT_CLUBS;
import static com.worthwhilegames.cardgames.shared.Constants.SUIT_DIAMONDS;
import static com.worthwhilegames.cardgames.shared.Constants.SUIT_HEARTS;
import static com.worthwhilegames.cardgames.shared.Constants.SUIT_SPADES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.worthwhilegames.cardgames.shared.Card;
import com.worthwhilegames.cardgames.shared.CardGame;
import com.worthwhilegames.cardgames.shared.Constants;
import com.worthwhilegames.cardgames.shared.Deck;
import com.worthwhilegames.cardgames.shared.Game;
import com.worthwhilegames.cardgames.shared.Player;
import com.worthwhilegames.cardgames.shared.Rules;
import com.worthwhilegames.cardgames.shared.Util;

public class EuchreTabletGame implements Game{

	/**
	 * A tag for the class name
	 */
	private static final String TAG = EuchreTabletGame.class.getName();

	/**
	 * A variable for and instance of the euchre game type
	 */
	private static EuchreTabletGame instance = null;

	/**
	 * A private variable for a list of players in the current game
	 */
	private List<Player> players;

	/**
	 * A private variable representing the game deck for the euchre game
	 */
	private Deck gameDeck;

	/**
	 * A private variable for the rules interface for the euchre game
	 */
	private Rules rules;

	/**
	 * A private variable to represent the difficulty of computers in the current game
	 */
	private String computerDifficulty = Constants.EASY;

	/**
	 * An integer to represent the trump suit
	 */
	private int trump;

	/**
	 * An integer to represent the player who picked up the trump card or named the suit
	 */
	private int playerCalledTrump;

	/**
	 * An integer to represent the player who is the dealer
	 */
	private int dealer;

	/**
	 * This is whoever started the current trick
	 */
	private int trickLeader;

	/**
	 * An iterator for removing cards from the shuffled deck
	 */
	private Iterator<Card> iter;

	/**
	 * A list of all the cards in the shuffle deck
	 */
	private ArrayList<Card> shuffledDeck;

	/**
	 * This is how many tricks each team has won in a round
	 */
	private int[] roundScores = new int[2];

	/**
	 * This is the total points for the team
	 */
	private int[] matchScores = new int[2];;

	/**
<<<<<<< HEAD
	 * A card to represent the first card turned over for players to bet on
	 */
	private Card topCard;

	/**
=======
>>>>>>> Made some changes to the controllers. including updating refresh to

	 * list of the cards played the last round
	 */
	private ArrayList<Card> cardsPlayed;

	/**
	 * The first card played for a trick or the card flipped over to bet on
	 */
	private Card cardLead;

	/**
	 * Represents whether the game is currently active
	 */
	private boolean gameActive = false;

	/**
	 * Create a new instance of the tablet game so that multiple classes are able to reference
	 * the same card game and only one instance will be made available. This method uses the default
	 * constructor.
	 * 
	 * @return an instance of CrazyEightsTabletGame
	 */
	public static EuchreTabletGame getInstance() {
		if (instance == null) {
			instance = new EuchreTabletGame();
		}

		return instance;
	}

	/**
	 * Clear the game instance
	 */
	public static void clearInstance() {
		instance = null;
	}

	/**
	 * A constructor for the crazy eights game type. This constructor will initialize the all the variables
	 * for a game of euchre including the rules, players, deck, shuffled deck pile and the discard pile.
	 * 
	 * @param players a list of Player objects in the current game
	 * @param gameDeck a deck of cards to be used in the game euchre
	 * @param rules a Rules object for the euchre game
	 */
	public EuchreTabletGame(List<Player> players, Deck gameDeck, Rules rules) {
		this.players = players;
		this.gameDeck = gameDeck;
		this.rules = rules;
		shuffledDeck = gameDeck.getCardIDs();
	}

	public EuchreTabletGame() {
		// TODO
	}

	/**
	 * This method will set up the initial game
	 */
	@Override
	public void setup() {
		roundScores[0] = 0;
		roundScores[1] = 0;
		matchScores[0] = 0;
		matchScores[1] = 0;

		//this is -1 because start round sets it to dealer +1 so it will start as 0;
		setDealer(-1);
	}

	/**
	 * This method will shuffle the cards and distribute them to the players in a 3-2 deal pattern
	 */
	@Override
	public void deal() {
		gameActive = true;
		for(int i = 0; i < 2; i++){
			for (Player p : players) {
				if(i == 0){
					p.addCard(iter.next());
					iter.remove();
					p.addCard(iter.next());
					iter.remove();
					p.addCard(iter.next());
					iter.remove();
				}else{
					p.addCard(iter.next());
					iter.remove();
					p.addCard(iter.next());
					iter.remove();
				}

			}
		}

		cardLead = iter.next();
		iter.remove();
		trump = cardLead.getSuit();

		cardsPlayed.add(getDealer(), cardLead);
	}

	/**
	 * This method is not used in the game of Euchre
	 */
	@Override
	public Card draw(Player player) {
		//No drawing in this game
		return null;
	}

	/**
	 * This method will remove a card from the players hand
	 * 
	 * @param player the player to remove the card from
	 * @param card the card to be removed
	 */
	@Override
	public void discard(Player player, Card card) {
		player.getCards().remove(card);
		cardsPlayed.add(players.indexOf(player), card);
	}

	/**
	 * This method will shuffle the initial deck making use of the Collections.shuffle method
	 */
	@Override
	public void shuffleDeck() {
		//create a random number generator
		Random generator = new Random();

		//shuffle the deck
		Collections.shuffle(shuffledDeck, generator);

		//set the iterator to go through the shuffled deck
		iter = shuffledDeck.iterator();

	}

	/**
	 * * This will end a series of 5 tricks and calculate the score
	 */
	public void endRound(){

		int bettingTeam = playerCalledTrump % 2;
		if( roundScores[bettingTeam] >= 3){
			//TODO add go alone scoring
			if( roundScores[bettingTeam] > 4 ){
				matchScores[bettingTeam] += 2;
			} else {
				matchScores[bettingTeam] ++;
			}
		} else {
			//betting team has been "Euchred" 2 points for defending team
			matchScores[(bettingTeam +1) % 2] += 2;
		}


		this.clearCardsPlayed();

	}

	/**
	 * This will start the next round by dealing new cards and setting up the card to bet on
	 */
	public void startRound(){
		//make the dealer the next player this also resets the trick leader
		this.setDealer(getDealer() + 1);


		//redeal
		this.gameDeck = new Deck(CardGame.EUCHRE);
		shuffledDeck = gameDeck.getCardIDs();
		this.shuffleDeck();
		this.deal();
	}

	/**
	 * This method is specific to Euchre and allows the player who called trump to be set
	 * 
	 * @param player the player who called trump
	 */
	public void pickItUp(Player player){
		playerCalledTrump = player.getPosition();
	}

	/**
	 * This method will drop a player from the current game by removing them from the players list and
	 * making them into a computer
	 * 
	 * @param playerMacAddress the unique id of the player to be dropped
	 */
	@Override
	public void dropPlayer(String playerMacAddress) {
		if (Util.isDebugBuild()) {
			Log.d(TAG, "dropPlayer: " + playerMacAddress);
		}

		Player p = null;

		for (Player player : players) {
			if (player.getId().equals(playerMacAddress)) {
				p = player;
				break;
			}
		}

		if (gameActive) {
			if (p != null) {
				p.setIsComputer(true);
				p.setComputerDifficulty(computerDifficulty);

				// TODO
				//				maxNumberOfPlayers--;
			} else {
				if (Util.isDebugBuild()) {
					Log.d(TAG, "dropPlayer: couldn't find player with id: " + playerMacAddress);
				}
			}
		} else {
			// If the game hasn't been started yet, just remove them from the list
			players.remove(p);
		}
	}



	@Override

	public Card getDiscardPileTop() {
		//no discard pile
		return null;
	}

	@Override
	public int getNumPlayers() {
		return players.size();
	}

	@Override
	public boolean isGameOver(Player player) {
		//TODO make the match score limit a setting?
		return matchScores[0] >= EUCHRE_SCORE_LIMIT || matchScores[1] >= EUCHRE_SCORE_LIMIT;
	}

	@Override
	public List<Player> getPlayers() {
		return players;
	}

	@Override
	public ArrayList<Card> getShuffledDeck() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setComputerDifficulty(String diff) {
		computerDifficulty = diff;
	}

	@Override
	public String getComputerDifficulty() {
		return computerDifficulty;
	}
	/**
	 * This method will determine the winner of a trick
	 * 
	 * @param cards a list of card to determine the winner from
	 * @param suitLed the suit led in the current round
	 * @return the card id of the winning card
	 */
	public void determineTrickWinner(){
		Card winningCard = cardsPlayed.get(0);
		adjustCards(winningCard);
		int winningPlayer = 0;

		for(int i = 1; i < cardsPlayed.size(); i++){
			Card card = cardsPlayed.get(i);
			adjustCards(card);
			winningCard = compareCards(winningCard, card, cardLead.getSuit());
			if(winningCard.equals(card)){
				winningPlayer = i;
			}
		}

		//add a trick taken to the round score for the player
		roundScores[winningPlayer % 2]++;

		clearCardsPlayed();

		setTrickLeader(winningPlayer);

		return;
	}

	/**
	 * This method will compare two cards and return the winning card of the two
	 * 
	 * @param card the first card played
	 * @param card2 the second card played
	 * @param suitLed the suit led in the current round
	 * @return the better of the two cards based on the current round and trump
	 */
	public Card compareCards(Card card, Card card2, int suitLed){
		//null check for if someone is going alone then one player will never play
		if(card == null){
			return card2;
		}else if(card2 == null){
			return card;
		}

		if(card.getSuit() == trump && card2.getSuit() != trump){
			return card;
		}else if(card.getSuit() != trump && card2.getSuit() == trump){
			return card2;
		}else if(card.getSuit() == suitLed && card2.getSuit() != suitLed){
			return card;
		}else if(card.getSuit() != suitLed && card2.getSuit() == suitLed){
			return card2;
		}else{
			if(card.getValue() >= card2.getValue()){
				return card;
			}else{
				return card2;
			}
		}

	}


	/**
	 * This method will adjust the jack trumps and the value of an ace to a higher number to more
	 * easily determine the winner of a trick
	 * 
	 * @param card the card to adjust
	 */
	public void adjustCards(Card card){
		if( card == null ){
			return;
		}

		switch(trump){
		case SUIT_CLUBS:
			if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_SPADES){
				card.setSuit(SUIT_CLUBS);
				card.setValue(15);
			}else if(card.getValue() == JACK_VALUE&& card.getSuit() == SUIT_CLUBS){
				card.setValue(16);
			}
			break;

		case SUIT_DIAMONDS:
			if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_HEARTS){
				card.setSuit(SUIT_DIAMONDS);
				card.setValue(15);
			}else if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_DIAMONDS){
				card.setValue(16);
			}
			break;

		case SUIT_HEARTS:
			if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_DIAMONDS){
				card.setSuit(SUIT_HEARTS);
				card.setValue(15);
			}else if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_HEARTS){
				card.setValue(16);
			}
			break;

		case SUIT_SPADES:
			if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_CLUBS){
				card.setSuit(SUIT_SPADES);
				card.setValue(15);
			}else if(card.getValue() == JACK_VALUE && card.getSuit() == SUIT_SPADES){
				card.setValue(16);
			}
			break;
		}

		if(card.getValue() == 0){
			card.setValue(14);
		}

	}

	public Deck getGameDeck() {
		return gameDeck;
	}

	public void setGameDeck(Deck gameDeck) {
		this.gameDeck = gameDeck;
	}

	public Rules getRules() {
		return rules;
	}

	public void setRules(Rules rules) {
		this.rules = rules;
	}

	public int getTrump() {
		return trump;
	}

	public void setTrump(int trump) {
		this.trump = trump;
	}

	public Iterator<Card> getIter() {
		return iter;
	}

	public void setIter(Iterator<Card> iter) {
		this.iter = iter;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public void setShuffledDeck(ArrayList<Card> shuffledDeck) {
		this.shuffledDeck = shuffledDeck;
	}

	public int getPlayerCalledTrump() {
		return playerCalledTrump;
	}

	public void setPlayerCalledTrump(int playerCalledTrump) {
		this.playerCalledTrump = playerCalledTrump;
	}

	public int getDealer() {
		return dealer;
	}

	public void setDealer(int dealer) {
		this.dealer = dealer;
		if(this.dealer > 3){
			this.dealer = 0;
		}
		setTrickLeader(dealer + 1);
	}

	public int getTrickLeader() {
		return trickLeader;
	}

	public void setTrickLeader(int trickLeader) {
		this.trickLeader = trickLeader;
		if(this.trickLeader > 3){
			this.trickLeader = 0;
		}
	}

	public int[] getRoundScores() {
		return roundScores;
	}

	public int[] getMatchScores() {
		return matchScores;
	}

	/**
	 * clears the cards that have been played by player.
	 */
	public void clearCardsPlayed(){
		//get rid of record of what has been played
		//TODO hopefully the cardsPlayed can be used by the gameboard to show which cards have been played.
		cardsPlayed.set(0, null);
		cardsPlayed.set(1, null);
		cardsPlayed.set(2, null);
		cardsPlayed.set(3, null);
	}

	public Card getCardLead() {
		return cardLead;
	}

	public void setCardLead(Card cardLead) {
		this.cardLead = cardLead;
	}

	/* (non-Javadoc)
	 * @see com.worthwhilegames.cardgames.shared.Game#isActive()
	 */
	@Override
	public boolean isActive() {
		return gameActive;
	}

	@Override
	public int getMaxNumPlayers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPlayer(Player p) {
		players.add(p);
	}
}
