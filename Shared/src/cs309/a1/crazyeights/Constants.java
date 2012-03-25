package cs309.a1.crazyeights;

public class Constants {
	
	public static final int NUMBER_OF_CARDS_PER_HAND = 5;
	
	public static final int SUIT_CLUBS = 0;
	public static final int SUIT_DIAMONDS = 1;
	public static final int SUIT_HEARTS = 2;
	public static final int SUIT_SPADES = 3;
	public static final int SUIT_JOKER = 4;
	
	public static final String SUIT = "suit";
	public static final String VALUE = "value";
	public static final String RESOURCE_ID = "resourceid";
	public static final String ID = "id";
	public static final String MESSAGE_TYPE = "messagetype";
	
	//these are bluetooth message types that can be sent by the GameBoard
	public static final int SETUP = 0;
	public static final int IS_TURN = 1;
	public static final int WINNER = 2;
	public static final int LOSER = 3;
	
	//these are bluetooth messages types that can be sent by Player
	public static final int PLAY_CARD = 4;
	public static final int DRAW_CARD = 5;
	public static final int PLAY_EIGHT = 6;
	
	

}
