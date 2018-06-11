import java.util.ArrayList;

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

    State(ArrayList<Player> players, ArrayList<Card> cards,
          State parent, Action appliedAction, int AIPlayer){
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
        this.AIPlayer = AIPlayer;

        for (Player player : this.players) {
            if (player.geisha.name == GeishasName.Oboro) {
                Geisha.OboroEffect(this, player);
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
        this.AIPlayer = anotherState.AIPlayer;

        for (Player player : this.players) {
            if ((player.geisha.name != GeishasName.Akenohoshi) &&
                    (player.geisha.name != GeishasName.Oboro)) {
                player.geishaEffect = player.geisha.numberEffect;
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
        drawDeck -= 1;
        return randomCard;
    }

    public boolean isTerminal(){
        for (Player player : this.players) {
            if (player.hand.isEmpty()) {
                return true;
            }
        }

        return this.drawDeck == 0;
    }

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

    public void getDeterminization(){
        for (int i = 0; i < this.players.size(); i++) {
            if(i != this.AIPlayer){
                for (int j = 0; j < this.players.get(i).hand.size(); j++) {
                    this.players.get(i).hand.add(this.getRandomCard());
                }
            }
        }
    }

}
