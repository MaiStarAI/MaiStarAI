import java.util.ArrayList;

/**
 * Class state contains information about game state
 *
 * players: players in current game
 * cards: cards which are in game (not cards which are in players' hands nor played cards)
 * drawDeck: number of cards in draw deck
 * turnPlayerIndex: index in ArrayList<Player> players of player which turn
 * AIPlayer: index in ArrayList<Player> players of player which is AIPlayer
 *
 * victories: number of victories for current state
 * visits: number of visits for current state
 * availability: number of available actions
 * parent: parent of current state
 * children: arrays of children for each determinization
 * appliedAction: action which was applied to get this state
 */

public class State {
    ArrayList<Player> players;
    ArrayList<Card> cards;
    int drawDeck;
    int turnPlayerIndex;
    int AIPlayer;

    int victories;
    int visits;
    int availability;
    State parent;
    ArrayList<ArrayList<State>> children;
    Action appliedAction;

    /**
     * Main constructor which is applicable only to start game
     * @param players: players of current game
     * @param cards: cards which are in game (not cards which are in players' hands nor played cards)
     * @param AIPlayer: index in ArrayList<Player> players of player which is AIPlayer
     */

    State(ArrayList<Player> players, ArrayList<Card> cards, int AIPlayer){
        this.players = players;
        this.cards = cards;
        this.turnPlayerIndex = 0;
        victories = 0;
        visits = 0;
        availability = 1;
        this.parent = null;
        children = new ArrayList<>();
        this.appliedAction = null;
        this.AIPlayer = AIPlayer;

        for (Player player : this.players) {
            if (player.geisha.name == GeishasName.Oboro) {
                Geisha.OboroEffect(this, player);
                break;
            }
        }
    }

    /**
     * Constructor to create copies of states for AIAlgorithm
     * @param anotherState: state which new object need to create
     */

    State(State anotherState){
        this.players = anotherState.players;
        this.cards = anotherState.cards;
        this.drawDeck = anotherState.drawDeck;
        this.turnPlayerIndex = anotherState.turnPlayerIndex;
        victories = anotherState.victories;
        visits = anotherState.visits;
        availability = anotherState.availability;
        this.parent = anotherState.parent;
        children = anotherState.children;
        this.appliedAction = anotherState.appliedAction;
        this.AIPlayer = anotherState.AIPlayer;

        for (Player player : this.players) {
            if ((player.geisha.name != GeishasName.Akenohoshi) &&
                    (player.geisha.name != GeishasName.Oboro)) {
                player.geishaEffect = player.geisha.numberEffect;
            }
        }
    }

    /**
     * Method to generate new turnPlayer of a game
     * @return index of new turnPlayer
     */

    public int getNextPlayer(){
        if(turnPlayerIndex == players.size() - 1){
            return 0;
        }
        else{
            return turnPlayerIndex++;
        }
    }

    /**
     * Method to get random card from cards which in game
     * @return object of class Card
     */

    public Card getRandomCard(){
        int random = (int) (Math.random() * drawDeck);
        Card randomCard = new Card(cards.get(random).name, cards.get(random).color, cards.get(random).requirement,
                cards.get(random).guestReward, cards.get(random).advReward);
        cards.remove(cards.get(random));
        drawDeck -= 1;
        return randomCard;
    }

    /**
     * Method to check state is terminal or not
     * @return true if state is terminal
     */

    public boolean isTerminal(){
        for (Player player : this.players) {
            if (player.hand.isEmpty()) {
                return true;
            }
        }

        return this.drawDeck == 0;
    }

    /**
     * Method to check state is victory for AIPlayer or not
     * @return true if it is  victory for AIPlayer
     */

    public boolean isVictory(){
        int max = 0;
        int maxInd = -1;
        for (int i = 0; i < this.players.size(); i++) {
            if(this.players.get(i).score > max){
                max = this.players.get(i).score;
                maxInd = i;
            }
        }

        return maxInd == this.AIPlayer;
    }

    /**
     * Method to get new random determinization for current state
     *
     */

    public void getDeterminization(){
        for (int i = 0; i < this.players.size(); i++) {
            if(i != this.AIPlayer){
                for (int j = 0; j < this.players.get(i).hand.size(); j++) {
                    this.players.get(i).hand.add(this.getRandomCard());
                }
            }
        }

        this.children.add(new ArrayList<>());
    }

}
