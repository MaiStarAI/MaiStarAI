public class Backpropagation {

    public static void doBackpropagation(State startSelection, State endSelection, boolean win){
        while (endSelection!=startSelection){
            endSelection = endSelection.parent;
            endSelection.children.clear();
        }

        for (int i = 0; i < startSelection.parent.children.size(); i++) {
            startSelection.parent.children.get(i).availability += 1;
        }

        while (startSelection!=null){
            startSelection.visits += 1;
            if(win){
                startSelection.victories += 1;
            }
            startSelection = startSelection.parent;
        }
    }
}
