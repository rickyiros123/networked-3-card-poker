package model;

import java.util.List;

public class PokerEngine {
	private final Player dealer;
	private final Player client;
	private int ante;
	private int pairPlus;
    private final Deck deck;
	
	public PokerEngine(Player client, int startingChips) {
		this.client = client;
		this.dealer = new Player(1001, "Delaer");
		this.dealer.adjustChips(Integer.MAX_VALUE);
		this.deck = new Deck();
		this.client.adjustChips(startingChips);
	}
	
	public void startSewHand() {
		ante = 0;
		pairPlus = 0;
		client.resetHand();
		dealer.resetHand();
		deck.shuffle();
		dealInitialCards();
//		listener.onHandStarted(getStateSnasho());
	}
	
	public void dealInitialCards() {
		List<Card> newClientHand = deck.deal(3);
		client.setNewHand(newClientHand);
		List<Card> newDealerHand = deck.deal(3);
		dealer.setNewHand(newDealerHand);
	}
	
	public void evaluateHands(int pairPlus, int anteWager) {
		int result = ThreeCardLogic.compareHands(dealer.getHand(), client.getHand());
		if(result == +1) {
			setAnte(anteWager * 2);
		} else if (result == -1 ) {
			setAnte(0);
		} else {
			setAnte(anteWager);
		}
		
		if(pairPlus > 0 && result == +1 ) {
			int pairPlusResult = ThreeCardLogic.evalPPWinnings(client.getHand(), pairPlus);
			setPairPlus(pairPlusResult);
		} else {
			setPairPlus(0);
		}
	}
	
	public void setAnte(int anteWager) { this.ante = anteWager; }
	public void setPairPlus(int pairPlusWager)  {this.pairPlus = pairPlusWager; }
	public int getPairPlus() { return this.pairPlus; }
	public int getAnte() { return this.ante; }
	
	public Player getClient() { return client; }
	public Player getDealer() { return dealer; }
	}

