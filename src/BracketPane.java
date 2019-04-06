import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.layout.Region;

import javafx.scene.control.Tooltip;


/**
 * Created by Richard and Ricardo on 5/3/17.
 */
public class BracketPane extends BorderPane {

        /**
         * Reference to the graphical representation of the nodes within the bracket.
         */
        private static ArrayList<BracketNode> nodes;
        /**
         * Used to initiate the paint of the bracket nodes
         */
        private static boolean isTop = true;
        /**
         * Maps the text "buttons" to it's respective grid-pane
         */
        private HashMap<StackPane, Pane> panes;
        /**
         * Reference to the current bracket.
         */
        private Bracket currentBracket;
        /**
         * Reference to active subtree within current bracket.
         */
        private int displayedSubtree;
        /**
         * Keeps track of whether or not bracket has been finalized.
         */
        private boolean finalized;
        /**
         * Important logical simplification for allowing for code that is easier
         * to maintain.
         */
        private HashMap<BracketNode, Integer> bracketMap = new HashMap<>();
        /**
         * Reverse of the above;
         */
        private HashMap<Integer, BracketNode> nodeMap = new HashMap<>();

        /**
         * Clears the entries of a team future wins
         *
         * @param treeNum
         */
        private void clearAbove(int treeNum) {
                int nextTreeNum = (treeNum - 1) / 2;
                if (!nodeMap.get(nextTreeNum).getName().isEmpty()) {
                        nodeMap.get(nextTreeNum).setName("");
                        clearAbove(nextTreeNum);
                }
        }
        
        
        public void clear(){
            clearSubtree(displayedSubtree);
        }

