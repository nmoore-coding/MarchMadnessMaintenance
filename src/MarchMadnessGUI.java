import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.event.Event;

/**
 *  MarchMadnessGUI
 * 
 * this class contains the buttons the user interacts
 * with and controls the actions of other objects 
 *
 * @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {

    //all the gui elements
    private BorderPane root;
    private ToolBar toolBar;
    private ToolBar btoolBar;
    private Button simulate;
    private Button logout;
    private Button scoreBoardButton;
    private Button viewBracketButton;
    private Button resetButton;
    private Button finalizeButton;

    //added by Eliza
    private Button instructionsButton;
    
    //allows you to go directly to division selection screen
    private Button chooseDivButton;
  
    //initial bracket
    private  Bracket startingBracket;

    //reference to currently logged in bracket
    private Bracket selectedBracket;
    private Bracket simResultBracket;
    
    private ArrayList<Bracket> playerBrackets;
    private HashMap<String, Bracket> playerMap;
    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginPane;
    private TournamentInfo teamInfo;

    @Override
    public void start(Stage primaryStage) {
        //try to load all the files, if there is an error display it
        try{
            teamInfo=new TournamentInfo();
            startingBracket= new Bracket(TournamentInfo.loadStartingBracket());
            simResultBracket=new Bracket(TournamentInfo.loadStartingBracket());
        } catch (IOException ex) {
            showError(new Exception("Can't find "+ex.getMessage(),ex),true);
        }
        //deserialize stored brackets
        playerBrackets = loadBrackets();
        
        playerMap = new HashMap<>();
        addAllToMap();

        //the main layout container
        root = new BorderPane();
        scoreBoard= new ScoreBoardTable();
        table=scoreBoard.start();
        loginPane =createLogin();
        CreateToolBars();
        
        //display login screen
        login();
        
        setActions();
        root.setTop(toolBar);   
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setMaximized(true);

        primaryStage.setTitle("March Madness Bracket Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can login
     */
    private void simulate(){
        //cant login and restart prog after simulate
        simulate.setDisable(true);
        
       scoreBoardButton.setDisable(false);
       viewBracketButton.setDisable(false);
       
       teamInfo.simulate(simResultBracket);
       for(Bracket b:playerBrackets){
           scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
       }
        
        displayPane(table);
    }
    
    /**
     * Displays the login screen
     * 
     */
    private void login(){            
        logout.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        btoolBar.setDisable(true);
        instructionsButton.setDisable(false);
        displayPane(loginPane = createLogin());
    }
    
     /**
     * Displays the score board
     * 
     */
    private void scoreBoard(){
        displayPane(table);
    }
    
     /**
      * Displays Simulated Bracket
      * 
      */
    private void viewBracket(){
       selectedBracket=simResultBracket;
       bracketPane=new BracketPane(selectedBracket);
       GridPane full = bracketPane.getFullPane();
       full.setAlignment(Pos.CENTER);
       full.setDisable(true);
       displayPane(new ScrollPane(full));
    }
    
    /**
     * allows user to choose bracket
     * 
     */
   private void chooseBracket(){
        btoolBar.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

    }
    /**
     * resets current selected sub tree
     * for final4 reset Ro2 and winner
     */
    private void clear(){
        bracketPane.clear();
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);
        
    }
    
    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            int lastPane = bracketPane.getLastPane();
            selectedBracket=new Bracket(startingBracket);
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
            /**
             * puts the user chooseDivButton to the bracket section they were looking at prior to resetting
             * uses fireEvent to make a fake mouseclick on the bracket the user was last on
             * uses 5 as an out of bounds placeholder to show that the user has not yet been to a region bracket
             * Try not to get lost in the constructor
             */ 
            if(lastPane != 5){    
                Event.fireEvent(bracketPane.getButtons().get(lastPane), new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false,null));
            }
        }
    }
    
    private void finalizeBracket(){
       if(bracketPane.isComplete()){
           btoolBar.setDisable(true);
           bracketPane.setDisable(true);
           simulate.setDisable(false);
           logout.setDisable(false);
           //save the bracket along with account info
           serializeBracket(selectedBracket);
       } else {
            infoAlert("You can only finalize a bracket once it has been completed.");
            //go chooseDivButton to bracket section selection screen
            // bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
       }
       //bracketPane=new BracketPane(selectedBracket);
    }
    
    
    /**
     * displays element in the center of the screen
     * 
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }
    
    /**
     * Creates toolBar and buttons.
     * adds buttons to the toolbar and saves global references to them
     */
    private void CreateToolBars(){
        toolBar  = new ToolBar();
        btoolBar  = new ToolBar();

        logout=new Button("Logout");
        logout.getStyleClass().add("buttons");
        simulate=new Button("Simulate");
        simulate.getStyleClass().add("buttons");
        scoreBoardButton=new Button("ScoreBoard");
        scoreBoardButton.getStyleClass().add("buttons");
        viewBracketButton= new Button("View Simulated Bracket");
        viewBracketButton.getStyleClass().add("buttons");
//        clearButton=new Button("Clear");
//        clearButton.getStyleClass().add("buttons");
        resetButton=new Button("Reset");
        resetButton.getStyleClass().add("buttons");
        finalizeButton=new Button("Finalize");
        finalizeButton.getStyleClass().add("buttons");
        instructionsButton =new Button("Instructions");
        instructionsButton.getStyleClass().add("buttons");
        toolBar.getItems().addAll(
                createSpacer(),
                logout,
                simulate,
                scoreBoardButton,
                viewBracketButton,
                createSpacer()
        );
        btoolBar.getItems().addAll(
                createSpacer(),
//                clearButton,
                chooseDivButton =new Button("Choose Division"),
                resetButton,
                finalizeButton,
                instructionsButton,
                createSpacer()
        );
        chooseDivButton.getStyleClass().add("buttons");
    }
    
   /**
    * sets the actions for each button
    */
    private void setActions(){
        logout.setOnAction(e->login());
        simulate.setOnAction(e->simulate());
        scoreBoardButton.setOnAction(e->scoreBoard());
        viewBracketButton.setOnAction(e->viewBracket());
//        clearButton.setOnAction(e->clear());
        resetButton.setOnAction(e->reset());
        finalizeButton.setOnAction(e->finalizeBracket());
        instructionsButton.setOnAction(e->instructions());
        chooseDivButton.setOnAction(e->{
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        });
    }
    
    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(
                spacer,
                Priority.SOMETIMES
        );
        return spacer;
    }
    
    
    private GridPane createLogin(){
        
        
        /*
        LoginPane
        Sergio and Joao
         */

        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);

        Image title = new Image("march_madness_logo.png");
        ImageView titleView = new ImageView();
        titleView.setImage(title);
        titleView.setFitWidth(350);
        titleView.setPreserveRatio(true);
        loginPane.add(titleView, 0, 0, 2, 1);

