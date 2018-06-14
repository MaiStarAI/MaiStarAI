import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Colors, Integer> ability = new HashMap<>();
        ability.put(Colors.Red, 5);
        ability.put(Colors.Blue, 5);
        ability.put(Colors.Green, 5);
        Geisha one = new Geisha(GeishasName.Oboro, ability, 0);
        Geisha two = new Geisha(one);
        two.name = GeishasName.Harukaze;
        two.numberEffect = 1;
        HashMap<Colors, Integer> ability2 = new HashMap<>();
        ability2.put(Colors.Red, 2);
        ability2.put(Colors.Blue, 2);
        ability2.put(Colors.Green, 2);
        two.abilities = ability2;

        HashMap<Colors, Integer> ability1 = new HashMap<>();
        ability1.put(Colors.Red, 1);
        ability1.put(Colors.Blue, 1);
        ability1.put(Colors.Green, 1);

        ArrayList<Card> cards = new ArrayList<>();
        Card first = new Card(CardsNames.Actor, Colors.Red, 5, 5, ability1);
        Card second = new Card(CardsNames.Ronin, Colors.Red, 2, 2, ability1);
        cards.add(first);
        cards.add(second);

        ArrayList<Card> cards1 = new ArrayList<>();
        Card first1 = new Card(CardsNames.Actor, Colors.Green, 5, 5, ability1);
        Card second1 = new Card(CardsNames.Actor, Colors.Blue, 5, 5, ability1);
        cards1.add(first1);
        cards1.add(second1);

        Player three = new Player("three", cards, one);
        Player five = new Player("five", cards1, two);

        ArrayList<Player> players = new ArrayList<>();
        players.add(three);
        players.add(five);
        ArrayList<Card> stateCards = new ArrayList<>();
        stateCards.add(first);
        stateCards.add(second);
        stateCards.add(first1);
        stateCards.add(second1);


        State state = new State(players, stateCards, 1);
        System.out.println(state.toString());
        Action action1 = new Action(state.players.get(state.turnPlayerIndex).hand.get(0), true);
        if(action1.isApplicableAction(state)) {
            state = action1.applyAction(state);
            System.out.println(state.toString());
        }

        Action action2 = new Action(state.players.get(state.turnPlayerIndex).hand.get(0), true);
        if(action2.isApplicableAction(state)){
            state = action2.applyAction(state);
            System.out.println(state.toString());
        }
        else{
            action2 = new Action(state.players.get(state.turnPlayerIndex).hand.get(0));
            if(action2.isApplicableAction(state)) {
                state = action2.applyAction(state);
                System.out.println(state.toString());
            }
        }
    }
}
