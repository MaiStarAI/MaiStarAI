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
        HashMap<Colors, Integer> ability1 = new HashMap<>();
        ability1.put(Colors.Red, 1);
        ability1.put(Colors.Blue, 1);
        ability1.put(Colors.Green, 1);
        two.abilities = ability1;
        two.name = GeishasName.Momiji;
        System.out.println(one.abilities);
        System.out.println(two.abilities);

        ArrayList<Card> cards = new ArrayList<>();
        Card first = new Card(CardsNames.Actor, Colors.Red, 5, 5, ability1);
        Card second = new Card(CardsNames.Ronin, Colors.Red, 5, 5, ability1);
        cards.add(first);
        cards.add(second);
        Player three = new Player("Name", cards, one);
        Player five = new Player("Name", cards, one);
        ArrayList<Card> cards1 = new ArrayList<>();
        Card first1 = new Card(CardsNames.Actor, Colors.Red, 5, 5, ability1);
        Card second1 = new Card(CardsNames.Actor, Colors.Red, 5, 5, ability1);
        cards1.add(first1);
        cards1.add(second1);
        five.hand = cards1;
        five.geisha = two;
        System.out.println(three.geisha.name);
        System.out.println(five.geisha.name);

        Action action = new Action(first, false);
        System.out.println(action.toString());




    }
}
