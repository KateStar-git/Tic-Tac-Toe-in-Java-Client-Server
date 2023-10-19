package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <strong>Politechnika Warszawska, Wydzia�, Studia Podyplomowe Java EE <br>
 * <em></em>Praca Dyplomowa: Gra sieciowa, K�ko i Krzy�yk  </strong><br>
 * <br>
 * @author Kinga Izdebska
 * @author Katarzyn Chojnowska
 * <br>
 * <strong> Opis gry: </strong><br>
 * Gra posiada dw�ch graczy: X i O. Gracze ��cz� si� do server przez WiFi lub hotspot. <br>
 * Po wpisaniu adresu IP i portu wy�wietla si� plansza dla ka�dego gracza. Ka�dy gracz klika na pola planszy <br>
 * zostawiaj�c przypisany do niego znak: X lub O. <br>
 * Wygrywa ten zawodnik, kt�remu uda si� wpisa� 3 razy sw�j znak w linii ci�g�ej. <br>
 * Gracz aktywny mo�e rozpocz�� w ka�dej chwili now� gr� lub zako�czy�. <br>
 */


/* *
* Klasa odpowiada za nawi�zanie po��czenia z graczami.
 * Przetwarza informacje o ich kolejnych ruchach. Przypisuje znak 'X' lub 'O' do graczy.
 * Metody w tej klasie informuj� graczy na podstawie informacji otrzymanych od Klienta o:
 * rozpocz�ciu gry, kto ma wykona� kolejny ruch, zako�czeniu gry, wyznaczeniu zwyci�zcy,
 * rozpocz�ciu nowej gry.
 */
public class TicTacToeServer {
    private static final short PLAYER_X=0;
    private static final short PLAYER_O=1;

    private short activePlayer; //okre�lenie aktywnego gracza, kt�ry ma prawo wykona� ruch
    private Socket[] playerSocketArray = new Socket[2] ;  //stworzenie 2 socket�w, przez kt�re ��cz� si� gracze
    private final char[] playerSignArray = {'X','O'};

    private BufferedReader[] inBufferArray = new BufferedReader[2] ;
    private DataOutputStream[] outStreamArray = new DataOutputStream[2];

    private TttBoardHandler gameBoard = new TttBoardHandler();

    /**
    Klasa zawieraj�ca warto�ci deklaruj�ce statusy proces�w przetwarzanych przez Server aplikacji
     HELLO-przywitanie gracza
     START-rozpocz�cie gry
     MOVE- kolejny ruch
     WAIT-gracz czeka na sw�j ruch (jest wtedy nieaktywny)
     OK-
     WINNER-okre�la zwyci�zc� gry
     LOOSER-okre�la przegranego gracza
     NO_WINNER- gra jest nierozstrzygni�ta; nie ma zwyci�zcy
     NEW_GAME-u�ytkownik nacisn�� przycisk 'Nowa Gra'
     EXIT-u�ytkownik nacisn�� przycisk 'Wyjd�'
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
    sta�a warto�� numeru portu, przez kt�ry ��cz� si� gracze do server
     */
    private int port = 9889;