//        Text welcomeMessage = new Text("March Madness Login");
//        welcomeMessage.getStyleClass().add("welcomeMessage");
//        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        enterUser.getStyleClass().add("input");
        loginPane.add(enterUser, 1, 1);

        Label password = new Label("Password:");
        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("input");
        loginPane.add(passwordField, 1, 2);

        Button signButton = new Button("Sign in/Create Account");
        signButton.getStyleClass().add("buttons");
        loginPane.add(signButton, 1, 4);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        loginPane.add(message, 1, 5);
        
        //added by Eliza
        Button loginIns = new Button("Instructions");
        loginIns.getStyleClass().add("buttons");
        loginIns.setOnAction(e->instructions());
        loginPane.add(loginIns,1,5);
        signButton.setOnAction(event -> {
            
            //Louis added logout button to be enabled after sign-in
            logout.setDisable(false);
            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();
            
            //fixes logout button appearing if user leaves fields blank and yells at the user!
            if(name.equals("") && playerPass.equals("")){
                logout.setDisable(true);
                infoAlert("Please enter a username and password");
            }
            //yells at the user to put in a name
            else if(name.equals("")){
                logout.setDisable(true);
                infoAlert("Please enter a username");
            }

            if (playerMap.get(name) != null) {
                //check password of user
                 
                Bracket tmpBracket = this.playerMap.get(name);
               
                String password1 = tmpBracket.getPassword();
                
                //yells at the user to have a password
                if(playerPass.equals("")){
                    logout.setDisable(true);
                    infoAlert("Please enter a password");
                }
                else if (Objects.equals(password1, playerPass)) {
                    // load bracket
                    selectedBracket=playerMap.get(name);
                    chooseBracket();
                }else{
                   infoAlert("The password you have entered is incorrect!");
                }

            } else {
                //check for empty fields
                if(!name.equals("")&&!playerPass.equals("")){
                    //create new bracket
                    Bracket tmpPlayerBracket = new Bracket(startingBracket, name);
                    playerBrackets.add(tmpPlayerBracket);
                    tmpPlayerBracket.setPassword(playerPass);

                    playerMap.put(name, tmpPlayerBracket);
                    selectedBracket = tmpPlayerBracket;
                    //alert user that an account has been created
                    infoAlert("No user with the Username \""  + name + "\" exists. A new account has been created.");
                    chooseBracket();
                }
            }
        });
        
        return loginPane;
    }
    
    /**
     * addAllToMap
     * adds all the brackets to the map for login
     */
    private void addAllToMap(){
        for(Bracket b:playerBrackets){
            playerMap.put(b.getPlayerName(), b);   
        }
    }
    
    /**
     * The Exception handler
     * Displays a error message to the user
     * and if the error is bad enough closes the program
     * @param e Exception
     * @param fatal true if the program should exit. false otherwise
     */
    private void showError(Exception e,boolean fatal){
        String msg=e.getMessage();
        if(fatal){
            msg=msg+" \n\nthe program will now close";
            //e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);   
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){ 
            System.exit(666);
        }   
    }
    
    /**
     * alerts user to the result of their actions in the login pane 
     * @param msg the message to be displayed to the user
     */
    private void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to reset the ENTIRE bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }
    
    
    /**
     * Tayon Watson 5/5
     * serializedBracket
     * @param B The bracket the is going to be serialized
     */
    private void serializeBracket(Bracket B){
        FileOutputStream outStream = null;
        ObjectOutputStream out = null;
    try 
    {
      outStream = new FileOutputStream(B.getPlayerName()+".ser");
      out = new ObjectOutputStream(outStream);
      out.writeObject(B);
      out.close();
    } 
    catch(IOException e)
    {
      // Grant osborn 5/6 hopefully this never happens 
      showError(new Exception("Error saving bracket \n"+e.getMessage(),e),false);
    }
    }
    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the serialized bracket file
     * @return deserialized bracket 
     */
    private Bracket deserializeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream = null;
        ObjectInputStream in = null;
    try 
    {
        inStream = new FileInputStream(filename);
        in = new ObjectInputStream(inStream);
        bracket = (Bracket) in.readObject();
        in.close();
    }catch (IOException | ClassNotFoundException e) {
      // Grant osborn 5/6 hopefully this never happens either
      showError(new Exception("Error loading bracket \n"+e.getMessage(),e),false);
    } 
    return bracket;
    }
    
      /**
     * Tayon Watson 5/5
     * deserializedBracket
     * @return deserialized bracket 
     */
    private ArrayList<Bracket> loadBrackets()
    {   
        ArrayList<Bracket> list=new ArrayList<Bracket>();
        File dir = new File(".");
        for (final File fileEntry : dir.listFiles()){
            String fileName = fileEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".")+1);
       
            if (extension.equals("ser")){
                list.add(deserializeBracket(fileName));
            }
        }
        return list;
    }
    /**
     * Eliza Doering 4/2019
     */
    private void instructions(){
        
        Text text = new Text();
        String str = "\tMarch Madness is a college basketball tournament consisting of 64 teams. First, choose a " +
                "division that you'd like to start filling out a bracket for or click Full to view the entire bracket. " +
                "When viewing a bracket, click on the teams that you predict will win each match and this will progress them " +
                "forward in the bracket. If you choose winners for each division then you still have to go to the full bracket " +
                "in order to choose the champion. Once finished, click 'finalize' to lock in your bracket. At this point, you can " +
                "logout of your account and login with a new user if you want to create multiple brackets. Once all user's brackets " +
                "have been finalized then the Simulate button can be clicked to simulate the tournament. You may then click " +
                "the Scoreboard button to see how many points you and any other users received. While playing, if you decide that " +
                "you want to clear the bracket that you are currently on then click the clear button. Have fun and good luck!";
        text.setText(str);
        text.setWrappingWidth(500);
        text.setStyle("-fx-font-family: \"Franklin Gothic Medium\"; -fx-font-size: 15px;");
        //added by Eliza
        TextFlow instructionsTxt = new TextFlow();
        instructionsTxt.setPadding(new Insets(20));
        instructionsTxt.setPrefSize(500, 300);
        instructionsTxt.getStyleClass().add("instructionsText");
        instructionsTxt.getChildren().addAll(text);

        Stage stage = new Stage();
        Scene scene = new Scene(instructionsTxt);
        stage.setTitle("Instructions");
        stage.setScene(scene);
        stage.show();
    }
       
}
