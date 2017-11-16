package de.uulm.in.vs.grn.a2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NumberGuessingGameThreadedServer {

  private static final int SERVER_PORT = 5555;
  private static final int NUM_THREADS = 4;

  public static void main(String[] args) {
    ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

    try (ServerSocket socket = new ServerSocket(SERVER_PORT)) {
      while (!socket.isClosed()) {
        Socket connection = socket.accept();

        pool.submit(new NumberGuessingGameRequestHandler(connection));
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
