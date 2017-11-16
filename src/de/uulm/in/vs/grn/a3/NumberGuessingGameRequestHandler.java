package de.uulm.in.vs.grn.a3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

class NumberGuessingGameRequestHandler implements Runnable {

  private final Socket connection;

  NumberGuessingGameRequestHandler(Socket connection) {
    this.connection = connection;
  }

  @Override
  public void run() {
    System.out.println("[" + connection.getInetAddress().getHostAddress() + "] connected");

    try (InputStream inStream = connection.getInputStream();
        OutputStream outStream = connection.getOutputStream()) {

      BufferedReader in = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));

      out.print("\r\nWelcome to Luisa's secret NumberGuessing game!\r\n"
          + "I'll grant you 6 turns to guess my secret number between 0 and 50\r\n");

      boolean won = false;
      int number = ThreadLocalRandom.current().nextInt(50);

      for (int attemptsLeft = 6; attemptsLeft > 0; --attemptsLeft) {
        out.print("> ");
        out.flush();

        String line = in.readLine();
        int guess;

        try {
          guess = Integer.parseInt(line);
        } catch (NumberFormatException ex) {
          out.print("That's not a valid number!\r\n");
          ++attemptsLeft;
          continue;
        }

        if (guess < number) {
          out.print("Your guess is too low.");
        } else if (guess > number) {
          out.print("Your guess is too high.");
        } else {
          won = true;
          break;
        }

        out.print(" Remaining turns: " + (attemptsLeft - 1) + "\r\n");
      }

      if (won) {
        out.print("Congratulations, you won!\r\n\r\n");
      } else {
        out.print("You lost :(\r\n\r\n");
      }

      out.flush();

    } catch (IOException | RuntimeException ex) {
      ex.printStackTrace();
    }

    System.out.println("[" + connection.getInetAddress().getHostAddress() + "] disconnected");
  }
}
