class Card {

    enum Name {
        Monk, Doctor, Shogun, Actor, Okaasan, Sumo_Wrestler, Emissary, Samurai,
        Daimyo, Ronin, District_Kanryou, Thief, Yakuza, Courtier, Merchant, Scholar
    }

    enum Color {
        RED, BLUE, GREEN, BLACK
    }

    private Name name;
    private Color color;
    private Reputation bonus;
    private Reputation req; // Requirement
    private int reward;
    int usages = 0; // For Momiji effect with Ronins

    // For ISMCTS
    boolean is_known = false;

    /** Constructor for creation of the new card */
    Card (Name name, Color color) {
        this.name = name;
        this.color = color;
        switch (name) {
            case Ronin: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(2, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 2, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 2);
                        break;
                    }
                }
                reward =  2;
                break;
            }
            case Monk: {
                bonus = new Reputation(1, 1, 1);
                req =  new Reputation(0, 0, 0, 9);
                reward = 10;
                break;
            }
            case Actor: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 1, 1);
                        req =  new Reputation(5, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(1, 1, 1);
                        req =  new Reputation(0, 5, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(1, 1, 1);
                        req =  new Reputation(0, 0, 5);
                        break;
                    }
                }
                reward =  5;
                break;
            }
            case Thief: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(4, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 4, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 4);
                        break;
                    }
                }
                reward =  4;
                break;
            }
            case Daimyo: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(9, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 9, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 9);
                        break;
                    }
                }
                reward = 10;
                break;
            }
            case Doctor: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(7, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 7, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 7);
                        break;
                    }
                }
                reward = 6;
                break;
            }
            case Shogun: {
                bonus = new Reputation(1, 1, 1);
                req =  new Reputation(0, 0, 0, 10);
                reward = 10;
                break;
            }
            case Courtier: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(3, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 3, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 3);
                        break;
                    }
                }
                reward = 0;
                break;
            }
            case Yakuza: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(3, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 3, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 3);
                        break;
                    }
                }
                reward = 3;
                break;
            }
            case Okaasan: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(2, 0, 0);
                        req =  new Reputation(2, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 2, 0);
                        req =  new Reputation(0, 2, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 2);
                        req =  new Reputation(0, 0, 2);
                        break;
                    }
                }
                reward = 1;
                break;
            }
            case Samurai: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(8, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 8, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 8);
                        break;
                    }
                }
                reward = 8;
                break;
            }
            case Scholar: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(1, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 1, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 1);
                        break;
                    }
                }
                reward = 2;
                break;
            }
            case Emissary: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(6, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 6, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 6);
                        break;
                    }
                }
                reward = 5;
                break;
            }
            case Merchant: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(4, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 4, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 4);
                        break;
                    }
                }
                reward = 4;
                break;
            }
            case Sumo_Wrestler: {
                switch (color) {
                    case RED: {
                        bonus = new Reputation(1, 0, 0);
                        req =  new Reputation(5, 0, 0);
                        break;
                    }
                    case BLUE: {
                        bonus = new Reputation(0, 1, 0);
                        req =  new Reputation(0, 5, 0);
                        break;
                    }
                    case GREEN: {
                        bonus = new Reputation(0, 0, 1);
                        req =  new Reputation(0, 0, 5);
                        break;
                    }
                }
                reward = 3;
                break;
            }
            case District_Kanryou: {
                bonus = new Reputation(1, 1, 1);
                req =  new Reputation(0, 0, 0, 0);
                reward = 0;
                break;
            }
        }
    }

    /** Copy constructor */
    Card (Card another) {
        this(another.name, another.color);
        is_known = another.is_known;
    }

    Name getName() { return name; }
    Color getColor() { return color; }

    int getReward () { return reward; }
    Reputation getBonus () { return bonus; }
    Reputation getReq ()  { return req; }

    boolean equals (Card another) {
        return another != null && name == another.getName() &&
                color ==  another.getColor();
    }

    public String toString () {
        return getName().toString() + " " + getColor().toString();
    }

}
