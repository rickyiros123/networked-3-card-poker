import model.Card;
import model.ThreeCardLogic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ThreeCardLogic.
 * - Deterministic shuffle to exercise order-independence while remaining reproducible.
 * - No checks for input mutation (per request).
 */
public class MyTest {

    private static final Random RNG = new Random(42);
    private static final int BET = 10;

    // helper to make tests less noisy
    private Card card(Card.Suit s, Card.Rank r) {
        return new Card(s, r);
    }

    // build a hand from provided cards and shuffle deterministically to check order independence
    private List<Card> hand(Card... cards) {
        List<Card> h = new ArrayList<>(Arrays.asList(cards));
        Collections.shuffle(h, RNG);
        return h;
    }

    @Test
    void testThreeOfAKind() {
        List<Card> hand = hand(
                card(Card.Suit.SPADE, Card.Rank.ACE),
                card(Card.Suit.CLUB, Card.Rank.ACE),
                card(Card.Suit.HEART, Card.Rank.ACE)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.THREE_OF_A_KIND, got, "expected three of a kind");
    }

    @Test
    void testStraightFlush() {
        List<Card> hand = hand(
                card(Card.Suit.HEART, Card.Rank.TEN),
                card(Card.Suit.HEART, Card.Rank.JACK),
                card(Card.Suit.HEART, Card.Rank.QUEEN)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.STRAIGHT_FLUSH, got, "expected straight flush");
    }

    @Test
    void testPair() {
        List<Card> hand = hand(
                card(Card.Suit.SPADE, Card.Rank.KING),
                card(Card.Suit.HEART, Card.Rank.KING),
                card(Card.Suit.CLUB, Card.Rank.THREE)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.PAIR, got, "expected pair");
    }

    @Test
    void testFlush() {
        List<Card> hand = hand(
                card(Card.Suit.HEART, Card.Rank.TWO),
                card(Card.Suit.HEART, Card.Rank.SEVEN),
                card(Card.Suit.HEART, Card.Rank.NINE)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.FLUSH, got, "expected flush");
    }

    @Test
    void testStraightAceLow() {
        // A-2-3 (not all same suit) should be considered straight
        List<Card> hand = hand(
                card(Card.Suit.SPADE, Card.Rank.ACE),
                card(Card.Suit.HEART, Card.Rank.TWO),
                card(Card.Suit.CLUB, Card.Rank.THREE)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.STRAIGHT, got, "expected straight (A-2-3)");
    }

    @Test
    void testStraightAceHigh() {
        // Q-K-A (mixed suits) should be considered a straight (ace as high)
        List<Card> hand = hand(
                card(Card.Suit.SPADE, Card.Rank.QUEEN),
                card(Card.Suit.HEART, Card.Rank.KING),
                card(Card.Suit.CLUB, Card.Rank.ACE)
        );
        ThreeCardLogic.HandRank got = ThreeCardLogic.evalHand(hand);
        assertEquals(ThreeCardLogic.HandRank.STRAIGHT, got, "expected straight (Q-K-A)");
    }

    @Test
    void testEvalPPWinnings() {
        List<Card> pair = hand(
                card(Card.Suit.HEART, Card.Rank.FIVE),
                card(Card.Suit.SPADE, Card.Rank.FIVE),
                card(Card.Suit.CLUB, Card.Rank.EIGHT)
        );
        assertEquals(1 * BET, ThreeCardLogic.evalPPWinnings(pair, BET), "pair should pay 1:1");

        List<Card> flush = hand(
                card(Card.Suit.HEART, Card.Rank.TWO),
                card(Card.Suit.HEART, Card.Rank.FOUR),
                card(Card.Suit.HEART, Card.Rank.SEVEN)
        );
        assertEquals(4 * BET, ThreeCardLogic.evalPPWinnings(flush, BET), "flush should pay 4:1");

        List<Card> straight = hand(
                card(Card.Suit.HEART, Card.Rank.NINE),
                card(Card.Suit.SPADE, Card.Rank.TEN),
                card(Card.Suit.CLUB, Card.Rank.JACK)
        );
        assertEquals(6 * BET, ThreeCardLogic.evalPPWinnings(straight, BET), "straight should pay 6:1");

        List<Card> trips = hand(
                card(Card.Suit.HEART, Card.Rank.QUEEN),
                card(Card.Suit.SPADE, Card.Rank.QUEEN),
                card(Card.Suit.CLUB, Card.Rank.QUEEN)
        );
        assertEquals(30 * BET, ThreeCardLogic.evalPPWinnings(trips, BET), "three of a kind should pay 30:1");

        List<Card> sf = hand(
                card(Card.Suit.SPADE, Card.Rank.TEN),
                card(Card.Suit.SPADE, Card.Rank.JACK),
                card(Card.Suit.SPADE, Card.Rank.QUEEN)
        );
        assertEquals(40 * BET, ThreeCardLogic.evalPPWinnings(sf, BET), "straight flush should pay 40:1");
    }

    @Test
    void testCompareHands_pairBeatsHighCard() {
        List<Card> dealer = hand(
                card(Card.Suit.HEART, Card.Rank.TWO),
                card(Card.Suit.SPADE, Card.Rank.FOUR),
                card(Card.Suit.CLUB, Card.Rank.SEVEN)
        );
        List<Card> player = hand(
                card(Card.Suit.HEART, Card.Rank.NINE),
                card(Card.Suit.SPADE, Card.Rank.NINE),
                card(Card.Suit.CLUB, Card.Rank.TWO)
        );
        int result = ThreeCardLogic.compareHands(dealer, player);
        assertTrue(result > 0, "positive result expected when player has the stronger hand (pair beats high card)");
    }

    @Test
    void testCompareHands_highCardKicker() {
        List<Card> dealer = hand(
                card(Card.Suit.HEART, Card.Rank.KING),
                card(Card.Suit.SPADE, Card.Rank.EIGHT),
                card(Card.Suit.CLUB, Card.Rank.FIVE)
        );
        List<Card> player = hand(
                card(Card.Suit.HEART, Card.Rank.KING),
                card(Card.Suit.SPADE, Card.Rank.NINE), // higher kicker -> player wins
                card(Card.Suit.CLUB, Card.Rank.FOUR)
        );
        int result = ThreeCardLogic.compareHands(dealer, player);
        assertTrue(result > 0, "player should win because of higher kicker");
    }

    @Test
    void testCompareHands_straightTiebreak() {
        List<Card> dealer = hand(
                card(Card.Suit.HEART, Card.Rank.FOUR),
                card(Card.Suit.SPADE, Card.Rank.FIVE),
                card(Card.Suit.CLUB, Card.Rank.SIX)
        );
        List<Card> player = hand(
                card(Card.Suit.HEART, Card.Rank.FIVE),
                card(Card.Suit.SPADE, Card.Rank.SIX),
                card(Card.Suit.CLUB, Card.Rank.SEVEN)
        );
        int result = ThreeCardLogic.compareHands(dealer, player);
        assertTrue(result > 0, "player's straight (5-6-7) should beat dealer's straight (4-5-6)");
    }

    @Test
    void testInvalidHandSizeThrows() {
        List<Card> tooFew = hand(
                card(Card.Suit.HEART, Card.Rank.ACE),
                card(Card.Suit.SPADE, Card.Rank.TWO)
        );
        assertThrows(IllegalArgumentException.class, () -> ThreeCardLogic.evalHand(tooFew),
                "evalHand should throw when hand size != 3");
    }
}