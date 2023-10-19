package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TttBoardHandlerTest {
    private TttBoardHandler boardHandler;

    @BeforeEach
    void setUp() {
        boardHandler = new TttBoardHandler();
    }

    @Test
    @DisplayName("Test sprawdzający, czy plansza jest pusta na początku gry")
    void testEmptyCurrentBoard() {
        boardHandler.emptyCurrentBoard();
        char[][] currentBoard = boardHandler.getCurrentBoard();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals('P', currentBoard[i][j]);
            }
        }
    }

    @ParameterizedTest
    @DisplayName("Test sprawdzający poprawne wczytanie wypełnionej planszy")
    @ValueSource(strings={
            "X,O,X,P,X,P,O,P,O,"   // wygrana X w pierwszym wierszu
    })
    void testFillBoard(String inputBoard) {
        boardHandler.emptyCurrentBoard();

        char[][] expectedBoard = new char[3][3];
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                expectedBoard[i][j] = inputBoard.charAt(index);
                index=index+2; //pomijanie przecinków
            }
        }


        boardHandler.fillBoard(inputBoard);

        assertArrayEquals(expectedBoard, boardHandler.getCurrentBoard());
    }
}
