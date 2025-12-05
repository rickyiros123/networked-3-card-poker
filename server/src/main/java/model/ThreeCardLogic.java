package model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import model.Card.Rank;

public class ThreeCardLogic {

    public enum HandRank {
        HIGH_CARD(0),
        PAIR(1),
        FLUSH(2),
        STRAIGHT(3),
        THREE_OF_A_KIND(4),
        STRAIGHT_FLUSH(5);

        private final int strength;
        HandRank(int strength) { this.strength = strength; }
        public int strength() { return strength; }
        public boolean beats(HandRank other) { return this.strength > other.strength; }
    }

    // Include HIGH_CARD -> 0 so lookups never return null for any HandRank
    private static final Map<HandRank, Integer> PAIR_PLUS_PAYOUT = Map.of(
            HandRank.HIGH_CARD, 0,
            HandRank.PAIR, 1,
            HandRank.FLUSH, 4,
            HandRank.STRAIGHT, 6,
            HandRank.THREE_OF_A_KIND, 30,
            HandRank.STRAIGHT_FLUSH, 40
    );

    private static final Map<Rank, Integer> RANK_VALUES = new EnumMap<>(Rank.class);
    static {
        RANK_VALUES.put(Rank.ACE, 14);
        RANK_VALUES.put(Rank.TWO, 2);
        RANK_VALUES.put(Rank.THREE, 3);
        RANK_VALUES.put(Rank.FOUR, 4);
        RANK_VALUES.put(Rank.FIVE, 5);
        RANK_VALUES.put(Rank.SIX, 6);
        RANK_VALUES.put(Rank.SEVEN, 7);
        RANK_VALUES.put(Rank.EIGHT, 8);
        RANK_VALUES.put(Rank.NINE, 9);
        RANK_VALUES.put(Rank.TEN, 10);
        RANK_VALUES.put(Rank.JACK, 11);
        RANK_VALUES.put(Rank.QUEEN, 12);
        RANK_VALUES.put(Rank.KING, 13);
    }

    public static HandRank evalHand(List<Card> hand) {
        if (hand == null || hand.size() != 3) {
            throw new IllegalArgumentException("Hand must contain exactly 3 cards");
        }

        boolean isFlush = isFlush(hand);
        boolean isStraight = isStraight(hand);
        int distinctRanks = (int) hand.stream()
                .map(Card::getRank)
                .distinct()
                .count();


        if(isStraight && isFlush) return HandRank.STRAIGHT_FLUSH;
        if(distinctRanks == 1) return HandRank.THREE_OF_A_KIND;
        if(isStraight) return HandRank.STRAIGHT;
        if(isFlush) return HandRank.FLUSH;
        if(distinctRanks == 2) return HandRank.PAIR;
        return HandRank.HIGH_CARD;
    }

    public static int compareHands(List<Card> dealer, List<Card> player) {
        // return -1 if dealer wins, 0 push/tie, +1 if player wins
        HandRank playerHand = evalHand(player);
        HandRank dealerHand = evalHand(dealer);
        int cmp = Integer.compare(playerHand.strength(), dealerHand.strength());

        if(cmp < 0) {
            return -1;
        } else if (cmp > 0) {
            return +1;
        }

        Collections.sort(player);
        Collections.sort(dealer);

        int playerHigh = RANK_VALUES.get(player.get(2).getRank());
        int dealerHigh = RANK_VALUES.get(dealer.get(2).getRank());

        if(playerHigh < dealerHigh) return -1;
        if(playerHigh > dealerHigh) return +1;


        int playerKicker = RANK_VALUES.get(player.get(1).getRank());
        int dealerKicker = RANK_VALUES.get(dealer.get(1).getRank());

        if(playerKicker < dealerKicker) return -1;
        if(playerKicker > dealerKicker) return +1;


        return 0;
    }

    public static int evalPPWinnings(List<Card> hand, int pairPlus) {
        HandRank playerHand = evalHand(hand);
        // defensive lookup: use default 0 if mapping missing
        int playerMultiplier = PAIR_PLUS_PAYOUT.getOrDefault(playerHand, 0);
        return playerMultiplier * pairPlus;
    }

    public static boolean isFlush(List<Card> hand) {
        Card.Suit suit = hand.get(0).getSuit();
        for (Card c : hand) {
            if (c == null || c.getSuit() != suit) return false;
        }
        return true;
    }

    public static boolean isStraight(List<Card> hand) {
        Collections.sort(hand);

        if(hand.get(1).getRank() == Rank.QUEEN && hand.get(2).getRank() == Rank.KING && hand.get(0).getRank() == Rank.ACE) return true;

        for(int i = 0; i < 2; i++) {
            int card = hand.get(i).getRankIndex();
            int nextCard = hand.get(i+1).getRankIndex();
            if(card != nextCard - 1) return false;
        }

        return true;
    }

    public static boolean isThreePair(List<Card> hand) {
        return hand.get(0).getSuit() == hand.get(1).getSuit() ||
               hand.get(0).getSuit() == hand.get(2).getSuit() ||
               hand.get(1).getSuit() == hand.get(2).getSuit();
    }

    public static boolean isPair(List<Card> hand) {
        return hand.get(0).getRank() == hand.get(1).getRank() ||
               hand.get(0).getRank() == hand.get(2).getRank() ||
               hand.get(1).getRank() == hand.get(2).getRank();
    }
}