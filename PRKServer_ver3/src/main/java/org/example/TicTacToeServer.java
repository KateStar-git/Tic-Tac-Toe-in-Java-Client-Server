package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <strong>Politechnika Warszawska, Wydzia³, Studia Podyplomowe Java EE <br>
 * <em></em>Praca Dyplomowa: Gra sieciowa, Kó³ko i Krzy¿yk  </strong><br>
 * <br>
 * @author Kinga Izdebska
 * @author Katarzyn Chojnowska
 * <br>
 * <strong> Opis gry: </strong><br>
 * Gra posiada dwóch graczy: X i O. Gracze ³¹cz¹ siê do server przez WiFi lub hotspot. <br>
 * Po wpisaniu adresu IP i portu wyœwietla siê plansza dla ka¿dego gracza. Ka¿dy gracz klika na pola planszy <br>
 * zostawiaj¹c przypisany do niego znak: X lub O. <br>
 * Wygrywa ten zawodnik, któremu uda siê wpisaæ 3 razy swój znak w linii ci¹g³ej. <br>
 * Gracz aktywny mo¿e rozpocz¹æ w ka¿dej chwili now¹ grê lub zakoñczyæ. <br>
 */


/* *
* Klasa odpowiada za nawi¹zanie po³¹czenia z graczami.
 * Przetwarza informacje o ich kolejnych ruchach. Przypisuje znak 'X' lub 'O' do graczy.
 * Metody w tej klasie informuj¹ graczy na podstawie informacji otrzymanych od Klienta o:
 * rozpoczêciu gry, kto ma wykonaæ kolejny ruch, zakoñczeniu gry, wyznaczeniu zwyciêzcy,
 * rozpoczêciu nowej gry.
 */
public class TicTacToeServer {
    private static final short PLAYER_X=0;
    private static final short PLAYER_O=1;

    private short activePlayer; //okreœlenie aktywnego gracza, który ma prawo wykonaæ ruch
    private Socket[] playerSocketArray = new Socket[2] ;  //stworzenie 2 socketów, przez które ³¹cz¹ siê gracze
    private final char[] playerSignArray = {'X','O'};

    private BufferedReader[] inBufferArray = new BufferedReader[2] ;
    private DataOutputStream[] outStreamArray = new DataOutputStream[2];

    private TttBoardHandler gameBoard = new TttBoardHandler();

    /**
    Klasa zawieraj¹ca wartoœci deklaruj¹ce statusy procesów przetwarzanych przez Server aplikacji
     HELLO-przywitanie gracza
     START-rozpoczêcie gry
     MOVE- kolejny ruch
     WAIT-gracz czeka na swój ruch (jest wtedy nieaktywny)
     OK-
     WINNER-okreœla zwyciêzcê gry
     LOOSER-okreœla przegranego gracza
     NO_WINNER- gra jest nierozstrzygniêta; nie ma zwyciêzcy
     NEW_GAME-u¿ytkownik nacisn¹³ przycisk 'Nowa Gra'
     EXIT-u¿ytkownik nacisn¹³ przycisk 'WyjdŸ'
     */
    private static enum actions {
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

    /**
    sta³a wartoœæ numeru portu, przez który ³¹cz¹ siê gracze do server
     */
    private int port = 9889;

