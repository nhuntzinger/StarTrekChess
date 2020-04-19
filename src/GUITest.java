import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GUITest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    Game myGame;

    @Override
    public void start(Stage primaryStage) {

        AtomicInteger currentLevel = new AtomicInteger(6);
        primaryStage.setTitle("Star Trek Chess");
        this.myGame = new Game();

        AtomicReference<BoardPosition> moveFrom = new AtomicReference<>(new BoardPosition());
        AtomicReference<BoardPosition> moveTo = new AtomicReference<>(new BoardPosition());
        AtomicReference<Boolean> pieceSelected = new AtomicReference<>(false);
        AtomicReference<Boolean> boardSelected = new AtomicReference<>(false);
        AtomicReference<Boolean> selectMovableBoard = new AtomicReference<>(false);
        AtomicReference<MovableBoardPosition> boardMoveFrom = new AtomicReference<>(new MovableBoardPosition());
        AtomicReference<MovableBoardPosition> boardMoveTo = new AtomicReference<>(new MovableBoardPosition());

        StackPane gameBoardStack = new StackPane();
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(gameBoardStack);

        VBox chatPane = setUpChat();
        mainPane.setRight(chatPane);

        ArrayList<GridPane> levels = new ArrayList<>();
        for (int i = 0; i < 7; i++){
            levels.add(new GridPane());
            gameBoardStack.getChildren().add(levels.get(i));
        }

        BackgroundImage myBI= new BackgroundImage(new Image(GUITest.class.getResourceAsStream("background.jpg"), 2000, 800, true, false),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        mainPane.setBackground(new Background(myBI));

        Image pawn_w_image = new Image(GUITest.class.getResourceAsStream("pawn_w.png"), 50, 50, false, false);
        Image pawn_b_image = new Image(GUITest.class.getResourceAsStream("pawn_b.png"), 50, 50, false, false);
        Image rook_w_image = new Image(GUITest.class.getResourceAsStream("rook_w.png"), 50, 50, false, false);
        Image rook_b_image = new Image(GUITest.class.getResourceAsStream("rook_b.png"), 50, 50, false, false);
        Image knight_w_image = new Image(GUITest.class.getResourceAsStream("knight_w.png"), 50, 50, false, false);
        Image knight_b_image = new Image(GUITest.class.getResourceAsStream("knight_b.png"), 50, 50, false, false);
        Image bishop_w_image = new Image(GUITest.class.getResourceAsStream("bishop_w.png"), 50, 50, false, false);
        Image bishop_b_image = new Image(GUITest.class.getResourceAsStream("bishop_b.png"), 50, 50, false, false);
        Image queen_w_image = new Image(GUITest.class.getResourceAsStream("queen_w.png"), 50, 50, false, false);
        Image queen_b_image = new Image(GUITest.class.getResourceAsStream("queen_b.png"), 50, 50, false, false);
        Image king_w_image = new Image(GUITest.class.getResourceAsStream("king_w.png"), 50, 50, false, false);
        Image king_b_image = new Image(GUITest.class.getResourceAsStream("king_b.png"), 50, 50, false, false);

        ArrayList<Image> images = new ArrayList<>();
        images.add(pawn_w_image);
        images.add(rook_w_image);
        images.add(knight_w_image);
        images.add(bishop_w_image);
        images.add(queen_w_image);
        images.add(king_w_image);
        images.add(pawn_b_image);
        images.add(rook_b_image);
        images.add(knight_b_image);
        images.add(bishop_b_image);
        images.add(queen_b_image);
        images.add(king_b_image);

        ArrayList<ArrayList<ArrayList<BoardPosition>>> board = myGame.getGameBoard().getBoard();

        //Internal class
        class BoardSquare extends Rectangle {
            int row;
            int col;

            BoardSquare(int width, int height, int row, int col){
                super(width, height);
                this.row = row;
                this.col = col;

                this.setOnMouseClicked(e->{
                    System.out.println("Space: (" + this.row + ", " + this.col + ") at level: " + currentLevel.get() + " was clicked");
                    if(selectMovableBoard.get() == false) {
                        if (pieceSelected.get() == false) {
                            if (myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get()).getPiece() != null) {
                                BoardPosition pos = myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get());
                                System.out.println("A piece was selected!");
                                pieceSelected.set(true);
                                moveFrom.set(pos);
                                ArrayList<BoardPosition> possibleMoves = myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get()).getPiece().getMoves();
                                for (BoardPosition move : possibleMoves){
                                    System.out.println(move.toString());
                                    StackPane stack = (StackPane) getNodeFromGridPane(levels.get(move.getLevel()), move.getCol(), move.getRow());
                                    Rectangle rect = (Rectangle) stack.getChildren().get(0);
                                    if(move.getPiece() != null){
                                        rect.setFill(Color.RED);
                                    }
                                    else {
                                        rect.setFill(Color.BLUE);
                                    }
                                }
                            }
                        } else {
                            if (myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get()).isValidSpace()) {
                                moveTo.set(myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get()));
                                Move move = new Move(moveFrom.get(), moveTo.get());
                                myGame.executeMove(move);
                                client.toServer(move);
                                pieceSelected.set(false);
                                System.out.println("A piece was moved");
                                updateBoard(myGame, board, levels, images);
                            }
                        }
                    }
                    else{
                        if(boardSelected.get() == false){
                            System.out.println("Selecting board to move");
                            for(int i = 0; i < myGame.getGameBoard().getMovableBoardPositions().size(); i++){
                                ArrayList<ArrayList<BoardPosition>> section = myGame.getGameBoard().getMovableBoardPositions().get(i).getBoardSection();
                                for(int r = 0; r < section.size(); r ++){
                                    for(int c = 0; c < section.get(r).size(); c++){
                                        if(section.get(r).get(c) == myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get()) && myGame.getGameBoard().getMovableBoardPositions().get(i).getMovableBoard() != null){
                                            boardMoveFrom.set(myGame.getGameBoard().getMovableBoardPositions().get(i));
                                            boardSelected.set(true);
                                            System.out.println("A board was selected");
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            for(int i = 0; i < myGame.getGameBoard().getMovableBoardPositions().size(); i++){
                                ArrayList<ArrayList<BoardPosition>> section = myGame.getGameBoard().getMovableBoardPositions().get(i).getBoardSection();
                                for(int r = 0; r < section.size(); r ++){
                                    for(int c = 0; c < section.get(r).size(); c++){
                                        if(section.get(r).get(c) == myGame.getGameBoard().getPosition(this.row, this.col, currentLevel.get())){
                                            boardMoveTo.set(myGame.getGameBoard().getMovableBoardPositions().get(i));
                                            myGame.executeMovableBoardMove(new MovableBoardMove(boardMoveFrom.get(), boardMoveTo.get()));
                                            boardSelected.set(false);
                                            selectMovableBoard.set(false);
                                            System.out.println("A board was moved");
                                            updateBoard(myGame, board, levels, images);
                                        }
                                    }
                                }
                            }

                        }
                    }
                });
            }

            public int getRow() {
                return row;
            }

            public int getCol() {
                return col;
            }
        }

        //first initializes the board.
        for (int r = 0; r < board.size(); r++) {
            for (int c = 0; c < board.get(r).size(); c++) {
                for (int l = 0; l < board.get(r).get(c).size(); l++) {
                    Rectangle rect = new Rectangle(50,50);
                    if(!myGame.getGameBoard().getPosition(r,c,l).isValidSpace()){
                        rect.setFill(Color.TRANSPARENT);
                    }
                    else{
                        if((r % 2 == 0 && c % 2 == 0) || (r % 2 == 1 && c % 2 == 1)){
                            rect.setFill(Color.BLACK);
                            rect.setStroke(Color.BLACK);
                        }
                        else{
                            rect.setFill(Color.WHITE);
                            rect.setStroke(Color.BLACK);
                        }
                    }
                    levels.get(l).add(new StackPane(rect), c, r);
                    StackPane stack = (StackPane) getNodeFromGridPane(levels.get(l), c, r);
                    if(myGame.getGameBoard().getPosition(r,c,l).getPiece() != null && myGame.getGameBoard().getPosition(r,c,l).getPiece().getPlayer().getPlayerNumber() == 1) {
                        if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Pawn) {
                            ImageView pawn_w = new ImageView();
                            pawn_w.setImage(pawn_w_image);
                            stack.getChildren().add(1, pawn_w);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Rook) {
                            ImageView rook_w = new ImageView();
                            rook_w.setImage(rook_w_image);
                            stack.getChildren().add(1, rook_w);
                        }
                        else if(myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Knight){
                            ImageView knight_w = new ImageView();
                            knight_w.setImage(knight_w_image);
                            stack.getChildren().add(1, knight_w);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Bishop) {
                            ImageView bishop_w = new ImageView();
                            bishop_w.setImage(bishop_w_image);
                            stack.getChildren().add(1, bishop_w);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Queen) {
                            ImageView queen_w = new ImageView();
                            queen_w.setImage(queen_w_image);
                            stack.getChildren().add(1, queen_w);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof King) {
                            ImageView king_w = new ImageView();
                            king_w.setImage(king_w_image);
                            stack.getChildren().add(1, king_w);
                        }
                    }
                    else if(myGame.getGameBoard().getPosition(r,c,l).getPiece() != null && myGame.getGameBoard().getPosition(r,c,l).getPiece().getPlayer().getPlayerNumber() == 2){
                        if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Pawn) {
                            ImageView pawn_b = new ImageView();
                            pawn_b.setImage(pawn_b_image);
                            stack.getChildren().add(1, pawn_b);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Rook) {
                            ImageView rook_b = new ImageView();
                            rook_b.setImage(rook_b_image);
                            stack.getChildren().add(1, rook_b);
                        }
                        else if(myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Knight){
                            ImageView knight_b = new ImageView();
                            knight_b.setImage(knight_b_image);
                            stack.getChildren().add(1, knight_b);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Bishop) {
                            ImageView bishop_b = new ImageView();
                            bishop_b.setImage(bishop_b_image);
                            stack.getChildren().add(1, bishop_b);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Queen) {
                            ImageView queen_b = new ImageView();
                            queen_b.setImage(queen_b_image);
                            stack.getChildren().add(1, queen_b);
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof King) {
                            ImageView king_b = new ImageView();
                            king_b.setImage(king_b_image);
                            stack.getChildren().add(1, king_b);
                        }
                    }
                    else {
                        ImageView piece = new ImageView();
                        piece.setImage(null);
//                        piece.setFill(Color.TRANSPARENT);
//                        piece.setStroke(Color.TRANSPARENT);
                        stack.getChildren().add(1, piece);
                    }

                    BoardSquare clickableSquare = new BoardSquare(50,50,r,c);
                    clickableSquare.setFill(Color.TRANSPARENT);
                    stack.getChildren().add(2,clickableSquare);
                }
            }
        }

        Scene scene = new Scene(mainPane, 600, 600);

        scene.setOnKeyTyped(e->{
            Character levelChar = e.getCharacter().charAt(0);
            if(levelChar == '`'){
                levelChar = '0';
            }
            for (int r = 0; r < board.size(); r++) {
                for (int c = 0; c < board.get(r).size(); c++) {
                    for (int l = 0; l < board.get(r).get(c).size(); l++) {
                        if(Character.isDigit(levelChar)) {
                            StackPane stack = (StackPane) getNodeFromGridPane(levels.get(l), c, r);
                            Rectangle rect = (Rectangle) stack.getChildren().get(0);
                            Node piece = (Node) stack.getChildren().get(1);

                            if(levelChar == '7'){
                                currentLevel.set(6);
                                rect.setOpacity(1);
                                piece.setOpacity(1);
                            }
                            else if (Character.getNumericValue(levelChar) != l) {
                                rect.setOpacity(0.15);
                                piece.setOpacity(0.15);
                            }

                            else{
                                currentLevel.set(l);
                                rect.setOpacity(1);
                                piece.setOpacity(1);
                            }
                        }

                    }
                }
            }
        });

        scene.setOnKeyPressed(e->{
            if (e.getCode() == KeyCode.ESCAPE) {
                System.out.println("Deselecting");
                selectMovableBoard.set(false);
                boardSelected.set(false);
                pieceSelected.set(false);
                moveFrom.set(null);
                boardMoveFrom.set(null);
                updateBoard(myGame, board, levels, images);
            }
            else if (e.getCode() == KeyCode.SHIFT) {
                System.out.println("Entering board selection mode");
                selectMovableBoard.set(true);
            }
        });

        scene.setOnKeyReleased(e->{
            if (e.getCode() == KeyCode.SHIFT && boardSelected.get() == false) {
                System.out.println("Exiting board selection mode");
                selectMovableBoard.set(false);
            }
        });


        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void updateBoard(Game myGame, ArrayList<ArrayList<ArrayList<BoardPosition>>> board, ArrayList<GridPane> levels, ArrayList<Image> images){
        for (int r = 0; r < board.size(); r++) {
            for (int c = 0; c < board.get(r).size(); c++) {
                for (int l = 0; l < board.get(r).get(c).size(); l++) {
                    StackPane stack = (StackPane) getNodeFromGridPane(levels.get(l), c, r);
                    Rectangle rect = (Rectangle) stack.getChildren().get(0);

                    if(!myGame.getGameBoard().getPosition(r,c,l).isValidSpace()){
                        rect.setFill(Color.TRANSPARENT);
                        rect.setStroke(Color.TRANSPARENT);
                    }
                    else{
                        if((r % 2 == 0 && c % 2 == 0) || (r % 2 == 1 && c % 2 == 1)){
                            rect.setFill(Color.WHITE);
                            rect.setStroke(Color.BLACK);
                        }
                        else{
                            rect.setFill(Color.BLACK);
                            rect.setStroke(Color.BLACK);
                        }
                    }
                    ImageView piece = (ImageView) stack.getChildren().get(1);
                    if(myGame.getGameBoard().getPosition(r,c,l).getPiece() != null && myGame.getGameBoard().getPosition(r,c,l).getPiece().getPlayer().getPlayerNumber() == 1) {
                        if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Pawn) {
                            piece.setImage(images.get(0));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Rook) {
                            piece.setImage(images.get(1));
                        }
                        else if(myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Knight){
                            piece.setImage(images.get(2));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Bishop) {
                            piece.setImage(images.get(3));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Queen) {
                            piece.setImage(images.get(4));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof King) {
                            piece.setImage(images.get(5));
                        }
                    }
                    else if(myGame.getGameBoard().getPosition(r,c,l).getPiece() != null && myGame.getGameBoard().getPosition(r,c,l).getPiece().getPlayer().getPlayerNumber() == 2){
                        if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Pawn) {
                            piece.setImage(images.get(6));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Rook) {
                            piece.setImage(images.get(7));
                        }
                        else if(myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Knight){
                            piece.setImage(images.get(8));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Bishop) {
                            piece.setImage(images.get(9));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof Queen) {
                            piece.setImage(images.get(10));
                        }
                        else if (myGame.getGameBoard().getPosition(r, c, l).getPiece() instanceof King) {
                            piece.setImage(images.get(11));
                        }
                    }
                    else {
                        piece.setImage(null);
                    }
                }
            }
        }
    }

    /**
     * Chat Functions
     */
    private Text name;
    private VBox chat;
    private Client client;
    private String hostname;
    private int port;

    private VBox setUpChat() {
        VBox mainChatPane;
        mainChatPane = new VBox();
        this.chat = new VBox();
        HBox input = createChatInputPane();

        mainChatPane.setSpacing(10);
        mainChatPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        mainChatPane.setOpacity(.5);
        mainChatPane.setMinWidth(300);

        mainChatPane.getChildren().add(chat);
        mainChatPane.getChildren().add(input);
        mainChatPane.setAlignment(Pos.BOTTOM_CENTER);

        return mainChatPane;
    }

    private HBox createChatInputPane() {
        HBox inputPane = new HBox();
        inputPane.setSpacing(10);

        name = new Text("...");
        name.setFill(Color.WHITE);
        name.setFont(new Font("Ubuntu", 20));
        TextField chatInput = new TextField();
        Button sendButton = new Button("Send");

        inputPane.getChildren().add(name);
        inputPane.getChildren().add(chatInput);
        inputPane.getChildren().add(sendButton);

        this.toGUI(new ChatObj("System", "What is your name?"));

        chatInput.setOnKeyPressed(e -> { if (e.getCode().equals(KeyCode.ENTER)) { sendButton.fire(); } });
        sendButton.setOnAction(event -> {
            ChatObj newChat;
            if (this.client == null) {
                if (this.name.getText().equals("...")) {
                    this.name.setText(chatInput.getText());
                    chatInput.setText("");
                    this.toGUI(new ChatObj("System", "Setting your name to '" + name.getText() + "'"));
                    this.toGUI(new ChatObj("System", "What is the host IP adress?"));
                }
                else if (this.hostname == null) {
                    this.hostname = chatInput.getText();
                    chatInput.setText("");
                    this.toGUI(new ChatObj("System", "What is the host port?"));
                }
                else {
                    try {
                        this.port = Integer.parseInt(chatInput.getText().trim());
                        chatInput.setText("");
                        this.toGUI(new ChatObj("System", "Connecting to " + this.hostname + ":" + this.port));
                        this.client = new Client(this.hostname, this.port, this);
                        this.client.start();
                    }
                    catch (NumberFormatException ex) {
                        this.toGUI(new ChatObj("ERROR", "Invalid port number"));
                    }
                }
            }
            else {  // Send a message to the server
                newChat = new ChatObj(name.getText(), chatInput.getText());
                chatInput.setText("");
                this.toGUI(newChat);
                this.client.toServer(newChat);
            }
        });

        return inputPane;
    }

    public void toGUI(Object msg) {
        if (msg instanceof ChatObj) {
            Text text = new Text(msg.toString());
            text.setFont(new Font("Ubuntu", 16));
            text.setFill(Color.WHITE);
            this.chat.getChildren().add(text);
        }
        else if (msg instanceof Move) {
            myGame.executeMove((Move) msg);
            updateBoard();
        }
        else if (msg instanceof MovableBoardMove) {
            myGame.executeMovableBoardMove((MovableBoardMove) msg);
        }
        else {
            System.out.println("Invalid Object type received from server");
        }
    }
}
