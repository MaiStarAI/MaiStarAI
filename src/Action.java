class Action {

    enum Name {
        Guest,
        GuestEffect,
        Advertiser,
        Exchange,
        Introduce,
        Search,
        Geisha,
        CancelEffectRonin,
        CancelEffectDistrict,
        AllowEffect,
        HarukazeDiscard,
        EndTurn
    }

    private Name name;
    private Player player; // Who performed this action
    private Player targetPlayer; // In case of guest effect
    private Card card1; // Guest, Advertiser, Guest effect, Geisha
    private Card card2; // Guest effect, Introduce, Exchange, Geisha
    private Reputation rep;
    private int exchange_ind_1;
    private int exchange_ind_2;

    Action(Name name, Player player, Card card1, Card card2, Player targetPlayer, Reputation rep) {
        this.name = name;
        this.player = player;
        this.card1 = card1;
        this.card2 = card2;
        this.targetPlayer = targetPlayer;
        this.rep = rep;
        exchange_ind_1 = -1;
        exchange_ind_2 = -1;
    }

    Name getName () { return name; }
    Player getPlayer () { return player; }
    Player getTargetPlayer () { return targetPlayer; }
    Card getCard1 () { return card1; }
    Card getCard2 () { return card2; }
    Reputation getRep () {
        return rep;
    }
    int get_exchange_ind_1 () { return exchange_ind_1; }
    void set_exchange_ind_1 (int exchange_ind_1) { this.exchange_ind_1 = exchange_ind_1; }
    int get_exchange_ind_2 () { return exchange_ind_2; }
    void set_exchange_ind_2 (int exchange_ind_2) { this.exchange_ind_2 = exchange_ind_2; }

    boolean equals (Action another) {
        return another != null && getName() ==  another.getName() &&
                ((card1 == null || another.card1 == null) ? card1 == another.card1 : card1.equals(another.card1)) &&
                ((card2 == null || another.card2 == null) ? card2 == another.card2 : card2.equals(another.card2)) &&
                player.getName().equals(another.player.getName()) &&
                ((targetPlayer == null || another.targetPlayer == null) ? targetPlayer == another.targetPlayer
                        : targetPlayer.getName().equals(another.targetPlayer.getName())) &&
                ((rep == null || another.rep == null) ? rep == another.rep : rep.equals(another.rep)) &&
                (exchange_ind_1 == another.exchange_ind_1) && (exchange_ind_2 == another.exchange_ind_2);
    }
    public String toString () {
        return getName().toString() + ":\r\n" +
                "\tPlayer: " + player.getName() + " (Geisha: " + player.getGeisha().getName().toString() + ")\r\n" +
                (card1 != null ?
                        ("\tCard 1: " + card1.toString() + "\r\n") :
                        ("")) +
                (card2 != null ?
                        ("\tCard 2: " + card2.toString() + "\r\n") :
                        ("")) +
                (targetPlayer != null ?
                        ("\tTarget player: " + targetPlayer.getName() + " (Geisha: " + targetPlayer.getGeisha().getName().toString() + ")\r\n") :
                        ("")) +
                (rep != null ?
                        ("\tReputation: " + rep.toString() + "\r\n") :
                        (""))
                ;

    }

}
