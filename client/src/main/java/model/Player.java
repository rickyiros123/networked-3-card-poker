package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Player represents a single client in the poker game.
 *
 * Responsibilities:
 * - Hold player identity (id, name) and mutable state (chips, fold status, hand).
 * - Provide simple helpers to modify chips, manage the hand, and query status.
 *
 */
public class Player {
	private final int id;
	private final String name;
	private int chips;
	private boolean folded;
	private List<Card> hand = new ArrayList<>();
	
	/**
	 * Construct a Player with an id and display name.
	 *
	 * param id   numeric player id (unique within a game/session)
	 * param name display name for UI/logging
	 */
	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/** Return the player's id. */
	public int getID () { return id; }
	
	/** Return the player's display name. */
	public String getName() { return name; }
	
	/** Return the player's current chip count. */
	public int getChips() { return chips; }
	
	/** Return the player's current hand (mutable list). */
	public List<Card> getHand() { return hand; }
	
	/**
	 * Adjust the player's chips by adding newChips.
	 * Note: this method adds the value to the existing chips (can be negative).
	 *
	 * param newChips amount to add (use negative to subtract)
	 */
	public void adjustChips(int newChips) { this.chips += newChips; }
	
	/** Clear the player's hand (remove all cards). */
	public void clearHand() { hand.clear(); }
	
	/** Add a single card to the player's hand. */
	public void addCard(Card c) { hand.add(c); }
	
	/** Replace the player's hand with a new list reference (shallow copy not performed here). */
	public void setNewHand(List<Card> newHand) { this.hand = newHand; }
	
	/** Return true if the player has folded this round. */
	public boolean isFolded() { return folded; }
	
	/** Mark the player as folded (no parameters). */
	public void folded() { folded = true; }
	
	/** Reset the player's folded state and clear the hand for a new round. */
	public void resetHand() { folded = false; clearHand(); }
}