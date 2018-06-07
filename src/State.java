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

        for (int i = 0; i < this.players.size(); i++) {
            if(this.players.get(i).geisha.name == GeishasName.Oboro){
                Geisha.OboroEffect(this,this.players.get(i));
                break;
            }
        }
    }

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

        for (int i = 0; i < this.players.size(); i++) {
            if((this.players.get(i).geisha.name!=GeishasName.Akenohoshi)&&
                    (this.players.get(i).geisha.name!=GeishasName.Oboro)){
                this.players.get(i).geishaEffect = this.players.get(i).geisha.numberEffect;
            }
        }
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
        int random = (int) (Math.random() * drawDeck);
        Card randomCard = new Card(cards.get(random).name, cards.get(random).color, cards.get(random).requirement,
                cards.get(random).guestReward, cards.get(random).advReward);
        cards.remove(cards.get(random));
        return randomCard;
    }


}
