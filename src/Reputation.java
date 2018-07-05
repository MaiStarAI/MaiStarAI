class Reputation {

    private int r_value;
    private int b_value;
    private int g_value;
    private int black_value;

    Reputation(int r_value, int b_value, int g_value) {
        this.r_value = r_value;
        this.b_value = b_value;
        this.g_value = g_value;
        black_value = -1;
    }

    Reputation(int r_value, int b_value, int g_value, int black_value) {
        this.r_value = r_value;
        this.b_value = b_value;
        this.g_value = g_value;
        this.black_value = black_value;
    }

    int getRed() { return r_value; }
    int getBlue() { return b_value; }
    int getGreen() { return g_value; }
    int getBlack() { return black_value; }

    boolean equals (Reputation another) {
        return another != null &&
                r_value == another.r_value &&
                b_value == another.b_value &&
                g_value == another.g_value &&
                black_value == another.black_value;
    }
    public String toString () {
        return "Red: " + r_value + ", Blue: " + b_value + ", Green: " + g_value + ", Black: " + black_value;
    }
}