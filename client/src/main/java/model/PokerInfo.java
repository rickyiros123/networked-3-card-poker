package model;

import java.io.Serializable;
import java.util.List;

public class PokerInfo implements Serializable{
	private static final long serialVersionUID = -5618077535578984935L;
	public enum Type {
        CHAT,          // simple chat/text/status
        GAME_DEAL,     // player cards + dealer cards
        GAME_RESULT,   // win/lose, payouts
        FOLD,
        WELCOME,
        START,
        PLAY,
        LOG
    }
	
	private final Type type;
	private final String message;
	private final List<Card> playerHand;
	private final List<Card> dealerHand;
	private final int ante;
	private final int pairPlus;
	private List<String> log;
	
	
	
	public PokerInfo(Type type, List<Card> playerHand, List<Card> dealerHand, int ante, int pairPlus, String message) {
		this.type = type;
		this.message = message;
		this.playerHand = playerHand;
		this.dealerHand = dealerHand;
		this.ante = ante;
		this.pairPlus = pairPlus;
		this.log = null;

	}
	public PokerInfo(Type type, List<Card> playerHand, List<Card> dealerHand, int ante, int pairPlus) {
		this.type = type;
		this.playerHand = playerHand;
		this.dealerHand = dealerHand;
		this.ante = ante;
		this.pairPlus = pairPlus;
		this.message = "";
		this.log = null;
	}
	
	public PokerInfo(Type type, List<Card> playerHand, List<Card> dealerHand, int ante, int pairPlus, List<String> log) {
		this.type = type;
		this.playerHand = playerHand;
		this.dealerHand = dealerHand;
		this.ante = ante;
		this.pairPlus = pairPlus;
		this.message = "";
		this.log = log;
	}
	
	public PokerInfo(Type type, List<Card> playerHand, List<Card> dealerHand) {
		this.type = type;
		this.message = null;
		this.playerHand = playerHand;
		this.dealerHand = dealerHand;
		this.ante = 0;
		this.pairPlus = 0;
		this.log = null;

	}
	
	public PokerInfo(Type type, String message) {
		this.type = type;
		this.message = message;
		this.playerHand = null;
		this.dealerHand = null;
		this.ante = 0;
		this.pairPlus = 0;
		this.log = null;

	}
	
	public PokerInfo(Type type, int chips) {
		this.type = type;
		this.message = null;
		this.playerHand = null;
		this.dealerHand = null;
		this.ante = 0;
		this.pairPlus = 0;
		this.log = null;

	}
	
	public PokerInfo(Type type, List<String> log) {
		this.type = type;
		this.message = null;
		this.playerHand = null;
		this.dealerHand = null;
		this.ante = 0;
		this.pairPlus = 0;
		this.log = log;
	}
	public void printPlayerCards() {
		System.out.println(this.playerHand.get(0)); 
		System.out.println(this.playerHand.get(1));
		System.out.println(this.playerHand.get(2));
	}
	
	public Type getType() { return type; }
	public String getMessage() { return message; }
	public List<Card> getDealerHand() { return dealerHand; }
	public List<Card> getPlayerHand() { return playerHand; }
	public int getAnte() { return ante; }
	public int getPairPlus() { return pairPlus; }
	public List<String> getLog() { return log; }
	public void setLog(List<String> log) { this.log = log; }
}
