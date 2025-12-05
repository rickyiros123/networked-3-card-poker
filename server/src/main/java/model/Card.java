package model;

import java.io.Serializable;

/**
 * Card represents a single playing card with a Suit and Rank.
 *
 * Responsibilities:
 * - Hold immutable suit and rank values.
 * - Provide comparison by rank then suit (for sorting).
 * - Provide a human-readable toString and a helper to return the resource
 *   image path for this card.
 */
public class Card implements Serializable, Comparable<Card> {
	private static final long serialVersionUID = 8475503589781242492L;

	// Standard 4 suits
	public enum Suit { HEART, DIAMOND, SPADE, CLUB }
	// Ranks in natural order (ACE..KING)
	public enum Rank { 
		ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;
	}
	

	private final Suit suit; 
	private final Rank rank;
	
	/**
	 * Construct a Card with the given suit and rank.
	 *
	 * param suit the card suit (HEART/DIAMOND/SPADE/CLUB)
	 * param rank the card rank (ACE..KING)
	 */
	public Card(Suit suit, Rank rank){
		this.suit = suit;
		this.rank = rank;
	}
	
	/** Return the suit. */
	public Suit getSuit() {
		return suit;
	}
	
	/** Return the rank. */
	public Rank getRank() {
		return rank;
	}
	
	/** Return the ordinal index of the rank (0-based). */
	public int getRankIndex() {
        return rank.ordinal();
    }

	/**
	 * Compare cards primarily by rank, secondarily by suit.
	 * This allows sorting a collection of Card objects.
	 */
	@Override
	public int compareTo(Card o) {
	    int r = Integer.compare(this.rank.ordinal(), o.rank.ordinal()); // rank-first order
	    return (r != 0) ? r : Integer.compare(this.suit.ordinal(), o.suit.ordinal());
	}
	
	/** Human-readable representation */
	@Override
	public String toString() { return rank + " of " + suit; }

	/**
	 * Build and return the image resource path for this card.
	 *
	 * Stores card images under /images/cards/ with filenames of the form:
	 *   /images/cards/{suit}-{N}-256.png
	 * where {suit} is the lowercase suit name (heart, diamond, spade, club)
	 * and {N} is the numeric rank.
	 *
	 *
	 * return resource path to the card image (e.g. "/images/cards/diamond-13-256.png")
	 */
	public String getImagePath() {
		String suit = this.getSuit().name().toLowerCase();
		String rank = this.getRank().name();
		int rankNum = 0;
		if(rank == "ACE") {
			rankNum = 1;
		}
		else if(rank == "TWO") {
			rankNum = 2;
		}
		else if(rank == "THREE") {
			rankNum = 3;
		}
		else if(rank == "FOUR") {
			rankNum = 4;
		}
		else if(rank == "FIVE") {
			rankNum = 5;
		}
		else if(rank == "SIX") {
			rankNum = 6;
		}
		else if(rank == "SEVEN") {
			rankNum = 7;
		}
		else if(rank == "EIGHT") {
			rankNum = 8;
		}
		else if(rank == "NINE") {
			rankNum = 9;
		}
		else if(rank == "TEN") {
			rankNum = 10;
		}
		else if(rank == "JACK") {
			rankNum = 11;
		}
		else if(rank == "QUEEN") {
			rankNum = 12;
		}
		else if(rank == "KING") {
			rankNum = 13;
		}
		String newNum = Integer.toString(rankNum);
		
		System.out.println("/images/cards/" + suit + '-' + rank + "-256.png");
		return "/images/cards/" + suit + '-' + newNum + "-256.png";

	}
}