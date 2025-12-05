package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
	private final List<Card> cards = new ArrayList<>();
	
	public Deck() {
		reset();
	}
	
	public final void reset() {
		cards.clear();
		for(Card.Suit s : Card.Suit.values()) {
			for(Card.Rank r : Card.Rank.values()) {
				Card card = new Card(s, r);
				cards.add(card);
			}
		}
	}
	
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	public final List<Card> deal(int handSize){
		List<Card> hand = new ArrayList<>();
		for(int i = 0; i < handSize && !cards.isEmpty(); i++){
			hand.add(cards.remove(cards.size() - 1));
		}
		return hand;
	}
	
	
}