    /** Metoda obs³uguje pocz¹tkowe po³¹czenie od 2 graczy.
     * NA pocz¹tku serwer informuje, pod jakim adresem i portem rozpoczyna dzia³anie.
     *  Najpierw czeka na po³¹czenie od gracza X i wysy³a mu przywitanie HELLO X, gdzie informuje go, jakie ma oznaczenie.
     *  Nastêpnie czeka na po³¹czenie od gracza O i wysy³a mu przywitanie HELLO O, gdzie informuje go, jakie ma oznaczenie.
     *
     */
    private void establishConnectionWithClients () {
        try {
            System.out.println("Czekamy na graczy pod tym adresem: "+ InetAddress.getLocalHost().getHostAddress()+" i portem: "+port);
            ServerSocket serverSock = new ServerSocket(port);

            /*
             *  Czekamy najpierw na Player X
             */

            this.playerSocketArray[PLAYER_X]  = serverSock.accept();
            System.out.println("Gracz " + "KRZY¯YK po³¹czy³ siê.");
            //ustalenie komunikacji
            this.inBufferArray[PLAYER_X] = new BufferedReader(new InputStreamReader( this.playerSocketArray[PLAYER_X].getInputStream()));
            this.outStreamArray[PLAYER_X] = new DataOutputStream(this.playerSocketArray[PLAYER_X].getOutputStream());
            sendMessageToClient(PLAYER_X,actions.HELLO,this.gameBoard.toString());
            receiveClientMessage(PLAYER_X);

            /*
             *  Czekamy najpierw na Player 0
             */
            this.playerSocketArray[PLAYER_O]  = serverSock.accept();
            System.out.println("Player" + "Circle get connected");
            //ustalenie komunikacji
            this.inBufferArray[PLAYER_O] = new BufferedReader(new InputStreamReader( this.playerSocketArray[PLAYER_O].getInputStream()));
            this.outStreamArray[PLAYER_O] = new DataOutputStream(this.playerSocketArray[PLAYER_O].getOutputStream());
            sendMessageToClient(PLAYER_O,actions.HELLO,this.gameBoard.toString());
            receiveClientMessage(PLAYER_O);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metoda obs³uguj¹ca grê.
     * W pêtli wysy³a kolejno akcjê WAIT do nieaktywnego gracza i MOC do aktywnego gracza.
     * Nastêpnie zamienia graczy.
     * Umo¿liwia równie¿ zakoñczenie i restart gry dla aktywnego gracza.
     * Pêtla dzia³a, dopóki któryœ z graczy nie zakoñczy gry.
     */
    private void playTocTacToe () {
        //TODO .RuntimeException catch dodaæ
        //TODO java.net.SocketException
        Message message;
        activePlayer=PLAYER_X;
        do  {
            sendMessageToClient(getOppositePlayer(activePlayer),actions.WAIT,this.gameBoard.toString());
            message = receiveClientMessage(getOppositePlayer(activePlayer));

            sendMessageToClient(activePlayer,actions.MOVE,this.gameBoard.toString());
            message = receiveClientMessage(activePlayer);
            processClientMessage(message);

            if (!gameBoard.isExitRequested()) {
                switchPlayer();
            }
            if (gameBoard.isGameOver()) {
                checkWinner();
                switchPlayer();
            }
        } while (!gameBoard.isExitRequested());

    }

    /**
     * Metoda, która umo¿liwia informowanie graczy kto jest zwyciêzc¹ gry: gracz 'X' lub gracz 'O'
     * oraz informuj¹ca graczy, ¿e gra pozostaje nierozstrzygniêta
     */
    private void checkWinner() {
        if (playerSignArray[PLAYER_X]==gameBoard.getWinner()) {
            sendMessageToClient(PLAYER_X,actions.WINNER,this.gameBoard.toString());
            sendMessageToClient(PLAYER_O,actions.LOSER,this.gameBoard.toString());
        } else if (playerSignArray[PLAYER_O]==gameBoard.getWinner()) {
            sendMessageToClient(PLAYER_O,actions.WINNER,this.gameBoard.toString());
            sendMessageToClient(PLAYER_X,actions.LOSER,this.gameBoard.toString());
        } else {
            sendMessageToClient(PLAYER_O,actions.NO_WINNER,this.gameBoard.toString());
            sendMessageToClient(PLAYER_X,actions.NO_WINNER,this.gameBoard.toString());
        }
    }

    /**
     * Metoda umo¿liwiaj¹ca Serwerowi wysy³anie informacji do graczy,
     * który gracz jaki ruch wykona³ oraz tablicê z 9 znakami: 'O','X' lub 'P'(puste pole)
     * @param player okreœlenie gracza X lub Y,
     * @param action rodzaj wykonanej akcji: MOVE, WAIT
     * @param board ci¹g 9 znaków 'O','X' lub 'P' (puste pole)
     * @throws RuntimeException w przypadku niepowodzenia wys³ania informacji do któregokolwiek gracza
     */
    private void sendMessageToClient(short player,actions action,String board){
        char playerSign = this.playerSignArray[player];
        String message=action +" "+playerSign+" "+board+ "\r\n";
        System.out.println("Wysy³amy wiadomoœæ:"+message);
        try {
            outStreamArray[player].writeBytes(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Wys³anie wiadomoœci do "+playerSign+" nie uda³o siê.");
        }
    }

    /**
     * Metoda przyjmuj¹ca informacjê od graczy
     * @param player identyfikacja, który gracz wykona³ ruch
     * @return zwraca obiekt Message o okreœlone strukturze (zawiera pola player,action,board) przetwarzan¹ dalej w {@link TicTacToeServer#processClientMessage}
     * @throws RuntimeException w przypadku niepowodzenia w odbieraniu wiadomoœci od gracza
     */
    private Message receiveClientMessage(short player) {
        Message messageFromClient = null;
        try {
            messageFromClient = new Message(inBufferArray[player].readLine());
            System.out.println("Odbieramy wiadomoœæ "+messageFromClient);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("odbieranie  wiadomoœci od "+this.playerSignArray[player]+" nie uda³o siê.");
        }
        return messageFromClient;
    }
    /**
    Metoda przetwarzaj¹ca informacjê od graczy z metody {@link TicTacToeServer#receiveClientMessage(short)}
    i w zale¿noœci od zadeklarowanych sta³ych zmiennych z klasy {@link actions}
    wykonuje dzia³anie opisane w {@link actions}
     */
    private void processClientMessage(Message message) {
        System.out.println("processClientMessage: Odbieramy wiadomoœæ "+message);
        Message messageFromClient = message;
        switch (messageFromClient.getAction()) {
            case "MOVE" :
                gameBoard.fillBoard(message.getBoard());
                break;
            case "EXIT" :
                sendMessageToClient(PLAYER_O,actions.EXIT,this.gameBoard.toString());
                sendMessageToClient(PLAYER_X,actions.EXIT,this.gameBoard.toString());
                gameBoard.setExitRequested(true);
                break;
            case "NEW_GAME" :
                startNewGame();
                break;
            default: break;
        }
    }

    /**
     * Metoda zaczynaj¹ca now¹ grê, w przypadku naciœniêcia przez aktywnego gracza przycisku 'Nowa gra'.
     */
    private void startNewGame() {
        gameBoard.clear();
        sendMessageToClient(PLAYER_O,actions.NEW_GAME,this.gameBoard.toString());
        sendMessageToClient(PLAYER_X,actions.NEW_GAME,this.gameBoard.toString());
        activePlayer=PLAYER_X;
    }

    /**
     * Metoda prze³¹cza gracza aktywnego na nieaktywnego,
     * a status drugiego gracza zmienia na aktywny, aby móg³ wykonaæ kolejny ruch
     */
    private void switchPlayer() {
        if (activePlayer==PLAYER_O) activePlayer=PLAYER_X;
        else if (activePlayer==PLAYER_X) activePlayer=PLAYER_O;
        else throw  new RuntimeException("Nieprawid³owe oznaczenie gracza");
    }

    /**
     * Metoda wyznacza gracza przeciwnego do gracza podanego w parametrze metody
     * @param player gracz, do którego zostanie wyznaczony gracz przeciwny,
     * @return zwraca gracza przeciwnego do gracza podanego w parametrze metody
     */
    private short getOppositePlayer(short player) {
        if (player==PLAYER_O) return PLAYER_X;
        else if (player==PLAYER_X) return PLAYER_O;
        else throw  new RuntimeException("Nieprawid³owe oznaczenie gracza");
    }

    /**git pu
     * Metoda uruchamia po³¹czenie z klientem {@link TicTacToeServer#establishConnectionWithClients()}
     * Metoda uruchamia grê {@link TicTacToeServer#playTocTacToe()}
     */
    public static void main(String[] args) {
        TicTacToeServer ticTacToeServer = new TicTacToeServer();
        ticTacToeServer.establishConnectionWithClients();
        ticTacToeServer.playTocTacToe(); //try
    }
}