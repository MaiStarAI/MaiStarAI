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
    ArrayList<State> children;
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
        this.drawDeck = cards.size();
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

        drawDeck = cards.size();
    }

    /**
     * Constructor to create copies of states for AIAlgorithm
     * @param anotherState: state which new object need to create
     */

    State(State anotherState){
        this.players = new ArrayList<>();
        for (Player p : anotherState.players) this.players.add(new Player(p));
        this.cards = new ArrayList<>(anotherState.cards);
        this.drawDeck = anotherState.drawDeck;
        this.turnPlayerIndex = anotherState.turnPlayerIndex;
        victories = anotherState.victories;
        visits = anotherState.visits;
        availability = anotherState.availability;
        this.parent = anotherState.parent;
        if (anotherState.children != null)
            children = new ArrayList<>(anotherState.children);
        else
            children = null;
        this.appliedAction = anotherState.appliedAction;
        this.AIPlayer = anotherState.AIPlayer;
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
            return turnPlayerIndex + 1;
        }
    }

    /**
     * Method to get random card from cards which in game
     * @return object of class Card
     */

    public Card getRandomCard(){
        Card removed = this.cards.remove(0);
        drawDeck -= 1;
        return removed;
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
        for (int i = 0; i < this.cards.size(); i++) {
            int random = (int) (Math.random() * drawDeck);
            if(!cards.get(i).known && !cards.get(random).known){
                Card oneCard = cards.get(i);
                cards.set(i, cards.get(random));
                cards.set(random, oneCard);
            }
        }

        for (int i = 0; i < this.players.size(); i++) {
            if(i != this.AIPlayer){
                for (int j = 0; j < this.players.get(i).hand.size(); j++) {
                    if(!this.players.get(i).hand.get(j).known) {
                        drawDeck += 1;
                        this.cards.add(this.players.get(i).hand.get(j));
                        this.players.get(i).hand.set(j, this.getRandomCard());
                    }
                }
            }
        }
    }

    /**
     * Method to get main information about state
     * @return string with main information
     */

    @Override
    public String toString() {
        StringBuilder info = new StringBuilder();
        for (Player iPlayer : players) {
            info.append(iPlayer.name).append("\n").append("Hand: \n");

            for (int j = 0; j < iPlayer.hand.size(); j++) {
                info.append(iPlayer.hand.get(j).name).append(" ").append(iPlayer.hand.get(j).color).append(", ");
            }

            info.append("\n Cards number: ").append(iPlayer.cardsNumber).append("\n Geisha: ")
                    .append(iPlayer.geisha.name).append(" ").append(iPlayer.geisha.abilities.get(Colors.Red))
                    .append(" ").append(iPlayer.geisha.abilities.get(Colors.Blue)).append(" ").
                    append(iPlayer.geisha.abilities.get(Colors.Green)).append("\n Score: ").
                    append(iPlayer.score).append("\n Guests: ");

            for (int j = 0; j < iPlayer.guests.size(); j++) {
                info.append(iPlayer.guests.get(j).name).append(" ").append(iPlayer.guests.get(j).color).append(", ");
            }

            info.append("\n Advertisers: ");

            for (int j = 0; j < iPlayer.advertisers.size(); j++) {
                info.append(iPlayer.advertisers.get(j).name).append(" ").append(iPlayer.advertisers.get(j).color)
                        .append(", ");
            }

            info.append("\n Special effects: ");

            for (int j = 0; j < iPlayer.specialEffects.size(); j++) {
                info.append(iPlayer.specialEffects.get(j)).append(" ");
            }

            info.append("\n Number possible geisha's effect: \n").append(iPlayer.geishaEffect).append("\n");
        }

        info.append("Draw deck: \n");

        for (Card card : cards) {
            info.append(card.name).append(" ").append(card.color).append(", ");
        }

        info.append("\n Number of cards in draw deck: ").append(drawDeck).append("\n Turn player: ").
                append(players.get(turnPlayerIndex).name).append("\n AI player: ").append(players.get(AIPlayer).name);

        return info.toString();
    }

}
