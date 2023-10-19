package com.example.tictactoeclient3;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Klasa kontroluj�ca gr�.
 * ��czy graczy z serwerem. Przypisuje do gracza znak 'X' lub 'Y'.
 * Po klikni�ciu gracza w pole planszy wy�wietla odpowiedni obrazek w formie gif.
 *
 */

public class TicTacToeController implements Initializable {
    private String serverAddress = "192.168.1.17";
    private String serverPort = "9889";
    private final String IMAGE_O = "/ikonaO.gif";
    private final String IMAGE_O_WINNER = "/ikonaO_winner.gif";

    private final String IMAGE_X_WINNER = "/ikonaX_winner.gif";
    private final String IMAGE_X = "/ikonaX.gif";

    private final int USER_DATA_IMG_X = 1;
    private final int USER_DATA_IMG_O = 2;
    private final int USER_DATA_IMG_EMPTY = 0;
    private Socket clientSocket;
    private BufferedReader clientIn;
    private PrintWriter clientOut;


    private char playerSign;
    private boolean active;

    public static final char KOLKO = 'O';
    public static final char KRZY�YK = 'X';
    public static final char PUSTE = 'P';
    public static final char WINNER_X = 'W';
    public static final char WINNER_O = 'R';

    @FXML
    private Label playerSignLabel;
    @FXML
    private Label infoLabel;

    @FXML
    private Button field1;
    @FXML
    private Button field2;
    @FXML
    private Button field3;
    @FXML
    private Button field4;
    @FXML
    private Button field5;
    @FXML
    private Button field6;
    @FXML
    private Button field7;
    @FXML
    private Button field8;
    @FXML
    private Button field9;
    @FXML
    private Button exit;
    @FXML
    private Button newGame;


    private Button[][] gameButtons;

    /**
     Klasa zawieraj�ca warto�ci deklaruj�ce statusy proces�w przetwarzanych przez aplikacj� Klienta (Gracza)
     HELLO-przywitanie gracza
     START-rozpocz�cie gry
     MOVE- kolejny ruch
     WAIT-gracz czeka na sw�j ruch (jest wtedy nieaktywny)
     OK-informacja od gracza, �e przeczyta� komunikat od serwera
     WINNER-okre�la zwyci�zc� gry
     LOOSER-okre�la przegranego gracza
     NO_WINNER- gra jest nierozstrzygni�ta; nie ma zwyci�zcy
     NEW_GAME-u�ytkownik nacisn�� przycisk 'Nowa Gra'
     EXIT-u�ytkownik nacisn�� przycisk 'Wyjd�'
     */
    private enum actions {
        HELLO,
        START,
        MOVE,
        WAIT,
        OK,
        WINNER,
        LOSER,
        NO_WINNER,
        NEW_GAME,
        EXIT
    }

    @FXML
    protected void onStart() {
        deactivateAllControls();
        active = false;
    }

    private void activateButtons(boolean activate) {
        if (activate) {
            exit.setDisable(false);
            newGame.setDisable(false);
        } else {
            exit.setDisable(true);
            newGame.setDisable(true);
        }
    }

