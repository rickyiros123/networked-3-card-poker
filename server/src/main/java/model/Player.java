package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
	private final int id;
	private final String name;
	private int chips;
	private boolean folded;
	private List<Card> hand = new ArrayList<>();
	
	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getID () { return id; }
	public String getName() { return name; }
	public int getChips() { return chips; }
	public List<Card> getHand() { return hand; }
	
	public void adjustChips(int newChips) { this.chips += newChips; }
	public void clearHand() { hand.clear(); }
	public void addCard(Card c) { hand.add(c); }
	public void setNewHand(List<Card> newHand) { this.hand = newHand; }
	
	public boolean isFolded() { return folded; }
	public void folded() { folded = true; }
	public void resetHand() { folded = false; clearHand(); }
}
