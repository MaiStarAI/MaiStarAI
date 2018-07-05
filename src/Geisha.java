class Geisha {

    enum Name {
        Natsumi, Suzune, Momiji, Harukaze, Oboro, Akenohoshi
    }

    private Name name;
    private Reputation rep; // Does not update
    private int max_usages; // How many times geisha's effect can be used

    /** Constructor for creation of the new geisha */
    Geisha (Name name) {
        this.name = name;
        switch (name) {
            case Natsumi: {
                rep = new Reputation(3, 5, 1);
                max_usages = 2;
                break;
            }
            case Suzune: {
                rep = new Reputation(2, 2, 2);
                max_usages = 1;
                break;
            }
            case Akenohoshi: {
                rep = new Reputation(3, 3, 3);
                max_usages = 1;
                break;
            }
            case Oboro: {
                rep =  new Reputation(5, 5, 5);
                max_usages = 0;
                break;
            }
            case Momiji: {
                rep = new Reputation(5, 1, 3);
                max_usages = 1;
                break;
            }
            case Harukaze: {
                rep = new Reputation(1, 3, 5);
                max_usages = 1;
                break;
            }
        }
    }

    Name getName() {
        return name;
    }

    int getMaxUsages () { return max_usages; }

    int getRed () { return rep.getRed(); }
    int getBlue () { return rep.getBlue(); }
    int getGreen () { return rep.getGreen(); }

}
