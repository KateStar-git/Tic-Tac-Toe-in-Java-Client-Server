package org.example;

/**
 * Klasa pomocnicza tworz¹ca wiadomoœci z serwera do graczy oraz od graczy do serwera
 */
public class Message {
    private String action;
    private char player;
    private String board;

    @Override
    public String toString() {
        return  "< action=" + action  +
                ", player=" + player +
                ", board=" + board + ">"
               ;
    }

    /**
     * Konstruktor klasy
     * @param inputMessage
     */
    public Message(String inputMessage) {
        String[] msgs = inputMessage.split(" ");
        this.action = msgs[0].trim();
        this.player = msgs[1].trim().charAt(0);
        this.board = null;
        if (msgs.length==3) {
            board=msgs[2].trim();
        }
        System.out.println(this.toString());
    }

    public String getAction() {
        return action;
    }

    public char getPlayer() {
        return player;
    }

    public String getBoard() {
        return board;
    }
}
