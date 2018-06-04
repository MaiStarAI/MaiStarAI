/** Selection function */
State select (State s0, Determinization d) {
	State best_child = s0;
	while (!isTerminal(best_child) && u(best_child, d).size() == 0)  {
		best_child = best_child.getChildren().get(0);
		for (int i = 1; i < best_child.getChildren().size(); ++i) {
			if (bandit(best_child) < bandit(s0.getChildren().get(i))) {
				best_child = s0.getChildren().get(i);
			}
		}
	}
	return best_child;
}

/** Bandit algorithm */
double bandit (State s) {
	return (double)(s.getReward() / s.getVisits()) + 
			(Math.sqrt(2*(double)(Math.log(e, s.getAvailability())/s.getVisits()));
}

/** Expansion function */
State expand (State s, Determinization d) {
	State child = random from u(s, d); // Tricky
	s.add(child);
	return c;
}

/** Returns if the state is terminal */
boolean isTerminal (State s, Determinization d) {
	if (s.getDrawDeckSize() == 0) {
		return true;
	} else {
		for (int i = 0; i < s.getPlayers().size(); ++i) {
			if (s.getPlayers().get(i).cards(d).size() == 0) result = true;
		}
	}
	return result;
}

/** Get not added possible children */
ArrayList<State> u (State s, Determinization d) {
	ArrayList<State> all  = c(s, d);
	ArrayList<State> list = new ArrayList<>();
	for (int i = 0; i < all.size(); ++i) {
		if (!s.getChildren().contains(all.get(i))) list.add(c_i);
	}
	return list;
}

/** Returns all possible children according to the determinization */
ArrayList<State> c (State s, Determinization d) {
	ArrayList<State> list = new ArrayList<>();
	list.add(getSearchActions(s, d));
	list.addAll(getIntroduceActions(s, d));
	list.addAll(getExchangeActions(s, d));
	list.addAll(getAdvertiserActions(s, d));
	list.addAll(getGuestActions(s, d));
	return list;
}

State getSearchActions (State s, Determinization d) {
	// Add random card from the draw deck
	s.getActivePlayer().addCard(/*here should be random card according to d*/); 	
	return s;
}

ArrayList<State> getIntroduceActions (State s, Determinization d) {
	ArrayList<State> list = new ArrayList<>();
	for (int i = 0; i < s.getActivePlayer().getCards(d).size(); ++i) {
		for (int j = i + 1; j < s.getActivePlayer().getCards(d).size(); ++j) {
			State s_copy = new State();
			s_copy.addValues(s); // To copy values not reference
			s_copy.getActivePlayer().removeCard(i, d);
			s_copy.getActivePlayer().removeCard(j, d);
			s_copy.getActivePlayer().addCard(/*here should be random card according to d*/, d); 
			s_copy.getActivePlayer().addCard(/*here should be random card according to d*/, d); 
			list.add(s_copy);
		}
	}
	return list;
}

ArrayList<State> getExchangeActions (State s, Determinization d)  {
	ArrayList<State> list = new ArrayList<>();
	for (int i = 0; i < s.getActivePlayer().getCards(d).size(); ++i) {
		for (int j = 0; j < s.getActivePlayer().getTableCards().getAdvertisers().size(); ++j) {
			State s_copy = new State();
			s_copy.addValues(s); // To copy values not reference
			Card temp = new Card(s_copy.getActivePlayer().getCards().get(i)); // copy constructor
			s_copy.getActivePlayer().getCards().set(i, s_copy.getTableCards().getAdvertisers().get(j));
			s_copy.getActivePlayer().getTableCards().getAdvertisers().set(j, temp);
			list.add(s_copy);
		}
	}
	return list;
}

ArrayList<State> getAdvertiserActions (State s, Determinization d) {
	ArrayList<State> list = new ArrayList<>();
	for (int i = 0; i < s.getActivePlayer().getCards(d).size(); ++i) {
		State s_copy = new State();
		s_copy.addValues(s); // To copy values not reference
		s_copy.getActivePlayer().getCards(d).getTableCards().getAdvertisers()
				.add(s_copy.getActivePlayer().getCards(d).get(i));
		list.add(s_copy);
	}
	return list;
}

ArrayList<State> getGuestActions (State s, Determinization d) {
	ArrayList<State> list = new ArrayList<>();
	for (int i = 0; i < s.getActivePlayer().getCards(d).size(); ++i) {
		State s_copy = new State();
		s_copy.addValues(s);
		if (s_copy.getActivePlayer().getCards(d).get(i).getRequirement()
					   	<= s.getActivePlayer().getAllAbilities()) { // Tricky
			s_copy.getActivePlayer().getCards(d).getTableCards().getGuests()
					.add(s_copy.getActivePlayer().getCards(d).get(i));
			list.add(s_copy);
		}
	}
	list.addAll(getGuestEffects(list, d)); // Tricky
	return list;
}
