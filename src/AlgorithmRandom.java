import java.util.ArrayList;
import java.util.Random;

class AlgorithmRandom {

    AlgorithmRandom () {}

    Action getAction (State state) {
        ArrayList<Action> children = c(new AIState(state));
        return children.get(new Random().nextInt(children.size()));
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
            if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji &&
                    state.getLastAppliedAction().getName() == Action.Name.Geisha) {

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
                } else {
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
                            guest_effect_action.set_exchange_ind_1(-27);
                            if (state.isApplicableAction(guest_effect_action)) list.add(guest_effect_action);
                        }
                    }
                }
            } else if (state.sumo_player != null) {
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

        for (int i = 0; i < state.getTurnPlayer().getHand().size(); ++i) {
            for (int j = 0; j < state.getTurnPlayer().getAdverts().size(); ++j) {
                Action exchange_action = new Action(
                        Action.Name.Exchange,
                        state.getTurnPlayer(),
                        state.getTurnPlayer().getHand().get(i),
                        state.getTurnPlayer().getAdverts().get(j),
                        null,
                        null
                );
                exchange_action.set_exchange_ind_1(i);
                exchange_action.set_exchange_ind_2(j);
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
        switch (state.getTurnPlayer().getGeisha().getName()) {
            case Momiji: {
                if (state.getTurnPlayer().getGuests().size()  > 0) {
                    Action geisha_action = new Action(
                            Action.Name.Geisha,
                            state.getTurnPlayer(),
                            state.getTurnPlayer().getGuests().get(state.getTurnPlayer().getGuests().size() - 1),
                            null,
                            null,
                            null
                    );
                    if (state.isApplicableAction(geisha_action)) list.add(geisha_action);
                }
                break;
            }
            case Akenohoshi: {
                Action geisha_action_1 = new Action(
                        Action.Name.Geisha,
                        state.getTurnPlayer(),
                        null,
                        null,
                        null,
                        new Reputation(3, 0, 0, 0)
                );
                Action geisha_action_2 = new Action(
                        Action.Name.Geisha,
                        state.getTurnPlayer(),
                        null,
                        null,
                        null,
                        new Reputation(0, 3, 0, 0)
                );
                Action geisha_action_3 = new Action(
                        Action.Name.Geisha,
                        state.getTurnPlayer(),
                        null,
                        null,
                        null,
                        new Reputation(0, 0, 3, 0)
                );
                if (state.isApplicableAction(geisha_action_1)) list.add(geisha_action_1);
                if (state.isApplicableAction(geisha_action_2)) list.add(geisha_action_2);
                if (state.isApplicableAction(geisha_action_3)) list.add(geisha_action_3);
                break;
            }
            case Suzune: case Natsumi: {
                for (Card c : state.getTurnPlayer().getHand()) {
                    Action geisha_action = new Action(
                            Action.Name.Geisha,
                            state.getTurnPlayer(),
                            c,
                            null,
                            null,
                            null
                    );
                    if (state.isApplicableAction(geisha_action)) list.add(geisha_action);
                }
                break;
            }
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
}
