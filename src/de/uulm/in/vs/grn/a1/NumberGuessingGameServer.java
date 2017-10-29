package de.uulm.in.vs.grn.a1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class NumberGuessingGameServer {

  private static final int SERVER_PORT = 5555;

  public static void main(String[] main) {
    try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
      while (!serverSocket.isClosed()) {
        Socket connection = serverSocket.accept();

        // don't crash the server when an exception occurs with the client connection
        // and auto close the socket
        try (connection) {
          InputStream in = connection.getInputStream();
          OutputStream out = connection.getOutputStream();

          out.write(("\nWelcome to Luisa's secret NumberGuessing game!\n"
              + "I'll grant you 6 turns to guess my secret number between 0 and 50\n").getBytes());

          boolean won = false;
          int number = ThreadLocalRandom.current().nextInt(50);

          for(int attemptsLeft = 6; attemptsLeft > 0; --attemptsLeft) {
            byte[] buffer = new byte[8];
            int len = in.read(buffer);
            String input = new String(buffer, 0, len).trim();

            int guess;

            try {
              guess = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
              out.write("That's not a valid number!\n".getBytes());
              ++attemptsLeft;
              continue;
            }

            if (guess < number) {
              out.write("Your guess is too low.".getBytes());
            } else if (guess > number) {
              out.write("Your guess is too high.".getBytes());
            } else {
              won = true;
              break;
            }

            out.write((" Remaining turns: " + (attemptsLeft - 1) + '\n').getBytes());
          }

          if (won) {
            out.write("Congratulations, you won!\n".getBytes());
          } else {
            out.write("You lost :(\n".getBytes());
          }
        } catch (IOException | RuntimeException ex) {
          ex.printStackTrace();
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