        /**
         * Handles clicked events for BracketNode objects
         */
        private EventHandler<MouseEvent> clicked = mouseEvent -> {
                //conditional added by matt 5/7 to differentiate between left and right mouse click
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        int nextTreeNum = (treeNum - 1) / 2;
                        if (!nodeMap.get(nextTreeNum).getName().equals(n.getName())) {
                                currentBracket.removeAbove((nextTreeNum));
                                clearAbove(treeNum);
                                nodeMap.get((bracketMap.get(n) - 1) / 2).setName(n.getName());
                                currentBracket.moveTeamUp(treeNum);
                        }
                }
                //added by matt 5/7, shows the teams info if you right click
                
        };
        /**
         * Handles mouseEntered events for BracketNode objects
         */
        private EventHandler<MouseEvent> enter = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();
                tmp.setStyle("-fx-background-color: lightcyan;");
                tmp.setEffect(new InnerShadow(10, Color.LIGHTCYAN));
                String text = "";
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        String teamName = currentBracket.getBracket().get(treeNum);
                        try {
                                TournamentInfo info = new TournamentInfo();
                                Team t = info.getTeam(teamName);
                                //by Tyler - added the last two pieces of info to the pop up window
                                text += "Team: " + teamName + " | Ranking: " + t.getRanking() + "\nMascot: " + t.getNickname() + "\nInfo: " + t.getInfo() + "\nAverage Offensive PPG: " + t.getOffensePPG() + "\nAverage Defensive PPG: "+ t.getDefensePPG();
                        } catch (IOException | NullPointerException e) {//if for some reason TournamentInfo isnt working, it will display info not found
                                text += "Info for " + teamName + "not found";
                        }
                        /**
                        *Max Hernandez 4/5/19
                        * creates a tooltip window when the mouse is hovered over a bracketnode
                        */
                        Tooltip teamInfo = new Tooltip(text);
                        Tooltip.install(n, teamInfo);
        };

        /**
         * Handles mouseExited events for BracketNode objects
         */
        private EventHandler<MouseEvent> exit = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();
                tmp.setStyle(null);
                tmp.setEffect(null);

        };

        public GridPane getFullPane() {
                return fullPane;
        }

        private GridPane center;
        private GridPane fullPane;


        /**
         * TODO: Reduce. reuse, recycle!
         * Initializes the properties needed to construct a bracket.
         */
        public BracketPane(Bracket currentBracket) {
                displayedSubtree=0;
                this.currentBracket = currentBracket;

                bracketMap = new HashMap<>();
                nodeMap = new HashMap<>();
                panes = new HashMap<>();
                nodes = new ArrayList<>();
                ArrayList<Root> roots = new ArrayList<>();

                center = new GridPane();

                ArrayList<StackPane> buttons = new ArrayList<>();
                buttons.add(customButton("EAST"));
                buttons.add(customButton("WEST"));
                buttons.add(customButton("MIDWEST"));
                buttons.add(customButton("SOUTH"));
                buttons.add(customButton("FULL"));

                ArrayList<GridPane> gridPanes = new ArrayList<>();

                for (int m = 0; m < buttons.size() - 1; m++) {
                        roots.add(new Root(3 + m));
                        panes.put(buttons.get(m), roots.get(m));
                }
                
                             
                Pane finalPane = createFinalFour();
                //buttons.add(customButton("FINAL"));
                //panes.put(buttons.get(5), finalPane);
                fullPane = new GridPane();
                GridPane gp1 = new GridPane();
                
                gp1.add(levels(), 0,0);
                gp1.add(roots.get(0), 0, 1);
                gp1.add(roots.get(1), 0, 2);
                GridPane gp2 = new GridPane();
                gp2.add(levels(), 0, 0);
                gp2.add(roots.get(2), 0, 1);
                gp2.add(roots.get(3), 0, 2);
                gp2.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                fullPane.add(gp1, 0, 0);
                fullPane.add(finalPane, 1, 0, 1, 2);
                fullPane.add(gp2, 2, 0);
                fullPane.setAlignment(Pos.CENTER);
                panes.put(buttons.get((buttons.size() - 1)), fullPane);
                finalPane.toBack();

                // Initializes the button grid
                GridPane buttonGrid = new GridPane();
                for (int i = 0; i < buttons.size(); i++)
                        buttonGrid.add(buttons.get(i), 0, i);
                buttonGrid.setAlignment(Pos.CENTER);

                // set default center to the button grid
                this.setCenter(buttonGrid);

                for (StackPane t : buttons) {
                        t.setOnMouseEntered(mouseEvent -> {
                                t.setStyle("-fx-background-color: lightblue;");
                                t.setEffect(new InnerShadow(10, Color.LIGHTCYAN));
                        });
                        t.setOnMouseExited(mouseEvent -> {
                                t.setStyle("-fx-background-color: orange;");
                                t.setEffect(null);
                        });
                        t.setOnMouseClicked(mouseEvent -> {
                                setCenter(null);
                                /**
                                 * @update Grant & Tyler 
                                 * 			panes are added as ScrollPanes to retain center alignment when moving through full-view and region-view
                                 */
                                /**
                                 * @update Max Hernandez
                                 * added if statement to check if the button is full bracket
                                 * if not adds the levels to the region bracket
                                 */
                                if(buttons.size()-1!= buttons.indexOf(t)){
                                    center.add(levels(), 0,0);
                                }
                                center.add(new ScrollPane(panes.get(t)), 0, 1);
                                center.setAlignment(Pos.CENTER);
                                setCenter(center);
                                //Grant 5/7 this is for clearing the tree it kind of works 
                                displayedSubtree=buttons.indexOf(t)==7?0:buttons.indexOf(t)+3;
                        });
                }

        }

        /**
         * Helpful method to retrieve our magical numbers
         *
         * @param root the root node (3,4,5,6)
         * @param pos  the position in the tree (8 (16) , 4 (8) , 2 (4) , 1 (2))
         * @return The list representing the valid values.
         */
        public ArrayList<Integer> helper(int root, int pos) {
                ArrayList<Integer> positions = new ArrayList<>();
                int base = 0;
                int tmp = (root * 2) + 1;
                if (pos == 8) base = 3;
                else if (pos == 4) base = 2;
                else if (pos == 2) base = 1;
                for (int i = 0; i < base; i++) tmp = (tmp * 2) + 1;
                for (int j = 0; j < pos * 2; j++) positions.add(tmp + j);
                return positions; //                while ((tmp = ((location * 2) + 1)) <= 127) ;
        }

        /**
         * Sets the current bracket to,
         *
         * @param target The bracket to replace currentBracket
         */
        public void setBracket(Bracket target) {
                currentBracket = target;
        }

        /**
         * Clears the sub tree from,
         *
         * @param position The position to clear after
         */
        public void clearSubtree(int position) {
                currentBracket.resetSubtree(position);
        }

        /**
         * Resets the bracket-display
         */
        public void resetBracket() {
                currentBracket.resetSubtree(0);
        }

        /**
         * Requests a message from current bracket to tell if the bracket
         * has been completed.
         *
         * @return True if completed, false otherwise.
         */
        public boolean isComplete() {
                return currentBracket.isComplete();
        }

        /**
         * @return true if the current-bracket is complete and the value
         * of finalized is also true.
         */
        public boolean isFinalized() {
                return currentBracket.isComplete() && finalized;
        }

        /**
         * @param isFinalized The value to set finalized to.
         */
        public void setFinalized(boolean isFinalized) {
                finalized = isFinalized && currentBracket.isComplete();
        }

        /**
         * Returns a custom "Button" with specified
         *
         * @param name The name of the button
         * @return pane The stack-pane "button"
         */
        private StackPane customButton(String name) {
                StackPane pane = new StackPane();
                Rectangle r = new Rectangle(100, 50, Color.TRANSPARENT);
                Text t = new Text(name);
                t.setTextAlignment(TextAlignment.CENTER);
                pane.getChildren().addAll(r, t);
                pane.setStyle("-fx-background-color: orange;");
                return pane;
        }
        /**
         * Max Hernandez 4/5/19
         * creates the final 4 pane 
         * changed the mouse enter method so that the tooltip window would appear
         * also added lines for visibility
         * and the bracket level tag
         * @return pane 
         */
        public Pane createFinalFour() {
                Pane finalPane = new Pane();
                //creates the nodes
                BracketNode nodeFinal0 = new BracketNode("", 162, 300, 70, 0);
                BracketNode nodeFinal1 = new BracketNode("", 75, 400, 70, 0);
                BracketNode nodeFinal2 = new BracketNode("", 250, 400, 70, 0);
                //sets the teams for each node
                nodeFinal0.setName(currentBracket.getBracket().get(0));
                nodeFinal1.setName(currentBracket.getBracket().get(1));
                nodeFinal2.setName(currentBracket.getBracket().get(2));
                //sets the actions for each node
                nodeFinal0.setOnMouseClicked(clicked);
                nodeFinal0.setOnMouseEntered(enter);
                nodeFinal0.setOnMouseExited(exit);

                nodeFinal1.setOnMouseClicked(clicked);
                nodeFinal1.setOnMouseEntered(enter);
                nodeFinal1.setOnMouseExited(exit);

                nodeFinal2.setOnMouseClicked(clicked);
                nodeFinal2.setOnMouseEntered(enter);
                nodeFinal2.setOnMouseExited(exit);
                //adds the nodes to the pane
                finalPane.getChildren().add(nodeFinal0);
                finalPane.getChildren().add(nodeFinal1);
                finalPane.getChildren().add(nodeFinal2);
                bracketMap.put(nodeFinal1, 1);
                bracketMap.put(nodeFinal2, 2);
                bracketMap.put(nodeFinal0, 0);
                nodeMap.put(1, nodeFinal1);
                nodeMap.put(2, nodeFinal2);
                nodeMap.put(0, nodeFinal0);

                
                Line line0 = new Line(162,320,232,320);
                Line line1 = new Line(75,420,145,420);
                Line line2 = new Line(250,420,320,420);
                Line bracket1 = new Line(145,420,145,380);
                Line bracket2 = new Line(250,420,250,380);
                Line bracket3 = new Line(145,380,250,380);
                Line bracket4 = new Line(197,380,197,320);
                finalPane.getChildren().add(line0);
                finalPane.getChildren().add(line1);
                finalPane.getChildren().add(line2);
                finalPane.getChildren().add(bracket1);
                finalPane.getChildren().add(bracket2);
                finalPane.getChildren().add(bracket3);
                finalPane.getChildren().add(bracket4);
                Text tag = new Text("Final");
                tag.setX(176);
                tag.setY(200);
                finalPane.getChildren().add(tag);
                finalPane.setMinWidth(400.0);

                return finalPane;
        }

        /**
         * Creates the graphical representation of a subtree.
         * Note, this is a vague model. TODO: MAKE MODULAR
         */
        private class Root extends Pane {

                private int location;

                public Root(int location) {
                        this.location = location;
                        createVertices(420, 200, 100, 20, 0, 0);
                        createVertices(320, 119, 100, 200, 1, 0);
                        createVertices(220, 60, 100, 100, 2, 200);
                        createVertices(120, 35, 100, 50, 4, 100);
                        createVertices(20, 25, 100, 25, 8, 50);
                        for (BracketNode n : nodes) {
                                n.setOnMouseClicked(clicked);
                                n.setOnMouseEntered(enter);
                                n.setOnMouseExited(exit);
                        }
                }

                /**
                 * The secret sauce... well not really,
                 * Creates 3 lines in appropriate location unless it is the last line.
                 * Adds these lines and "BracketNodes" to the Pane of this inner class
                 */
                private void createVertices(int iX, int iY, int iXO, int iYO, int num, int increment) {
                        int y = iY;
                        if (num == 0 && increment == 0) {
                                BracketNode last = new BracketNode("", iX, y - 20, iXO, 20);
                                nodes.add(last);
                                getChildren().addAll(new Line(iX, iY, iX + iXO, iY), last);
                                last.setName(currentBracket.getBracket().get(location));
                                bracketMap.put(last, location);
                                nodeMap.put(location, last);
                        } else {
                                ArrayList<BracketNode> aNodeList = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                        Point2D tl = new Point2D(iX, y);
                                        Point2D tr = new Point2D(iX + iXO, y);
                                        Point2D bl = new Point2D(iX, y + iYO);
                                        Point2D br = new Point2D(iX + iXO, y + iYO);
                                        BracketNode nTop = new BracketNode("", iX, y - 20, iXO, 20);
                                        aNodeList.add(nTop);
                                        nodes.add(nTop);
                                        BracketNode nBottom = new BracketNode("", iX, y + (iYO - 20), iXO, 20);
                                        aNodeList.add(nBottom);
                                        nodes.add(nBottom);
                                        Line top = new Line(tl.getX(), tl.getY(), tr.getX(), tr.getY());
                                        Line bottom = new Line(bl.getX(), bl.getY(), br.getX(), br.getY());
                                        Line right = new Line(tr.getX(), tr.getY(), br.getX(), br.getY());
                                        getChildren().addAll(top, bottom, right, nTop, nBottom);
                                        isTop = !isTop;
                                        y += increment;
                                }
                                ArrayList<Integer> tmpHelp = helper(location, num);
                                for (int j = 0; j < aNodeList.size(); j++) {
                                        //System.out.println(currentBracket.getBracket().get(tmpHelp.get(j)));
                                        aNodeList.get(j).setName(currentBracket.getBracket().get(tmpHelp.get(j)));
                                        bracketMap.put(aNodeList.get(j), tmpHelp.get(j));
                                        nodeMap.put(tmpHelp.get(j), aNodeList.get(j));
                                        //System.out.println(bracketMap.get(aNodeList.get(j)));
                                }
                        }

                }
        }
        /**
         * Max Hernandez 4/5/19
         * creates an HBox of the bracket level ID
         * @return levels 
         */
        private HBox levels(){
             String first = "1st round";
             String second = "2nd Round";
             String sweet16 = "Sweet 16";
             String eight = "Elite Eight";
             String four =" Final Four";
             
             HBox levels = new HBox();
             levels.setPadding(new Insets(0,0,0,22));
             levels.setSpacing(55);
             levels.getChildren().add(new Text(first));
             levels.getChildren().add(new Text(second));
             levels.getChildren().add(new Text(sweet16));
             levels.getChildren().add(new Text(eight));
             levels.getChildren().add(new Text(four));
             return levels;
        }
        
        
        /**
         * The BracketNode model for the Graphical display of the "Bracket"
         */
        private class BracketNode extends Pane {
                private String teamName;
                private Rectangle rect;
                private Label name;

                /**
                 * Creates a BracketNode with,
                 *
                 * @param teamName The name if any
                 * @param x        The starting x location
                 * @param y        The starting y location
                 * @param rX       The width of the rectangle to fill pane
                 * @param rY       The height of the rectangle
                 */
                public BracketNode(String teamName, int x, int y, int rX, int rY) {
                        this.setLayoutX(x);
                        this.setLayoutY(y);
                        this.setMaxSize(rX, rY);
                        this.teamName = teamName;
                        rect = new Rectangle(rX, rY);
                        rect.setFill(Color.TRANSPARENT);
                        name = new Label(teamName);
                        // setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        name.setTranslateX(5);
                        getChildren().addAll(name, rect);
                }

                /**
                 * @return teamName The teams name.
                 */
                public String getName() {
                        return teamName;
                }

                /**
                 * @param teamName The name to assign to the node.
                 */
                public void setName(String teamName) {
                        this.teamName = teamName;
                        name.setText(teamName);
                }
        }
}
