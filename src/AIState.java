import java.util.ArrayList;
import java.util.Random;

class AIState extends State {

    int visits;
    int total_reward;
    int availability;

    Action applied;
    private AIState parent;
    private ArrayList<AIState> children;

    /** Constructor for definition of the algorithm state based on the ordinary state*/
    AIState (State another) {
        super(another);
        children = new ArrayList<>();
        for (Card c : getTurnPlayer().getHand()) c.is_known = true;
        parent = null;
        visits = 0;
        total_reward = 0;
        availability = 1;
        applied = null;
    }

    /** Copy constructor */
    AIState (AIState another) {
        this((State) another);

        visits = another.visits;
        total_reward = another.total_reward;
        availability = another.availability;

        children = new ArrayList<>();
        for (AIState ais : another.children) {
            children.add(new AIState(ais)); // Copies the whole subtree
        }
        parent = another.getParent();

        if (another.applied != null) {
            Player target_player = null;
            if (another.applied.getTargetPlayer() != null) {
                for (Player p : getPlayers())
                    if (p.getName().equals(another.applied.getTargetPlayer().getName()))
                        target_player = p;
            }
            applied = new Action(
                    another.applied.getName(),
                    another.applied.getPlayer(),
                    another.applied.getCard1() == null ? null : new Card(another.applied.getCard1()),
                    another.applied.getCard2() == null ? null : new Card(another.applied.getCard2()),
                    target_player,
                    another.applied.getRep()
            );
            applied.set_exchange_ind_1(another.applied.get_exchange_ind_1());
            applied.set_exchange_ind_2(another.applied.get_exchange_ind_2());
        } else {
            applied = null;
        }
    }

    void fillDeterminization () {

        Random rand = new Random();
        ArrayList<Card> unknown_cards = new ArrayList<>();

        int unknown_dd = 0;
        int[] unknown_players = new int[getPlayers().size()];
        for (int i = 0; i < getPlayers().size(); ++i)
            unknown_players[i] = 0;

        for (int i = getDrawDeck().size() - 1; i >= 0; --i) {
            if (!getDrawDeck().get(i).is_known) {
                unknown_cards.add(new Card(getDrawDeck().get(i)));
                getDrawDeck().remove(getDrawDeck().get(i));
                unknown_dd++;
            }
        }
        for (int i = 0; i < getPlayers().size(); ++i) {
            for (int j = getPlayers().get(i).getHand().size() - 1; j >= 0; --j) {
                if (!getPlayers().get(i).getHand().get(j).is_known) {
                    unknown_cards.add(new Card(getPlayers().get(i).getHand().get(j)));
                    getPlayers().get(i).discardCard(j);
                    unknown_players[i]++;
                }
            }
        }

        for (int i = 0; i < unknown_dd; ++i)
            draw_deck.add(unknown_cards.remove(rand.nextInt(unknown_cards.size())));

        for (int i = 0; i < getPlayers().size(); ++i) {
            for (int j = 0; j < unknown_players[i]; ++j) {
                getPlayers().get(i).addCard(unknown_cards.remove(rand.nextInt(unknown_cards.size())));
            }
        }

    }

    AIState getParent () { return parent; }
    void setParent (AIState parent) { this.parent = parent; }
    ArrayList<AIState> getChildren () { return children; }
    boolean isVictory (String player) {
        boolean is_winner = false;
        int max_score = Integer.MIN_VALUE;
        for (Player p : getPlayers()) {
            if (p.getScore() > max_score) {
                max_score = p.getScore();
                is_winner = p.getName().equals(player);
            }
        }
        return is_winner;
    }

    void setAppliedAction (Action applied) {
        Player target_player = null;
        if (applied.getTargetPlayer() != null) {
            for (Player p : getPlayers())
                if (p.getName().equals(applied.getTargetPlayer().getName()))
                    target_player = p;
        }
        this.applied = new Action(
                applied.getName(),
                applied.getPlayer(),
                applied.getCard1() == null ? null : new Card(applied.getCard1()),
                applied.getCard2() == null ? null : new Card(applied.getCard2()),
                target_player,
                applied.getRep()
        );
        this.applied.set_exchange_ind_1(applied.get_exchange_ind_1());
        this.applied.set_exchange_ind_2(applied.get_exchange_ind_2());
    }

    AIState nextTurn () {

        AIState new_state = new AIState(this);
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
        new_state.allowed_color = null;

        // Update applied actions
        new_state.applied_actions.clear();

        return new_state;
    }

}
