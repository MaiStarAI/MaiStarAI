import java.util.ArrayList;

public class State {
    ArrayList<Player> players;
    ArrayList<Card> cards;
    int drawDeck;
    int turnPlayerIndex;

    int victories;
    int visits;
    int availability;
    State parent;
    ArrayList<State> children;
    Action appliedAction;

    State(ArrayList<Player> players, ArrayList<Card> cards,
          State parent, Action appliedAction){
        this.players = players;
        this.cards = cards;
        this.drawDeck = cards.size();
        this.turnPlayerIndex = 0;
        victories = 0;
        visits = 0;
        availability = 1;
        this.parent = parent;
        children = new ArrayList<>();
        this.appliedAction = appliedAction;
    }

    public int getNextPlayer(){
        if(turnPlayerIndex == players.size() - 1){
            return 0;
        }
        else{
            return turnPlayerIndex++;
        }
    }

    public Card getRandomCard(){
        int random = 0 + (int) (Math.random() * drawDeck);
        Card randomCard = new Card(cards.get(random).name, cards.get(random).color, cards.get(random).requirement,
                cards.get(random).guestReward, cards.get(random).advReward);
        cards.remove(cards.get(random));
        return randomCard;
    }


}
