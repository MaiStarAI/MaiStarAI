import java.util.ArrayList;

class Player {

    enum Type {
        Human, ISMCTS, Random
    }

    private String name;
    private Type type;
    private Geisha geisha;
    private int geishaUsages;

    private Reputation akenohoshi_bonus;

    private ArrayList<Card> hand;
    private ArrayList<Card> a_line;
    private ArrayList<Card> g_line;

    /** Constructor for creation of the new player */
    Player (Type type, String name, Geisha geisha) {
        this.type = type;
        this.name = name;
        this.geisha = geisha;
        geishaUsages = 0;
        hand = new ArrayList<>();
        a_line = new ArrayList<>();
        g_line = new ArrayList<>();
    }

    /** Copy constructor */
    Player (Player another) {
        this(another.getType(), another.getName(), another.getGeisha());
        setGeishaUsages(another.getGeishaUsages());
        for (Card c : another.getHand()) addCard(c); // New instances
        for (Card c : another.getGuests()) g_line.add(new Card(c));
        for (Card c : another.getAdverts()) a_line.add(new Card(c));

    }

    /** Adds new card instance to the hand */
    void addCard (Card card) {
        hand.add(new Card(card));
    }
    void addAdv (Card card) { a_line.add(new Card(card)); }
    void addGuest (Card card) { g_line.add(new Card(card)); }

    /** Discards one of the cards of given type and color */
    void discardCard (Card card) {
        hand.removeIf(c -> c.equals(card));
    }
    void discardAdv (Card card) { a_line.removeIf(c -> c.equals(card)); }
    void discardGuest (Card card) { g_line.removeIf(c -> c.equals(card)); }

    /** Discards the i'th card */
    void discardCard (int i) {
        hand.remove(i);
    }
    void discardAdv (int i) { a_line.remove(i); }
    void discardGuest (int i) { g_line.remove(i); }

    /** Returns the score based on current guests */
    int getScore () {
        int score = 0;
        for (Card c : getGuests())
            score += c.getReward();
        score -= 2 * getHand().size();
        return score  < 0 ? 0 : score;
    }
    /** Returns overall reputation */
    Reputation getReputation () {
        int red = 0, blue = 0, green = 0, black = 0;
        red += geisha.getRed();
        blue += geisha.getBlue();
        green += geisha.getGreen();

        for (Card c : getAdverts()) {
            red += c.getBonus().getRed();
            blue += c.getBonus().getBlue();
            green += c.getBonus().getGreen();
        }

        black = Math.max(Math.max(red, blue), green);

        return new Reputation(red, blue, green, black);
    }

    /** Geisha usages */
    int getGeishaUsages() {
        return geishaUsages;
    }
    void setGeishaUsages(int geishaUsages) {
        this.geishaUsages = geishaUsages;
    }
    void increaseGeishaUsages () { geishaUsages++; }

    /** Getters and setters */
    Type getType () { return type; }
    String getName () { return name; }
    Geisha getGeisha () { return geisha; }

    ArrayList<Card> getHand () { return hand; }
    ArrayList<Card> getGuests () { return g_line; }
    ArrayList<Card> getAdverts () { return a_line; }

    Reputation getAkenohoshiBonus () { return akenohoshi_bonus; }
    void setAkenohoshiBonus (Reputation rep) { akenohoshi_bonus = rep; }

    public String toString () {
        StringBuilder hand = new StringBuilder();
        StringBuilder advs = new StringBuilder();
        StringBuilder guests = new StringBuilder();

        for (Card c : getHand()) hand.append("\t\t").append(c.toString()).append("\r\n");
        for (Card c : getAdverts()) advs.append("\t\t").append(c.toString()).append("\r\n");
        for (Card c : getGuests()) guests.append("\t\t").append(c.toString()).append("\r\n");

        return getName() + "(" + getType().toString() + "):\r\n" +
                "\tGeisha: " + getGeisha().getName().toString() + "\r\n" +
                "\tHand:\r\n" + hand.toString() +
                "\tAdvertisers:\r\n" + advs.toString() +
                "\tGuests:\r\n" + guests.toString() +
                "\tScore: " + getScore();
    }
}