    private void deactivateAllControls() {
        Platform.runLater(() -> {
            activateButtons(false);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    gameButtons[i][j].setDisable(true);
                }
            }
        });
    }

    private void activateControls() {
        Platform.runLater(() -> {
            activateButtons(true);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    //System.out.println("activateControls i="+i+",j="+j);
                    if (gameButtons == null) System.out.println("gameButtons jest puste");
                    if (gameButtons[i] == null) System.out.println("gameButtons wiersz jest puste");
                    //TO DO zmieni� na obrazek
                    String sign = gameButtons[i][j].getText().trim();
                    switch (sign) {
                        case "X":
                        case "O":
                            gameButtons[i][j].setDisable(true);
                            break;
                        default:
                            gameButtons[i][j].setDisable(false);
                            break;
                    }
                }
            }
        });
    }

    /**
     * Metoda pobiera informacj� z planszy, kt�ry gracz wykona� ruch, jaki ruch oraz kt�re pola pozostaj� puste
     * @return zwraca plansz� gry: String znak�w P(pusty przycisk), X(gracz X), O(gracz O)
     */
    private String getMessageFromGameButtons() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button=gameButtons[i][j];
                if (button.getGraphic() == null) {
                    sb.append("P,");
                    System.out.println("+P");
                } else {
                    int userData = (int) button.getUserData();
                    System.out.println("USer data ("+i+","+j+"="+userData);
                    if (userData == 1) {
                        sb.append("X,");
                        System.out.println("+X");
                    } else if (userData == 2) {
                        sb.append("O,");
                        System.out.println("+O");
                    } else {
                        System.out.println("+NIC");
                    }
                }
            }
        }
        System.out.println("KLIENT " + this.playerSign + " plansza przygotowana do wys�ania " + sb);
        return sb.toString();
    }

    /**
     * Metoda ustawiaj�ca odpowiedni obrazek (X lub O) na przycisku.
     * W przypadku wygranej ustawia obrazek zmieniony na kolor czerwony.
     * @param button przycisk na planszy gry
     * @param image obrazek ze znakiem X lub O.
     * @param imageType ustawianie informacj�, kt�ry obrazek jest wstawiany
     * @param isWinningField Zmienna boolean. Je�li true to pole jest przycisk zwyci�ski,
     *   wtedy ustawiany jest inny obrazek na przycisku (w kolorze czerwonym).
     */
    private void setImageForButton(Button button, String image, Integer imageType,Boolean isWinningField) {
        Platform.runLater(() -> {
            button.setGraphic(new ImageView(new Image(image)));
            button.setUserData(imageType);
            if (isWinningField) {
                button.getStyleClass().add("winner-button");
                button.setStyle("-fx-border-color:#FF3131; -fx-border-width: 5px;");
            }
            else {
                button.getStyleClass().add("button");
                button.setStyle("-fx-border-color:  #00F5D4; -fx-border-width: 1px;");
            }
        });
    }

    /**
     * Metoda pomocnicza usuwaj�ca wszelkie znaki z planszy
     * @param button przycisk na planszy gry
     */
    private void clearImageForButton(Button button) {
        Platform.runLater(() -> {
            button.setText(" ");
            button.setUserData(USER_DATA_IMG_EMPTY);
            button.setGraphic(null);
            button.getStyleClass().add("button");
            button.setStyle("-fx-border-color: #00F5D4; -fx-border-width: 1px;");
        } );
    }

    private void getGameButtonsFromMessage(String board) {
        System.out.println("Klient dosta� plansz�: " + board);
        String[] splitBoard = board.split(",");
        if (splitBoard.length < 9) {
            throw new RuntimeException("Z�y komunikat");
        }
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int column = i % 3;
            char value = splitBoard[i].trim().charAt(0);
            System.out.println("KLIENT: board[" + i +"] = "+splitBoard[i]+" znak= "+value);
                switch (value) {
                    case KRZY�YK:
                        setImageForButton(this.gameButtons[row][column], IMAGE_X, USER_DATA_IMG_X,false);
                        System.out.println("KLIENT: rozpoznano znak X dla pola (" + row + "," + column + ")");
                        break;
                    case KOLKO:
                        setImageForButton(this.gameButtons[row][column], IMAGE_O,  USER_DATA_IMG_O,false);
                        System.out.println("KLIENT: rozpoznano znak O dla pola (" + row + "," + column + ")");
                        break;
                    case WINNER_O:
                        setImageForButton(this.gameButtons[row][column], IMAGE_O_WINNER, USER_DATA_IMG_O,true);
                        System.out.println("KLIENT: rozpoznano znak Winner O dla pola (" + row + "," + column + ")");
                        break;
                    case WINNER_X:
                        setImageForButton(this.gameButtons[row][column], IMAGE_X_WINNER, USER_DATA_IMG_X, true);
                        System.out.println("KLIENT: rozpoznano znak Winner X dla pola (" + row + "," + column + ")");
                        break;
                    case PUSTE:
                        clearImageForButton(this.gameButtons[row][column]);
                        System.out.println("KLIENT: rozpoznano znak PUSTE dla pola (" + row + "," + column + ")");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + value);
                }
        }
    }

    /**
     * Metoda inicjuj�ca po��czenie z serwerem.
     * @param url adres IP, przez kt�ry gracz (Klient) ��czy si� z serwerem
     * @param resourceBundle
     */
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getGameServerAddress();


        gameButtons = new Button[3][3];
        gameButtons[0][0] = field1;
        gameButtons[0][1] = field2;
        gameButtons[0][2] = field3;
        gameButtons[1][0] = field4;
        gameButtons[1][1] = field5;
        gameButtons[1][2] = field6;
        gameButtons[2][0] = field7;
        gameButtons[2][1] = field8;
        gameButtons[2][2] = field9;
        //inicjalizacja po��czenia z serwerem
        try {
            clientSocket = new Socket(serverAddress, Integer.parseInt(serverPort));
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("Klient po��czy� si� z serwerem i tworzymy w�tek dla klienta");
            Thread receiverThread = new Thread(this::controlMessages);
            receiverThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metoda zarz�dzaj�ca wiadomo�ciami w przypadku akcji zdefiniowanych w {@link actions}
     */
    private void controlMessages() {
        //Komunikaty przez run Later, bo z w�tku nie ma wyj�cia na konsol�
        try {
            String message;
            boolean isExit = false;
            while ((message = clientIn.readLine()) != null) {
                System.out.println("Klient dosta� wiadomo��:<" + message + ">");
                String[] msgs = message.split(" ");
                String action = msgs[0].trim();
                char player = msgs[1].trim().charAt(0);
                String board = null;
                if (msgs.length == 3) {
                    board = msgs[2].trim();
                }
                System.out.println("action=" + action + " player=" + player + " board=" + board);

                //obs�uga info od serwera
                switch (actions.valueOf(action)) {
                    case HELLO:
                        helloFromServer(player);
                        displayInfoFromServer(player, "Nawi�zano po��czenie z serwerem");
                        break;
                    case MOVE:
                        playerMove(player, board, true);
                        displayInfoFromServer(player, "Tw�j ruch");
                        break;
                    case WAIT:
                        playerMove(player, board, false);
                        displayInfoFromServer(player, "Czekamy na ruch przeciwnika");
                        break;
                    case WINNER:
                        getGameButtonsFromMessage(board);
                        activateButtons(false);
                        confirmGameOverAndStartNew(player,"Gratulacje! Wygra�e� graczu " + playerSign + ".");
                        break;
                    case LOSER:
                        getGameButtonsFromMessage(board);
                        activateButtons(false);
                        confirmGameOverAndStartNew(player,"Przykro mi! Przegra�e� graczu " + playerSign + ".");
                        break;
                    case NO_WINNER:
                        getGameButtonsFromMessage(board);
                        activateButtons(false);
                        confirmGameOverAndStartNew(player,"Gra nierozstrzygni�ta graczu " +playerSign+ ".");

                        break;
                    case NEW_GAME:
                        getGameButtonsFromMessage(board);
                        displayInfoFromServer(player, "Nowa gra " + playerSign);
                        break;
                    case EXIT:
                        setPlayerActive(false);
                        isExit = true;
                        break;
                    default:
                        break;
                }
                if (isExit) {
                    Platform.exit();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);


        }
        System.out.println("W�a�nie zako�czy�e� gr�.");
    }

    /**
     * Metoda zaka�czaj�ca gr� i rozpocz�cie nowej gry potwierdzeniem od gracza.
     * @param player gracz X lub Y
     * @param message informacja od gracza o wyniku zako�czenia gry
     */
    private void confirmGameOverAndStartNew(char player, String message) {
        Platform.runLater(() -> {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Potwierdzenie");
            confirmationDialog.setHeaderText(null);
            confirmationDialog.setContentText(message);
            confirmationDialog.setX(100);
            confirmationDialog.setY(100);
            ButtonType confirmButton = new ButtonType("OK");
            confirmationDialog.getButtonTypes().setAll(confirmButton);

            confirmationDialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == confirmButton) {
                    System.out.println("Klikni�to 'OK'");
                    confirmationDialog.close();
                        //sendMessageToServer(player, actions.NEW_GAME);
                }
            });
        });


    }

    /**
     * Metoda pomocnicza wy�wietlaj�ce okno dialogowe do wprowadzania adresu IP oraz numeru portu do po��czenia si� gracza z serwerem.
     */
    private void getGameServerAddress() {
        DialogPane dialogPane = new DialogPane();
        dialogPane.setHeaderText("Podaj adres i port serwera gry.");

        GridPane gridPane = new GridPane();

        TextField addressTextField = new TextField();
        //default
        addressTextField.setText(serverAddress);
        TextField portTextField = new TextField();
        //default
        portTextField.setText(serverPort);
        gridPane.add(new Label("Adres IP:"), 0, 0);
        gridPane.add(addressTextField, 2, 0);
        gridPane.add(new Label("Port:"), 0, 1);
        gridPane.add(portTextField, 2, 1);

        dialogPane.setContent(gridPane);

        ButtonType confirmButtonType = new ButtonType("Potwierd�", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(confirmButtonType);

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Adres IP i port");
        dialog.setDialogPane(dialogPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>(addressTextField.getText(), portTextField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String ip = pair.getKey();
            String port = pair.getValue();
            this.serverAddress = ip;
            this.serverPort = port;
        });
    }

    /**
     * Metoda aktualizuje plansz� na podstawie informacji od serwera i
     * wyznacza na ich podstawie, kt�ry gracz b�dzie teraz aktywny i mo�e wykona� ruch.
     * @param player gracz X lub Y
     * @param board plansza gry
     * @param move zmienna boolean, je�li move = true gracz staje si� aktywny i ma mo�liwo�� ruchu
     */
    private void playerMove(char player, String board, boolean move) {
        //pobieramy plansz� z komunikatu od servera i aktualizujemy
        getGameButtonsFromMessage(board);
        if (move) {
            System.out.println("Gracz=" + this.playerSign + " aktywacja!");
            setPlayerActive(true);
        } else {
            System.out.println("Gracz=" + this.playerSign + " dezaktywacja!");
            setPlayerActive(false);
            confirmationToServer(player);
        }
    }

    /**
     * Metoda informuj�ca o po��czeniu danego gracza z serwerem oraz
     * wy�wietlanie na planszy gry, kt�ry znak zosta� przypisany do gracza.
     * @param player
     */
    private void helloFromServer(char player) {
        if (player != 'X' && player != 'O') {
            throw new RuntimeException("Nieprawid�owe oznaczenie gracza:" + player);
        } else {
            playerSign = player;
            final String displayPlayer = String.valueOf(this.playerSign);
            Platform.runLater(() -> playerSignLabel.setText("Jeste� Graczem: " + displayPlayer));
        }
        displayInfoFromServer(player, "Po��czono z serwerem");
        confirmationToServer(player);
    }

    /**
     * Metoda wy�wietlaj�ca na planszy gry informacje od serwera dla gracza
     * @param player oznacza gracza X lub O
     * @param info informacja od serwera
     */
    private void displayInfoFromServer(char player, String info) {
        //serverMessage
        final String displayInfo = info;
        Platform.runLater(() -> infoLabel.setText("Info: " + displayInfo));
    }

    private void confirmationToServer(char player) {
        if (player != 'X' && player != 'O') {
            throw new RuntimeException("Nieprawid�owe oznaczenie gracza:" + player);
        } else {
            clientOut.println("OK " + player);
        }
    }

    /**
     * Metoda wysy�aj�ca wiadomo�� do serwera informuj�ca o podj�tej akcji przez gracza {@link actions} opuszczeniu gry, kolejnym ruchu, nowej grze
     * @param player parametr oznaczaj�cy gracza X lub Y
     * @param action parametr oznaczaj�cy akcj� podj�t� przez gracza z{@link actions}
     */
    private void sendMessageToServer(char player, actions action) {
        System.out.println("sendMessageToServer");
        if (player != 'X' && player != 'O') {
            throw new RuntimeException("Nieprawid�owe oznaczenie gracza:" + player);
        } else {
            String msgString;
            switch (action) {
                case EXIT:
                    msgString = "EXIT " + this.playerSign + " " + getMessageFromGameButtons();
                    break;
                case OK:
                    msgString = "OK " + player;
                    break;
                case MOVE:
                    msgString = "MOVE " + player + " " + getMessageFromGameButtons();
                    break;
                case NEW_GAME:
                    msgString = "NEW_GAME " + this.playerSign ; //+ " " + getMessageFromGameButtons();
                    break;
                default:
                    return;
            }
            System.out.println("KLIENT WYSY�A  " + msgString);
            clientOut.println(msgString);
        }
    }

    /**
     * Metoda wysy�aj�ca informacj� do serwera, jaki ruch wykona� aktywny gracz
     * @param player parametr oznaczaj�cy gracza X lub O
     */
    private void sendMoveToServer(char player) {
        System.out.println("sendMoveToServer");
        if (player != 'X' && player != 'O') {
            throw new RuntimeException("Nieprawid�owe oznaczenie gracza:" + player);
        } else {
            String message = getMessageFromGameButtons();
            System.out.println("KLIENT WYSY�A MOVE " + player + " " + message);
            clientOut.println("MOVE " + player + " " + message);
        }
    }

    /**
     * Metoda pozwalaj�ca graczowi zako�czy� gr� w ka�dym momencie.
     */
    @FXML
    private void sendExitToServer() {
        System.out.println("senExitToServer");
        String message = getMessageFromGameButtons();
        System.out.println("KLIENT KO�CZY GR� " + this.playerSign + " " +message);
        clientOut.println("EXIT " + this.playerSign + " " + message);
    }

    /**
     * Metoda ustawia jednego gracza jako aktywnego z mo�liwo�ci� podj�cia jakiegokolwiek ruchu. W tym samym czasie drugiego gracza blokuje.
     * @param active zmienna boolean oznaczaj�ca czy gracz jest aktywny = true lub nieaktywny = false.
     */
    private void setPlayerActive(boolean active) {
        System.out.println("setPlayerActive");
        if (active) {
            System.out.println(this.playerSign + " jest aktywny");
            this.active = true;
            activateControls();
        } else {
            System.out.println(this.playerSign + " jest nieaktywny");
            this.active = false;
            deactivateAllControls();
        }
    }

    /**
     * Metoda ustawia na przycisku okre�lony obrazek ze znakiem X lub O
     * po klikni�ciu przez gracza w okre�lony przycisk (button).
     * Po czym wysy�a informacje do serwera o ruchu gracza oraz ustawia gracza aktywnego na nieaktywnego.
     * @param event podj�cie dzia�ania przez gracza-klikniecie w przycisk
     */
     @FXML
    void buttonClicked(ActionEvent event) {
        Button field = (Button) event.getSource();

        if (this.playerSign == 'X') {
                field.setGraphic(new ImageView(new Image(IMAGE_X)));
                field.setUserData(USER_DATA_IMG_X);
        } else if (this.playerSign == 'O') {
            field.setGraphic(new ImageView(new Image(IMAGE_O)));
            field.setUserData(USER_DATA_IMG_O);
        }
        sendMoveToServer(this.playerSign);
        setPlayerActive(false);
    }

    /**
     * Metoda pozwalaj�ca u�ytkownikowi w ka�dym momencie rozpocz�� now� gr�.
     * @see actions
     */
    @FXML
    void newGameClicked() {
        sendMessageToServer(this.playerSign, actions.NEW_GAME);
    }



}