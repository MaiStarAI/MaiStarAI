import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

class State {

    int turn;
    private int round;

    // For Ronin and Kanryou
    boolean special_turn;
    private Player last_player;
    private Action last_effect;

    // For sumo wrestler's effect
    Player sumo_player = null;

    boolean use_allowed_actions;
    ArrayList<Action.Name> allowed_actions;
    Card.Color allowed_color;

    ArrayList<Player> players;
    ArrayList<Card> draw_deck;
    ArrayList<Card> discarded;

    ArrayList<Action> applied_actions;  // All actions performed this turn

    Player turning_player;

    /** Constructor for creation of the new state */
    State () {
        turn = 0;
        turning_player = null;
        players = new ArrayList<>();
        draw_deck = new ArrayList<>();
        discarded = new ArrayList<>();
        round = 0;
        use_allowed_actions = false;
        allowed_actions = new ArrayList<>();
        applied_actions = new ArrayList<>();
    }

    /** Copy constructor */
    State (State another) {
        this();
        turn = another.turn;
        setRound(another.getRound());
        for (Player p : another.getPlayers()) addPlayer(new Player(p)); // New instance
        fillDrawDeck(another.getDrawDeck()); // New instances
        fillDiscarded(another.getDiscardedCards()); // New instances

        int index = another.getPlayers().indexOf(another.getTurnPlayer()); // Old player
        turning_player = getPlayers().get(index); // From new players
        use_allowed_actions = another.use_allowed_actions;
        allowed_color = another.allowed_color;
        allowed_actions.addAll(another.allowed_actions);
        for (Action act : another.applied_actions) {
            Player target_player = null;
            if (act.getTargetPlayer() != null) {
                for (Player p : getPlayers())
                    if (p.getName().equals(act.getTargetPlayer().getName()))
                        target_player = p;
            }
            applied_actions.add(new Action(
                    act.getName(),
                    act.getPlayer(),
                    act.getCard1() == null ? null : new Card(act.getCard1()),
                    act.getCard2() == null ? null : new Card(act.getCard2()),
                    target_player,
                    act.getRep()
            ));
        }

        special_turn = another.special_turn;

        if (another.last_player == null) {
            last_player = null;
        } else {
            for (Player p : getPlayers())
                if (p.getName().equals(another.last_player.getName()))
                    last_player = p;
        }

        Player target_player = null;
        if (another.last_effect == null) {
            last_effect = null;
        } else {
            if (another.last_effect.getTargetPlayer() != null) {
                for (Player p : getPlayers())
                    if (p.getName().equals(another.last_effect.getTargetPlayer().getName()))
                        target_player = p;
            }
            last_effect = new Action(
                    another.last_effect.getName(),
                    another.getTurnPlayer(),
                    another.last_effect.getCard1() == null ? null : new Card(another.last_effect.getCard1()),
                    another.last_effect.getCard2() == null ? null : new Card(another.last_effect.getCard2()),
                    target_player,
                    another.last_effect.getRep()
            );
        }

    }

