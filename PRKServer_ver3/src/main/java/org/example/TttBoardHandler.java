package org.example;

/**
 * Klasa pomocnicza tworząca planszę gry
 */
public class TttBoardHandler {

    private static final char KOLKO = 'O';
    private static final char KRZYZYK = 'X';
    private static final char PUSTE = 'P';

    private static final char WINNER_X = 'W';
    private static final char WINNER_O = 'R';
    private static final char SEP = ',';
    private int playerMove; //X = 1, O = -1

    public char[][] getCurrentBoard() {
        return this.currentBoard;
    }

    private char[][] currentBoard;

    private char winner='P';

    private boolean activeGame;
    private boolean isExitRequested = false;

    /**
     * Konstruktor klasy
     */
    public TttBoardHandler() {
        this.currentBoard = new char[3][3];
        emptyCurrentBoard();
        this.playerMove = 1;
        this.activeGame = true;
    }
    public void emptyCurrentBoard () {
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                currentBoard[i][j]=PUSTE;
            }
        }
    }

    public int getPlayerMove() {
        return this.playerMove;
    }

    /**
     * Metoda wypełniająca tablicę znakami pobranymi od gracza
     * @param board tablica gry
     */
    public void fillBoard (String board) {
        System.out.println("Start fill board "+board);
        String[] boardArray = board.split(",");
        if (boardArray.length!=9) {
            throw new RuntimeException("Nieprawidłowa plansza wielkości="+boardArray.length);
        }
        int column;
        int row;
        for(int i=0;i<9;i++) {
            row=i/3;
            column=i%3;
            if (boardArray[i].trim().length()>0) {
                currentBoard[row][column] = boardArray[i].trim().charAt(0);
            } else {
                currentBoard[row][column] = PUSTE;
            }
        }
        System.out.println("End Fill board "+this);
    }

    /**
     * Metoda sprawdzająca, czy nastąpiło zakończenie gry
     * @return zwraca wartość true, jeśli gra została zakończona oraz określa zwycięzcę
     */
    public boolean isGameOver() {
        this.winner=PUSTE;
        if (checkAndMarkWinner(KOLKO)) {
            this.activeGame=false;
            winner=KOLKO;
            return true;
        }
        if (checkAndMarkWinner(KRZYZYK)) {
            this.activeGame=false;
            winner=KRZYZYK;
            return true;
        }
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                if (currentBoard[i][j]==PUSTE) return false;
            }
        }
        this.activeGame=true;
        return true;
    }

    /**
     * Metoda sprawdzająca, który gracz wygrał i przypisanie mu znaku
     * na tablicy w kolorze czerwonym
     * @param player parametr char oznaczający jednego z dwóch graczy: X lub O
     * @return zwraca wartość true, jeśli gra jest rozstrzygnięta i jest zwycięzca
     *
     */
    private boolean checkAndMarkWinner(char player) {
        //wiersze
        char winnerSign;
        for (int row = 0; row < 3; row++) {
            if (currentBoard[row][0] == player && currentBoard[row][1] == player && currentBoard[row][2] == player) {

                if (player == KRZYZYK) {
                    winnerSign = WINNER_X;
                } else {
                    winnerSign = WINNER_O;
                }
                currentBoard[row][0] = winnerSign;
                currentBoard[row][1] = winnerSign;
                currentBoard[row][2] = winnerSign;

                return true;
            }
        }
        //kolumny
        for (int col = 0; col < 3; col++) {
            if (currentBoard[0][col] == player && currentBoard[1][col] == player && currentBoard[2][col] == player) {
                if (player == KRZYZYK) {
                    winnerSign = WINNER_X;
                } else {
                    winnerSign = WINNER_O;
                }
                currentBoard[0][col] = winnerSign;
                currentBoard[1][col] = winnerSign;
                currentBoard[2][col] = winnerSign;
                return true;
            }
        }
        //przekątne
        if (currentBoard[0][0] == player && currentBoard[1][1] == player && currentBoard[2][2] == player) {
            if (player == KRZYZYK) {
                winnerSign = WINNER_X;
            } else {
                winnerSign = WINNER_O;
            }
            currentBoard[0][0] = winnerSign;
            currentBoard[1][1] = winnerSign;
            currentBoard[2][2] = winnerSign;
            return true;
        }
        if (currentBoard[0][2] == player && currentBoard[1][1] == player && currentBoard[2][0] == player) {
            if (player == KRZYZYK) {
                winnerSign = WINNER_X;
            } else {
                winnerSign = WINNER_O;
            }
            currentBoard[0][2] = winnerSign;
            currentBoard[1][1] = winnerSign;
            currentBoard[2][0] = winnerSign;
            return true;
        }
        return false;
    }

    //odp KOLKO KRZYZYK PUSTE-nie ma wygranego
    public char getWinner(){
        return winner;
    }

    public boolean isActiveGame() {
        return activeGame;
    }

    public void setActiveGame(boolean activeGame) {
        this.activeGame = activeGame;
    }

    public boolean isExitRequested() {
        return isExitRequested;
    }

    public void setExitRequested(boolean exitRequested) {
        isExitRequested = exitRequested;
    }

    public void clear() {
        emptyCurrentBoard();
        System.out.println("After cleaning the board: "+this);
        this.winner='P';
        this.activeGame=false;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                switch (currentBoard[i][j]) {
                    case KOLKO: sb.append(KOLKO).append(SEP); break;
                    case KRZYZYK: sb.append(KRZYZYK).append(SEP); break;
                    case WINNER_O: sb.append(WINNER_O).append(SEP);break;
                    case WINNER_X: sb.append(WINNER_X).append(SEP);break;
                    case PUSTE: sb.append(PUSTE).append(SEP); break; //może to trzeba usunąć
                    default: sb.append(PUSTE).append(SEP); break;
                }
            }
        }
        return sb.toString();
    }
}
