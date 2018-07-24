import java.util.ArrayList;
import java.util.Random;

class AlgorithmISMCTS {

    AlgorithmISMCTS () {}

    Action getAction (State state) {

        int iterations_num = 10000;
        String player = state.getTurnPlayer().getName();
        AIState aiState = new AIState(state);
        AIState selected = aiState;

        for (int i = 0; i < iterations_num; ++i) {

            //if (i % 150 == 0) System.out.println((Math.round(((float) i / (float) iterations_num) * 100f)) + " %");

            // Determinization
            aiState.fillDeterminization();

            // Selection stage
            selected = selection(selected);

            // Expansion stage
            ArrayList<Action> u_children = u(selected);
            if (u_children.size() != 0 )  {
                selected = expansion(selected, u_children);
            }

            // Simulation stage
            int reward = simulation(selected, player);

            // Backpropagation stage
            selected = backpropagation(selected, reward);

        }

        // Choose the most visited node
        int max_visits = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < selected.getChildren().size(); ++i) {
            if (selected.getChildren().get(i).visits > max_visits) {
                max_visits = selected.getChildren().get(i).visits;
                index = i;
            }
        }

        return selected.getChildren().get(index).applied;

    }

    private AIState selection (AIState current) {
        while (u(current).size() == 0 && !current.isTerminal()) {
            switch (current.getTurnPlayer().getType()) {
                case Human: case ISMCTS: {
                    double bestValue = Double.MIN_VALUE;
                    int index = 0;
                    for (int i = 0; i < current.getChildren().size(); ++i) {
                        double value = ucb1(current.getChildren().get(i));
                        if (bestValue < value) {
                            bestValue = value;
                            index = i;
                        }
                    }
                    current = current.getChildren().get(index);
                    break;
                }
                case Random: {
                    current = current.getChildren().get(new Random().nextInt(current.getChildren().size()));
                }
            }

        }
        return current;
    }
    private static double ucb1 (AIState state) {
        double epsilon = 1e-6;
        return state.visits == 0 ? Double.MAX_VALUE :
                (state.total_reward / (state.visits + epsilon)) +
                        (Math.sqrt( (Math.log(state.availability)) / (state.visits + epsilon) ));
    }
    private AIState expansion (AIState selected, ArrayList<Action> u_children) {
        Random rand = new Random();
        Action action = u_children.get(rand.nextInt(u_children.size()));
        AIState new_state = new AIState(selected.applyAction(action));
        new_state = changeStateAfterAction(new_state);
        new_state.setParent(selected);
        new_state.setAppliedAction(action);
        selected.getChildren().add(new_state);
        return new_state;
    }
    private int simulation (AIState selected, String player) {
        Random rand = new Random();
        AIState copy = new AIState(selected);
        while (!copy.isTerminal()) {
            ArrayList<Action> children = c(copy);
            if (children.size() == 0) {
                return copy.isVictory(player) ? 1 : 0;
            } else {
                int rand_i = rand.nextInt( children.size() );
                Action chosen = children.get(rand_i);
                copy = new AIState(copy.applyAction(chosen));
                copy = changeStateAfterAction(copy);
            }
        }
        return copy.isVictory(player) ? 1 : 0;
    }
    private AIState backpropagation (AIState selected, int reward) {
        while (selected.getParent() != null) {
            selected.visits++;
            selected.availability++;
            selected.total_reward += reward;
            selected = selected.getParent();
        }
        return selected;
    }

    private ArrayList<Action> c (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        if (state.use_allowed_actions) {
            if (state.allowed_actions.contains(Action.Name.Guest))
                list.addAll(check_guests(state));
            if (state.allowed_actions.contains(Action.Name.GuestEffect))
                list.addAll(check_guests_effects(state));
            if (state.allowed_actions.contains(Action.Name.Advertiser))
                list.addAll(check_advertisers(state));
            if (state.allowed_actions.contains(Action.Name.Exchange))
                list.addAll(check_exchange(state));
            if (state.allowed_actions.contains(Action.Name.Introduce))
                list.addAll(check_introduce(state));
            if (state.allowed_actions.contains(Action.Name.Search))
                list.addAll(check_search(state));
            if (state.allowed_actions.contains(Action.Name.Geisha))
                list.addAll(check_geisha(state));
            if (state.allowed_actions.contains(Action.Name.CancelEffectRonin))
                list.addAll(check_cancel_effect_ronin(state));
            if (state.allowed_actions.contains(Action.Name.CancelEffectDistrict))
                list.addAll(check_cancel_effect_district(state));
            if (state.allowed_actions.contains(Action.Name.AllowEffect))
                list.addAll(check_allow_effect(state));
            if (state.allowed_actions.contains(Action.Name.HarukazeDiscard))
                list.addAll(check_harukaze(state));
            if (state.allowed_actions.contains(Action.Name.EndTurn))
                list.add(new Action(
                        Action.Name.EndTurn,
                        state.getTurnPlayer(),
                        null,
                        null,
                        null,
                        null
                ));
        } else {
            list.addAll(check_guests(state));
            list.addAll(check_guests_effects(state));
            list.addAll(check_advertisers(state));
            list.addAll(check_exchange(state));
            list.addAll(check_introduce(state));
            list.addAll(check_search(state));
            list.addAll(check_geisha(state));
            list.addAll(check_cancel_effect_ronin(state));
            list.addAll(check_cancel_effect_district(state));
            list.addAll(check_allow_effect(state));
        }

        return list;
    }
    private ArrayList<Action> u (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        ArrayList<Action> c_actions = c(state);

        for (Action c_act : c_actions) {
            boolean contains = false;
            for (AIState child : state.getChildren()) {
                if (c_act.equals(child.applied)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                list.add(c_act);
            }
        }

        return list;
    }

    private ArrayList<Action> check_guests (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        for (Card c : state.getTurnPlayer().getHand()) {
            Action guest_action = new Action(
                    Action.Name.Guest,
                    state.getTurnPlayer(),
                    c,
                    null,
                    null,
                    null
            );
            if (state.isApplicableAction(guest_action)) list.add(guest_action);
        }

        return list;
    }
    private ArrayList<Action> check_guests_effects (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        if (state.getLastAppliedAction() != null &&
                (state.getLastAppliedAction().getName() == Action.Name.Guest ||
                        state.getLastAppliedAction().getName() == Action.Name.AllowEffect ||
                        (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji &&
                                state.getLastAppliedAction().getName() == Action.Name.Geisha))) {
            if (state.sumo_player != null) {
                for (Card c : state.sumo_player.getHand()) {
                    Action guest_effect_action = new Action(
                            Action.Name.GuestEffect,
                            state.getTurnPlayer(),
                            state.getLastAppliedAction().getCard1(),
                            c,
                            state.sumo_player,
                            null
                    );
                    if (state.isApplicableAction(guest_effect_action)) list.add(guest_effect_action);
                }
            } else if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji &&
                    state.getLastAppliedAction().getName() == Action.Name.Geisha) {
                Action last_effect = null;
                for (Action act : state.getAppliedActions())
                    if (act.getName() == Action.Name.GuestEffect)
                        last_effect = act;
                if (last_effect != null) {
                    for (Player p : state.getPlayers()) {
                        Action guest_effect_action = new Action(
                                Action.Name.GuestEffect,
                                state.getTurnPlayer(),
                                state.getLastAppliedAction().getCard1(),
                                last_effect.getCard1(),
                                p,
                                null
                        );
                        if (state.isApplicableAction(guest_effect_action)) list.add(guest_effect_action);
                    }
                }
            } else {
                for (Player p : state.getPlayers()) {
                    for (Card c : p.getHand()) {
                        Action guest_effect_action = new Action(
                                Action.Name.GuestEffect,
                                state.getTurnPlayer(),
                                state.getLastAppliedAction().getCard1(),
                                c,
                                p,
                                null
                        );
                        if (state.isApplicableAction(guest_effect_action)) list.add(guest_effect_action);
                    }
                }
            }
        }

        return list;
    }
    private ArrayList<Action> check_advertisers (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        for (Card c : state.getTurnPlayer().getHand()) {
            Action advertiser_action = new Action(
                    Action.Name.Advertiser,
                    state.getTurnPlayer(),
                    c,
                    null,
                    null,
                    null
            );
            if (state.isApplicableAction(advertiser_action)) list.add(advertiser_action);
        }

        return list;
    }
    private ArrayList<Action> check_exchange (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        for (Card c1 : state.getTurnPlayer().getHand()) {
            for (Card c2 : state.getTurnPlayer().getAdverts()) {
                Action exchange_action = new Action(
                        Action.Name.Exchange,
                        state.getTurnPlayer(),
                        c1,
                        c2,
                        null,
                        null
                );
                if (state.isApplicableAction(exchange_action)) list.add(exchange_action);
            }
        }

        return list;
    }
    private ArrayList<Action> check_introduce (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
            for (int j = i + 1; j < state.getTurnPlayer().getHand().size(); ++j) {
                Action introduce_action = new Action(
                        Action.Name.Introduce,
                        state.getTurnPlayer(),
                        state.getTurnPlayer().getHand().get(i),
                        state.getTurnPlayer().getHand().get(j),
                        null,
                        null
                );
                if (state.isApplicableAction(introduce_action)) list.add(introduce_action);
            }
        }

        return list;
    }
    private ArrayList<Action> check_search (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        Action search_action = new Action(
                Action.Name.Search,
                state.getTurnPlayer(),
                null,
                null,
                null,
                null
        );
        if (state.isApplicableAction(search_action)) list.add(search_action);

        return list;
    }
    private ArrayList<Action> check_geisha (AIState state) {
        ArrayList<Action> list = new ArrayList<>();
        for (Card c : state.getTurnPlayer().getHand()) {
            Action geisha_action = new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    c,
                    null,
                    null,
                    new Reputation(3, 0, 0, 0)
            );
            if (state.isApplicableAction(geisha_action)) list.add(geisha_action);
            geisha_action = new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    c,
                    null,
                    null,
                    new Reputation(0, 3, 0, 0)
            );
            if (state.isApplicableAction(geisha_action)) list.add(geisha_action);
            geisha_action = new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    c,
                    null,
                    null,
                    new Reputation(0, 0, 3, 0)
            );
            if (state.isApplicableAction(geisha_action)) list.add(geisha_action);

        }

        if (state.getTurnPlayer().getGuests().size() > 0) {
            /* Momiji */
            Action geisha_action = new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    state.getTurnPlayer().getGuests().get(state.getTurnPlayer().getGuests().size() - 1),
                    null,
                    null,
                    new Reputation(0, 0, 0, 0)
            );
            if (state.isApplicableAction(geisha_action)) list.add(geisha_action);
        }

        return list;
    }
    private ArrayList<Action> check_cancel_effect_ronin (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        Action cancel_ronin_action = new Action(
                Action.Name.CancelEffectRonin,
                state.getTurnPlayer(),
                null,
                null,
                null,
                null
        );
        if (state.isApplicableAction(cancel_ronin_action)) list.add(cancel_ronin_action);

        return list;
    }
    private ArrayList<Action> check_cancel_effect_district (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        Action cancel_district_action = new Action(
                Action.Name.CancelEffectDistrict,
                state.getTurnPlayer(),
                null,
                null,
                null,
                null
        );
        if (state.isApplicableAction(cancel_district_action)) list.add(cancel_district_action);

        return list;
    }
    private ArrayList<Action> check_allow_effect (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        Action allow_action = new Action(
                Action.Name.AllowEffect,
                state.getTurnPlayer(),
                null,
                null,
                null,
                null
        );
        if (state.isApplicableAction(allow_action)) list.add(allow_action);

        return list;
    }
    private ArrayList<Action> check_harukaze (AIState state) {
        ArrayList<Action> list = new ArrayList<>();

        if (state.allowed_color == Card.Color.GREEN) {
            for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
                for (int j = i + 1; j < state.getTurnPlayer().getHand().size(); ++j) {
                    Action discard_cards = new Action(
                            Action.Name.HarukazeDiscard,
                            state.getTurnPlayer(),
                            state.getTurnPlayer().getHand().get(i),
                            state.getTurnPlayer().getHand().get(j),
                            null,
                            new Reputation(0,0,0,2)
                    );
                    if (state.isApplicableAction(discard_cards)) list.add(discard_cards);
                }
            }
            return list;
        } else if (state.allowed_color == Card.Color.BLUE) {
            for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
                list.add(new Action(
                        Action.Name.HarukazeDiscard,
                        state.getTurnPlayer(),
                        state.getTurnPlayer().getHand().get(i),
                        null,
                        null,
                        new Reputation(0,0,0,1)
                ));
            }
            return list;
        } else {
            list.add(new Action(
                    Action.Name.HarukazeDiscard,
                    state.getTurnPlayer(),
                    null,
                    null,
                    null,
                    new Reputation(0,0,0,0)
            ));
            return list;
        }

    }

    private static AIState changeStateAfterAction (AIState state) {

        /* If someone used effect against another player */
        if (state.special_turn) {
            return state;
        }

        switch (state.getLastAppliedAction().getName()) {
            case GuestEffect: {

                /* When a doctor effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Doctor) {
                    String turning_player = state.getTurnPlayer().getName();
                    state = state.nextTurn();
                    for (Player p : state.getPlayers()) {
                        if (p.getName().equals(turning_player)) {
                            state.setTurnPlayer(p);
                        }
                    }
                    return state;
                }

                /* When an okaasan effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Okaasan) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Advertiser);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                /* When a courtier effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Courtier) {
                    state.use_allowed_actions = true;
                    state.allowed_color = state.getLastAppliedAction().getCard1().getColor();
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Guest);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                break;
            }
            case Guest: {

                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Sumo_Wrestler) {
                    Player target = state.getTurnPlayer();
                    int max_cards = target.getHand().size();
                    for (Player p : state.getPlayers()) {
                        if (p.getHand().size() > max_cards) {
                            max_cards = p.getHand().size();
                            target = p;
                        }
                    }

                    state.sumo_player = target;
                    for (Card c : target.getHand()) c.is_known = true;

                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    return state;
                } else {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

            }
            case Geisha: {

                /* Akenohoshi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi) {
                    return state;
                }

                /* Suzune */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Suzune) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Advertiser);
                    return state;
                }

                /* Momiji */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                /* Natsumi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Guest);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                break;
            }
            case Advertiser: {

                /* Harukaze */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Harukaze) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.HarukazeDiscard);
                    switch (state.getDrawDeck().size()) {
                        case 0: {
                            state.allowed_color = Card.Color.RED;
                            break;
                        }
                        case 1: {
                            state.allowed_color = Card.Color.BLUE;
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            break;
                        }
                        default: {
                            state.allowed_color = Card.Color.GREEN;
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            break;
                        }
                    }
                    return state;
                }

                break;
            }
            case EndTurn: case HarukazeDiscard: {

                /* Natsumi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
                    if (state.isApplicableAction(new Action(
                            Action.Name.Geisha,
                            state.getTurnPlayer(),
                            state.getLastAppliedAction().getCard1(),
                            null,
                            null,
                            null
                    ))) {
                        state.use_allowed_actions = true;
                        state.allowed_actions.clear();
                        state.allowed_color = Card.Color.BLUE;
                        state.allowed_actions.add(Action.Name.Geisha);
                        state.allowed_actions.add(Action.Name.EndTurn);
                        return state;
                    }
                }

                state = state.nextTurn();
                return state;
            }
        }

        /* Momiji */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji) {
            if (state.isApplicableAction(new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    state.getLastAppliedAction().getCard1(),
                    null,
                    null,
                    null
            ))) {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_color = Card.Color.RED;
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Natsumi */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
            if (state.isApplicableAction(new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    state.getLastAppliedAction().getCard1(),
                    null,
                    null,
                    null
            ))) {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_color = Card.Color.BLUE;
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Suzune */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Suzune) {
            boolean geisha_was_used = false;
            for (Action act : state.getAppliedActions()) {
                if (act.getName() == Action.Name.Geisha) {
                    geisha_was_used = true;
                    break;
                }
            }
            if (geisha_was_used) {
                if (state.getAppliedActions().get(0).getName() == Action.Name.Geisha) {
                    if (state.getAppliedActions().size() == 2) {
                        state.use_allowed_actions = true;
                        ArrayList<Action.Name> names = state.getAllActions();
                        names.remove(Action.Name.Geisha);
                        state.allowed_actions.clear();
                        state.allowed_actions.addAll(names);
                        return state;
                    }
                } else {
                    state = state.nextTurn();
                    return state;
                }
            } else {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Delete Akenohoshi bonus */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi) {
            state.getTurnPlayer().setAkenohoshiBonus(new Reputation(0, 0, 0));
            state = state.nextTurn();
            return state;
        }

        /* Next turn as usual */
        state = state.nextTurn();
        return state;
    }

}
