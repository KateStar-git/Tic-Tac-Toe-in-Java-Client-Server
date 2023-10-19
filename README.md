# Tic-Tac-Toe-in-Java-Client-Server
On line game Client-Server uses sockets, threads and Model-View-Controller design pattern.

## Game Logic
It can be played by 2 players: 'X' and 'O'.
After connecting and providing the IP address and port number, each player connects to the server, which assigns him an 'X' or 'O' character.

The first player to place his mark three times in a row or diagonally across the game board wins.

The active player, i.e. the one making a move at a given moment, can end the game or start a new one at any time.

## Five game cases:
1. Player 'X' wins
2. Player 'O' wins    
3. The game remains undecided      
4. One of the players has finished the game.      
5. New Game - Only the active player can start a new game

## Tech Stack
* JAVA 17
* JavaFX
* Sockets
* MVC
* Threads
* CSS
* SceneBuilder
* JUNIT4

## Additional info
This project is the diploma project of the postgraduate program Java EE - Software Production, which I successfully completed in June 2023.
This is a team project, carried out together with a friend from the above-mentioned studies.

My contribution to this project is:
* cooperation in the game logic
* design of the client part and development of the entire graphic design (work in SceneBuilder, programming View: *css and *fxml files).
* I also solved the problem of two players from two different computers being unable to connect using the same hotspot.
  (connecting via the same WiFi worked fine).


  
