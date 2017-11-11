package de.uulm.in.vs.grn.a1;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public final class NumberGuessingGameServer {

  private static final int SERVER_PORT = 5555;

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
      while (!serverSocket.isClosed()) {
        Socket connection = serverSocket.accept();

        System.out.println("[" + connection.getInetAddress().getHostAddress() + "] connected");

        // don't crash the server when an exception occurs with the client connection.
        // closing one of the streams closes the socket
        try (InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream()) {
          out.write(("\r\nWelcome to Luisa's secret NumberGuessing game!\r\n"
              + "I'll grant you 6 turns to guess my secret number between 0 and 50\r\n").getBytes(UTF_8));

          boolean won = false;
          int number = ThreadLocalRandom.current().nextInt(50);

          for (int attemptsLeft = 6; attemptsLeft > 0; --attemptsLeft) {
            out.write("> ".getBytes(UTF_8));

            byte[] buffer = new byte[32];
            int len = in.read(buffer);

            if (len == -1) {
              System.out.println("[" + connection.getInetAddress().getHostAddress() + "] end of stream");
              break;
            }

            String line = new String(buffer, 0, len).trim();
            int guess;

            try {
              guess = Integer.parseInt(line);
            } catch (NumberFormatException ex) {
              out.write("That's not a valid number!\r\n".getBytes(UTF_8));
              ++attemptsLeft;
              continue;
            }

            if (guess < number) {
              out.write("Your guess is too low.".getBytes(UTF_8));
            } else if (guess > number) {
              out.write("Your guess is too high.".getBytes(UTF_8));
            } else {
              won = true;
              break;
            }

            out.write((" Remaining turns: " + (attemptsLeft - 1) + "\r\n").getBytes(UTF_8));
          }

          if (won) {
            out.write("Congratulations, you won!\r\n\r\n".getBytes(UTF_8));
          } else {
            out.write("You lost :(\r\n\r\n".getBytes(UTF_8));
          }
        } catch (IOException | RuntimeException ex) {
          ex.printStackTrace();
        }

        System.out.println("[" + connection.getInetAddress().getHostAddress() + "] disconnected");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