    /** Metoda obs�uguje pocz�tkowe po��czenie od 2 graczy.
     * NA pocz�tku serwer informuje, pod jakim adresem i portem rozpoczyna dzia�anie.
     *  Najpierw czeka na po��czenie od gracza X i wysy�a mu przywitanie HELLO X, gdzie informuje go, jakie ma oznaczenie.
     *  Nast�pnie czeka na po��czenie od gracza O i wysy�a mu przywitanie HELLO O, gdzie informuje go, jakie ma oznaczenie.
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
            System.out.println("Gracz " + "KRZY�YK po��czy� si�.");
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
     * Metoda obs�uguj�ca gr�.
     * W p�tli wysy�a kolejno akcj� WAIT do nieaktywnego gracza i MOC do aktywnego gracza.
     * Nast�pnie zamienia graczy.
     * Umo�liwia r�wnie� zako�czenie i restart gry dla aktywnego gracza.
     * P�tla dzia�a, dop�ki kt�ry� z graczy nie zako�czy gry.
     */
    private void playTocTacToe () {
        //TODO .RuntimeException catch doda�
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
     * Metoda, kt�ra umo�liwia informowanie graczy kto jest zwyci�zc� gry: gracz 'X' lub gracz 'O'
     * oraz informuj�ca graczy, �e gra pozostaje nierozstrzygni�ta
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
     * Metoda umo�liwiaj�ca Serwerowi wysy�anie informacji do graczy,
     * kt�ry gracz jaki ruch wykona� oraz tablic� z 9 znakami: 'O','X' lub 'P'(puste pole)
     * @param player okre�lenie gracza X lub Y,
     * @param action rodzaj wykonanej akcji: MOVE, WAIT
     * @param board ci�g 9 znak�w 'O','X' lub 'P' (puste pole)
     * @throws RuntimeException w przypadku niepowodzenia wys�ania informacji do kt�regokolwiek gracza
     */
    private void sendMessageToClient(short player,actions action,String board){
        char playerSign = this.playerSignArray[player];
        String message=action +" "+playerSign+" "+board+ "\r\n";
        System.out.println("Wysy�amy wiadomo��:"+message);
        try {
            outStreamArray[player].writeBytes(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Wys�anie wiadomo�ci do "+playerSign+" nie uda�o si�.");
        }
    }

    /**
     * Metoda przyjmuj�ca informacj� od graczy
     * @param player identyfikacja, kt�ry gracz wykona� ruch
     * @return zwraca obiekt Message o okre�lone strukturze (zawiera pola player,action,board) przetwarzan� dalej w {@link TicTacToeServer#processClientMessage}
     * @throws RuntimeException w przypadku niepowodzenia w odbieraniu wiadomo�ci od gracza
     */
    private Message receiveClientMessage(short player) {
        Message messageFromClient = null;
        try {
            messageFromClient = new Message(inBufferArray[player].readLine());
            System.out.println("Odbieramy wiadomo�� "+messageFromClient);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("odbieranie  wiadomo�ci od "+this.playerSignArray[player]+" nie uda�o si�.");
        }
        return messageFromClient;
    }
    /**
    Metoda przetwarzaj�ca informacj� od graczy z metody {@link TicTacToeServer#receiveClientMessage(short)}
    i w zale�no�ci od zadeklarowanych sta�ych zmiennych z klasy {@link actions}
    wykonuje dzia�anie opisane w {@link actions}
     */
    private void processClientMessage(Message message) {
        System.out.println("processClientMessage: Odbieramy wiadomo�� "+message);
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
     * Metoda zaczynaj�ca now� gr�, w przypadku naci�ni�cia przez aktywnego gracza przycisku 'Nowa gra'.
     */
    private void startNewGame() {
        gameBoard.clear();
        sendMessageToClient(PLAYER_O,actions.NEW_GAME,this.gameBoard.toString());
        sendMessageToClient(PLAYER_X,actions.NEW_GAME,this.gameBoard.toString());
        activePlayer=PLAYER_X;
    }

    /**
     * Metoda prze��cza gracza aktywnego na nieaktywnego,
     * a status drugiego gracza zmienia na aktywny, aby m�g� wykona� kolejny ruch
     */
    private void switchPlayer() {
        if (activePlayer==PLAYER_O) activePlayer=PLAYER_X;
        else if (activePlayer==PLAYER_X) activePlayer=PLAYER_O;
        else throw  new RuntimeException("Nieprawid�owe oznaczenie gracza");
    }

    /**
     * Metoda wyznacza gracza przeciwnego do gracza podanego w parametrze metody
     * @param player gracz, do kt�rego zostanie wyznaczony gracz przeciwny,
     * @return zwraca gracza przeciwnego do gracza podanego w parametrze metody
     */
    private short getOppositePlayer(short player) {
        if (player==PLAYER_O) return PLAYER_X;
        else if (player==PLAYER_X) return PLAYER_O;
        else throw  new RuntimeException("Nieprawid�owe oznaczenie gracza");
    }

    /**git pu
     * Metoda uruchamia po��czenie z klientem {@link TicTacToeServer#establishConnectionWithClients()}
     * Metoda uruchamia gr� {@link TicTacToeServer#playTocTacToe()}
     */
    public static void main(String[] args) {
        TicTacToeServer ticTacToeServer = new TicTacToeServer();
        ticTacToeServer.establishConnectionWithClients();
        ticTacToeServer.playTocTacToe(); //try
    }
}