import java.util.ArrayList;

/**
 * Class player contains information about player
 *
 * name: name of player
 * type: type of player
 * hand: cards in player's hand
 * cardsNumber: number of cards in hand
 * geisha: object of class Geisha which contains information about geisha of player
 * score: sum of guest rewards of all cards which played as a guest
 * guests: array of cards which played as guests
 * advertiser: array of cards which played as advertisers
 * specialEffects: array with names of cards which have special effect (Ronin, District Kanryou)
 * geishaEffect: how much times player can play effect of his geisha
 */

public class Player {
    String name;
    PlayerType type;
    ArrayList<Card> hand;
    int cardsNumber;
    Geisha geisha;
    int score;
    ArrayList<Card> guests;
    ArrayList<Card> advertisers;
    ArrayList<CardsNames> specialEffects;
    int geishaEffect;

    /**
     * Main constructor for class Player
     * @param name: name of player
     * @param hand: array with cards which is in player's hand
     * @param geisha: object of class Geisha which contains information about geisha of player
     */

    Player(String name, ArrayList<Card> hand, Geisha geisha) {
        this.name = name;
        this.hand = hand;
        this.geisha = geisha;
        cardsNumber = hand.size();
        score = 0;
        guests = new ArrayList<>();
        advertisers = new ArrayList<>();
        specialEffects = new ArrayList<>();
        this.geishaEffect = geisha.numberEffect;

        for (Card aHand : this.hand) {
            if (aHand.name == CardsNames.District_Kanryou) {
                this.specialEffects.add(CardsNames.District_Kanryou);
            }
        }
    }

    /**
     * Constructor to create copies of players
     * @param anotherPlayer: player which new object need to create
     */

    Player(Player anotherPlayer) {
        this.name = anotherPlayer.name;
        this.hand = new ArrayList<>(anotherPlayer.hand);
        this.geisha = new Geisha(anotherPlayer.geisha);
        cardsNumber = anotherPlayer.cardsNumber;
        score = anotherPlayer.score;
        guests = new ArrayList<>(anotherPlayer.guests);
        advertisers = new ArrayList<>(anotherPlayer.advertisers);
        specialEffects = new ArrayList<>(anotherPlayer.specialEffects);
        this.geishaEffect = anotherPlayer.geishaEffect;
    }

    /**
     * Method to set type
     * @param type: new type of player
     */

    public void setType(PlayerType type){
        this.type = type;
    }

    /**
     * Update array with special effects and player's hand in case application of such effect
     * @param cardName: card which effect was applied
     */

    public void updateSpecialEffect(CardsNames cardName) {
        switch (cardName) {
            case Ronin:
                for (int i = 0; i < specialEffects.size(); i++) {
                    if (specialEffects.get(i) == CardsNames.Ronin) {
                        specialEffects.remove(i);
                    }
                }

                for (int i = 0; i < guests.size(); i++) {
                    if (guests.get(i).name == CardsNames.Ronin) {
                        guests.remove(i);
                    }
                }
                break;

            case District_Kanryou:
                for (int i = 0; i < specialEffects.size(); i++) {
                    if (specialEffects.get(i) == CardsNames.District_Kanryou) {
                        specialEffects.remove(i);
                    }
                }

                for (int i = 0; i < guests.size(); i++) {
                    if (guests.get(i).name == CardsNames.District_Kanryou) {
                        guests.remove(i);
                    }
                }
                default:
                    System.out.println("Error: there is no such special effect");
        }
    }

}
