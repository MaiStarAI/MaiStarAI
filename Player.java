import java.util.ArrayList;

public class Player {
    String name;
    ArrayList<Card> hand;
    int cardsNumber;
    Geisha geisha;
    int score;
    ArrayList<Card> guests;
    ArrayList<Card> advertisers;
    ArrayList<CardsNames> specialEffects;

    Player(String name, ArrayList<Card> hand, Geisha geisha){
        this.name = name;
        this.hand = hand;
        this.geisha = geisha;
        cardsNumber = hand.size();
        score = 0;
        guests = new ArrayList<>();
        advertisers = new ArrayList<>();
        specialEffects = new ArrayList<>();
    }

    public void updateSpecialEffect(CardsNames cardName){
        switch (cardName){
            case Ronin:
                for (int i = 0; i < specialEffects.size(); i++) {
                    if(specialEffects.get(i) == CardsNames.Ronin){
                        specialEffects.remove(i);
                        //Delete from state
                    }
                }

                for (int i = 0; i < guests.size(); i++) {
                    if(guests.get(i).name == CardsNames.Ronin){
                        guests.remove(i);
                    }
                }

            case District_Kanryou:
                for (int i = 0; i < specialEffects.size(); i++) {
                    if(specialEffects.get(i) == CardsNames.District_Kanryou){
                        specialEffects.remove(i);
                        //Delete from state
                    }
                }

                for (int i = 0; i < guests.size(); i++) {
                    if(guests.get(i).name == CardsNames.District_Kanryou){
                        guests.remove(i);
                    }
                }
        }
    }

}