    boolean isApplicableAction (Action action) {
        switch (action.getName()) {
            case Geisha: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.Geisha))) &&
                        isApplicableGeisha(action);
            }
            case Guest: {
                boolean possible = (!use_allowed_actions || (allowed_actions.contains(Action.Name.Guest)));
                switch (action.getCard1().getColor()) {
                    case RED: {
                        return possible && (allowed_color == null || (allowed_color == Card.Color.RED)) &&
                                turning_player.getReputation().getRed() + turning_player.getAkenohoshiBonus().getRed() >= action.getCard1().getReq().getRed();
                    }
                    case BLUE: {
                        return possible && (allowed_color == null || (allowed_color == Card.Color.BLUE)) &&
                                turning_player.getReputation().getBlue() + turning_player.getAkenohoshiBonus().getBlue() >= action.getCard1().getReq().getBlue();
                    }
                    case GREEN: {
                        return possible && (allowed_color == null || (allowed_color == Card.Color.GREEN)) &&
                                turning_player.getReputation().getGreen() + turning_player.getAkenohoshiBonus().getGreen() >= action.getCard1().getReq().getGreen();
                    }
                    case BLACK: {
                        return possible && action.getCard1().getName() != Card.Name.District_Kanryou &&
                                turning_player.getReputation().getBlack() + turning_player.getAkenohoshiBonus().getBlack() >= action.getCard1().getReq().getBlack();
                    }
                    default: {
                        return false;
                    }
                }
            }
            case Exchange: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.Exchange))) &&
                        turning_player.getAdverts().size() > 0;
            }
            case Introduce: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.Introduce))) &&
                        draw_deck.size() > 1;
            }
            case Advertiser: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.Advertiser))) &&
                        draw_deck.size() > 0;
            }
            case GuestEffect: {
                return applied_actions.size() > 0 && (getLastAppliedAction().getName().equals(Action.Name.Guest) || (
                        getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji &&
                                getLastAppliedAction().getName() == Action.Name.Geisha
                ))
                        && (allowed_color == null || (allowed_color == action.getCard1().getColor())) &&
                        (!use_allowed_actions || (allowed_actions.contains(Action.Name.GuestEffect))) &&
                        isApplicableEffect(action);
            }
            case Search: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.Search))) && draw_deck.size() > 0;
            }
            case CancelEffectDistrict: {
                if (special_turn && allowed_actions.contains(Action.Name.CancelEffectDistrict))
                    for (Card c : turning_player.getHand()) if (c.getName() == Card.Name.District_Kanryou) return true;
                return false;
            }
            case CancelEffectRonin: {
                if (special_turn && allowed_actions.contains(Action.Name.CancelEffectRonin))
                    for (Card c : turning_player.getGuests()) if (c.getName() == Card.Name.Ronin) return true;
                return false;
            }
            case AllowEffect: {
                return special_turn && allowed_actions.contains(Action.Name.AllowEffect);
            }
            case HarukazeDiscard: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.HarukazeDiscard))) &&
                        turning_player.getHand().size() > 2;
            }
            case EndTurn: {
                return (!use_allowed_actions || (allowed_actions.contains(Action.Name.EndTurn)));
            }
            default: {
                return false;
            }
        }
    }
    private boolean isApplicableGeisha(Action action) {
        switch (turning_player.getGeisha().getName()) {
            case Oboro: {
                return false;
            }
            case Akenohoshi: {
                return turning_player.getGeishaUsages() == 0;
            }
            case Harukaze: {
                return false;
            }
            case Momiji: {
                if (applied_actions.size() == 0) return false;
                if (turning_player.getGeishaUsages() > 0) return false;
                boolean is_possible = false;
                for (int i = 0; i < applied_actions.size() - 1; ++i)
                    if (applied_actions.get(i).getName() == Action.Name.Guest &&
                            applied_actions.get(i).getCard1().getColor() == Card.Color.RED &&
                            action.getCard1() != null &&
                            applied_actions.get(i).getCard1().getName() == action.getCard1().getName() &&
                            applied_actions.get(i + 1).getName() == Action.Name.GuestEffect)
                        is_possible = true;
                return is_possible;
            }
            case Suzune: {
                return action.getCard1() != null && turning_player.getGeishaUsages() == 0;
            }
            case Natsumi: {
                if (applied_actions.size() == 0) return false;
                if (turning_player.getGeishaUsages() > 1) return false;
                boolean is_possible = false;
                for (Action applied_action : applied_actions)
                    if (applied_action.getName() == Action.Name.Guest &&
                            action.getCard1() != null &&
                            action.getCard1().getColor() == Card.Color.BLUE)
                        is_possible = true;
                return is_possible;
            }
            default: {
                return false;
            }
        }
    }
    private boolean isApplicableEffect (Action action) {
        switch (action.getCard1().getName()) {
            case Sumo_Wrestler: {
                return true;
            }
            case District_Kanryou: {
                return false;
            }
            case Merchant: {
                return draw_deck.size() > 1;
            }
            case Emissary: case Yakuza: {
                return action.getTargetPlayer().getAdverts().size() > 0;
            }
            case Scholar: {
                return draw_deck.size() > 1;
            }
            case Samurai: case Thief: {
                return action.getTargetPlayer().getGuests().size() > 0;
            }
            case Okaasan: {
                return action.getTargetPlayer().getName().equals(turning_player.getName()) && turning_player.getHand().size() > 0;
            }
            case Shogun: {
                return action.getTargetPlayer().getName().equals(turning_player.getName());
            }
            case Doctor: {
                return action.getTargetPlayer().getName().equals(turning_player.getName()) && turning_player.getHand().size() > 0;
            }
            case Daimyo: {
                if (!action.getTargetPlayer().getName().equals(turning_player.getName())) return false;
                for (Card c : turning_player.getAdverts()) if (c.getColor() == action.getCard1().getColor()) return true;
                return false;
            }
            case Actor: {
                return false;
            }
            case Monk: {
                return action.getTargetPlayer().getName().equals(turning_player.getName()) && turning_player.getHand().size() > 0;
            }
            case Ronin: {
                return false;
            }
            case Courtier: {
                if (!action.getTargetPlayer().getName().equals(turning_player.getName())) return false;
                boolean is_possible = false;
                for (Card c : turning_player.getHand()) {
                    switch (action.getCard1().getColor()) {
                        case RED: {
                            if (c.getColor() == Card.Color.RED &&
                                    turning_player.getReputation().getRed() >= c.getReq().getRed()) {
                                is_possible = true;
                                break;
                            }
                        }
                        case BLUE: {
                            if (c.getColor() == Card.Color.BLUE &&
                                    turning_player.getReputation().getBlue() >= c.getReq().getBlue()) {
                                is_possible = true;
                                break;
                            }
                        }
                        case GREEN: {
                            if (c.getColor() == Card.Color.GREEN &&
                                    turning_player.getReputation().getGreen() >= c.getReq().getGreen()) {
                                is_possible = true;
                                break;
                            }
                        }
                        default: {
                            return false;
                        }
                    }
                }
                return is_possible;
            }
        }
        return false;
    }

    State applyAction (Action action) {
        State new_state = new State(this);
        new_state.getAppliedActions().add(action);

        switch (action.getName()) {
            case CancelEffectRonin: {

                for (int i = 0; i < new_state.turning_player.getGuests().size(); ++i) {
                    if (new_state.turning_player.getGuests().get(i).getName() == Card.Name.Ronin) {
                        new_state.turning_player.getGuests().get(i).usages++;
                        if (!(new_state.turning_player.getGeisha().getName() == Geisha.Name.Momiji &&
                                new_state.turning_player.getGuests().get(i).getColor() == Card.Color.RED &&
                                new_state.turning_player.getGuests().get(i).usages < 2)) {
                            new_state.turning_player.discardGuest(new_state.turning_player.getGuests().get(i));
                        }
                    }
                }

                for (Player p : new_state.getPlayers()) {
                    if (p.getName().equals(last_player.getName())) {
                        new_state.turning_player = p;
                    }
                }
                new_state.special_turn = false;
                new_state.last_player = null;
                new_state.last_effect = null;
                new_state.allowed_actions.clear();
                new_state.use_allowed_actions = false;
                return new_state;
            }
            case CancelEffectDistrict: {

                for (int i = 0; i < new_state.turning_player.getHand().size(); ++i)
                    if (new_state.turning_player.getHand().get(i).getName() == Card.Name.District_Kanryou)
                        new_state.turning_player.discardCard(new_state.turning_player.getHand().get(i));

                for (Player p : new_state.getPlayers()) {
                    if (p.getName().equals(last_player.getName())) {
                        new_state.turning_player = p;
                    }
                }
                new_state.special_turn = false;
                new_state.last_player = null;
                new_state.last_effect = null;
                new_state.allowed_actions.clear();
                new_state.use_allowed_actions = false;
                return new_state;
            }
            case AllowEffect: {
                new_state = new_state.applyAction(last_effect);
                for (Player p : new_state.getPlayers()) {
                    if (p.getName().equals(last_player.getName())) {
                        new_state.turning_player = p;
                    }
                }

                new_state.special_turn = false;
                new_state.last_player = null;
                new_state.last_effect = null;
                new_state.allowed_actions.clear();
                new_state.use_allowed_actions = false;
                return new_state;
            }
            case GuestEffect: {
                if (action.getTargetPlayer().getName().equals(turning_player.getName())) {
                    new_state = applyEffect(action, new_state);
                    return new_state;
                }
                switch (action.getCard1().getName()) {
                    case Merchant:
                    case Samurai:
                    case Yakuza:
                    case Scholar:
                    case Thief:
                    case Emissary:
                    case Sumo_Wrestler: {
                        new_state.last_effect = action;
                        new_state.last_player = getTurnPlayer();
                        new_state.special_turn = true;
                        new_state.use_allowed_actions = true;
                        new_state.allowed_actions.clear();
                        new_state.allowed_actions.add(Action.Name.AllowEffect);
                        new_state.allowed_actions.add(Action.Name.CancelEffectRonin);
                        new_state.allowed_actions.add(Action.Name.CancelEffectDistrict);
                        for (Player p : new_state.players) {
                            if (p.getName().equals(action.getTargetPlayer().getName())) {
                                new_state.turning_player = p;
                                break;
                            }
                        }
                        return new_state;
                    }
                    default: {
                        new_state = applyEffect(action, new_state);
                        return new_state;
                    }
                }
            }
            case Advertiser: {
                new_state.getTurnPlayer().addAdv(action.getCard1());
                new_state.getTurnPlayer().discardCard(action.getCard1());
                new_state.getTurnPlayer().addCard(new_state.getRandomCard());
                return new_state;
            }
            case Introduce: {
                new_state.turning_player.discardCard(action.getCard1());
                new_state.turning_player.discardCard(action.getCard2());
                new_state.turning_player.addCard(new_state.getRandomCard());
                new_state.turning_player.addCard(new_state.getRandomCard());
                new_state.addToDiscard(action.getCard1()); // To discard
                new_state.addToDiscard(action.getCard2()); // To discard
                return new_state;
            }
            case Exchange: {
                new_state.getTurnPlayer().discardAdv(action.getCard2());
                new_state.getTurnPlayer().addAdv(action.getCard1());
                new_state.getTurnPlayer().discardCard(action.getCard1());
                new_state.getTurnPlayer().addCard(action.getCard2());
                return new_state;
            }
            case Search: {
                new_state.turning_player.addCard(new_state.getRandomCard());
                return new_state;
            }
            case Guest: {
                new_state.getTurnPlayer().addGuest(action.getCard1());
                new_state.getTurnPlayer().discardCard(action.getCard1());
                return new_state;
            }
            case Geisha: {
                new_state = applyGeishaEffect(action, new_state);
                return new_state;
            }
            case HarukazeDiscard: {
                switch (new_state.getDrawDeck().size()) {
                    case 0: {
                        return new_state;
                    }
                    case 1: {
                        new_state.getTurnPlayer().discardCard(action.getCard1());
                        Card card1 = new Card(action.getCard1()); card1.is_known = true;
                        new_state.getDrawDeck().add(card1);
                        return new_state;
                    }
                    default: {
                        new_state.getTurnPlayer().discardCard(action.getCard1());
                        new_state.getTurnPlayer().discardCard(action.getCard2());
                        Card card1 = new Card(action.getCard1()); card1.is_known = true;
                        Card card2 = new Card(action.getCard2()); card2.is_known = true;
                        new_state.getDrawDeck().add(card1);
                        new_state.getDrawDeck().add(card2);
                        return new_state;
                    }
                }
            }
            case EndTurn: {
                return new_state;
            }
            default: {
                return null;
            }
        }
    }
    private State applyEffect (Action action, State state) {

        state.special_turn = false;
        state.last_player = null;
        state.last_effect = null;
        state.allowed_actions.clear();
        state.use_allowed_actions = false;

        switch (action.getCard1().getName()) {
            case Sumo_Wrestler: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        p.discardCard(action.getCard2());
                        state.addToDiscard(action.getCard2());
                    }
                }
                if (!(getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji &&
                        getTurnPlayer().getGeishaUsages() == 0))
                    state.sumo_player = null;
                return state;
            }
            case Merchant: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        p.addCard(state.getRandomCard());
                        p.addCard(state.getRandomCard());
                    }
                }
                return state;
            }
            case Emissary: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        Card card = new Card(p.getAdverts().get(p.getAdverts().size() - 1));
                        state.turning_player.addAdv(card);
                        p.discardAdv(card);
                        state.addToDiscard(card);
                    }
                }
                return state;
            }
            case Yakuza: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        state.addToDiscard(p.getAdverts().get(p.getAdverts().size() - 1));
                        p.discardAdv(p.getAdverts().get(p.getAdverts().size() - 1));
                    }
                }
                return state;
            }
            case Scholar: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        p.addCard(state.getRandomCard());
                    }
                }
                return state;
            }
            case Samurai: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        Card card = new Card(p.getGuests().get(p.getGuests().size() - 1));
                        state.turning_player.addGuest(card);
                        p.discardGuest(card);
                        state.addToDiscard(card);
                    }
                }
                return state;
            }
            case Thief: {
                for (Player p : state.players) {
                    if (p.getName().equals(action.getTargetPlayer().getName())) {
                        state.addToDiscard(p.getGuests().get(p.getGuests().size() - 1));
                        p.discardGuest(p.getGuests().get(p.getGuests().size() - 1));
                    }
                }
                return state;
            }
            case Okaasan: {
                return state;
            }
            case Shogun: {
                for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
                    state.getTurnPlayer().addGuest(state.getTurnPlayer().getHand().get(i));
                    state.getTurnPlayer().discardCard(state.getTurnPlayer().getHand().get(i));
                }
                return state;
            }
            case Doctor: {
                return state;
            }
            case Daimyo: {
                for (int i = 0; i < state.getTurnPlayer().getAdverts().size(); ++i) {
                    if (state.getTurnPlayer().getAdverts().get(i).getColor() == action.getCard1().getColor()) {
                        state.getTurnPlayer().addGuest(state.getTurnPlayer().getAdverts().get(i));
                        state.getTurnPlayer().discardAdv(state.getTurnPlayer().getAdverts().get(i));
                    }
                }
            }
            case Monk: {
                for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
                    state.addToDiscard(state.getTurnPlayer().getHand().get(i));
                    state.getTurnPlayer().discardCard(state.getTurnPlayer().getHand().get(i));
                }
            }
            case Courtier: {
                state.applyAction(new Action(
                        Action.Name.Guest,
                        action.getPlayer(),
                        action.getCard2(),
                        null,
                        null,
                        null
                ));
                return state;
            }
        }
        return null;
    }
    private State applyGeishaEffect (Action action, State state) {
        switch (state.turning_player.getGeisha().getName()) {
            case Natsumi: {
                state.turning_player.increaseGeishaUsages();
                return state;
            }
            case Suzune: {
                state.turning_player.increaseGeishaUsages();
                return state;
            }
            case Momiji: {
                state.turning_player.increaseGeishaUsages();
                return state;
            }
            case Harukaze: {
                state.turning_player.increaseGeishaUsages();
                return state;
            }
            case Akenohoshi: {
                state.turning_player.setAkenohoshiBonus(action.getRep());
                state.turning_player.increaseGeishaUsages();
                return state;
            }
            default: {
                return null;
            }
        }
    }

    /** Adds a reference to the 'players' array */
    void addPlayer (Player p) { players.add(p); }

    /** Fills the draw deck with cards (creates instances) */
    void fillDrawDeck (ArrayList<Card> cards) {
        draw_deck.clear();
        for (Card c : cards) draw_deck.add(new Card(c));
    }
    void fillDiscarded (ArrayList<Card> cards) {
        discarded.clear();
        for (Card c : cards) discarded.add(new Card(c));
    }
    void addToDiscard (Card card) {
        discarded.add(new Card(card));
    }

    /** Returns new instance and removes old from the deck */
    Card getRandomCard () {
        int r_index = new Random().nextInt(draw_deck.size());
        Card random = new Card(draw_deck.get(r_index));
        draw_deck.remove(r_index);
        return random;
    }
    /** Removes a given card from the draw deck and returns it (returns null in case of card's absence) */
    Card removeCard (Card.Name name, Card.Color color) {
        for (int i = 0; i < draw_deck.size(); ++i) {
            Card copy = new Card(name, color);
            if (draw_deck.get(i).equals(copy)) {
                Card temp = new Card(draw_deck.get(i));
                draw_deck.remove(i);
                return temp;
            }
        }
        return null;
    }

    boolean isTerminal () {
        if (draw_deck.size() == 0) return true;
        for (Player p : players) if (p.getHand().size() == 0) return true;
        return false;
    }
    State nextTurn () {

        State new_state = new State(this);
        new_state.turn++;

        // Changes turning player (reference)
        int index = new_state.players.indexOf(new_state.turning_player);
        if (index == new_state.players.size() - 1) {
            new_state.turning_player = new_state.players.get(0);
        } else {
            new_state.turning_player = new_state.players.get(index + 1);
        }

        // Updates geisha value
        if (new_state.turning_player.getGeisha().getName() != Geisha.Name.Akenohoshi ||
                new_state.turning_player.getGeisha().getName() != Geisha.Name.Oboro) {
            new_state.turning_player.setGeishaUsages(0);
        }

        // Restore allowed actions variables
        new_state.use_allowed_actions =  false;
        new_state.allowed_actions.clear();
        new_state.allowed_color =  null;

        /*PrintWriter out_game = new PrintWriter(new FileWriter("game_logs/game_" + 1 +".log", true)); //todo

        // Print turn information
        out_game.println("--------------------------------------------");
        out_game.println("Round " + (getRound() + 1) + ", turn " + (turn + 1) + ":");
        out_game.println();
        out_game.println("Turn player: " + getTurnPlayer().toString());
        out_game.println();
        for (Action act : applied_actions) {
            out_game.println(act.toString());
        }

        out_game.close();*/

        // Update applied actions
        new_state.applied_actions.clear();

        return new_state;
    }
    int getTurn () { return turn; }

    int getRound () { return round; }
    void setRound (int round) { this.round = round; }

    Player getLastPlayer () { return last_player; }

    Player getTurnPlayer () { return turning_player; }
    void setTurnPlayer (Player player) {
        for (Player p : getPlayers()) {
            if (p.getName().equals(player.getName())) {
                turning_player = p;
                break;
            }
        }
    }
    ArrayList<Player> getPlayers () { return players; }
    ArrayList<Card> getDrawDeck () { return draw_deck; }
    ArrayList<Card> getDiscardedCards() { return discarded; }

    ArrayList<Action> getAppliedActions() { return applied_actions; }
    Action getLastAppliedAction () {
        return applied_actions.size() == 0 ? null :
                applied_actions.get(applied_actions.size() - 1);
    }

    ArrayList<Action.Name> getAllActions () {
        ArrayList<Action.Name> list = new ArrayList<>();
        list.add(Action.Name.Advertiser);
        list.add(Action.Name.AllowEffect);
        list.add(Action.Name.Exchange);
        list.add(Action.Name.Geisha);
        list.add(Action.Name.Guest);
        list.add(Action.Name.GuestEffect);
        list.add(Action.Name.Search);
        list.add(Action.Name.Introduce);
        list.add(Action.Name.CancelEffectRonin);
        list.add(Action.Name.CancelEffectDistrict);
        return list;
    }

}
