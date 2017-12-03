package de.uulm.in.vs.grn.b1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class URLFetcher {

  public static void main(String[] args) {
    URL url;

    if (args.length == 0) {
      System.out.print("> ");

      try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
        url = new URL(in.readLine());
      } catch (MalformedURLException ex) {
        System.err.println("invalid url: " + ex.getMessage());
        return;
      } catch (IOException ex) {
        ex.printStackTrace();
        return;
      }
    } else {
      try {
        url = new URL(args[0]);
      } catch (MalformedURLException ex) {
        System.err.println("invalid url: " + ex.getMessage());
        return;
      }
    }

    try (Socket connection = new Socket(url.getHost(), url.getDefaultPort())) {
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      PrintWriter out = new PrintWriter(connection.getOutputStream());

      String file = url.getFile();

      if (file.equals("")) {
        file = "/";
      }

      // HTTP request
      out.print("GET " + file + " HTTP/1.1\r\n");
      out.print("host: " + url.getHost() + "\r\n");
      out.print("connection: close\r\n\r\n");
      out.flush();

      ArrayList<String> responseHeader = new ArrayList<>();

      // HTTP response header
      // ends with a empty new line
      for (String line = in.readLine(); line != null && !line.equals(""); line = in.readLine()) {
        responseHeader.add(line);
      }

      String[] firstLine = responseHeader.get(0).split(" ");

      if (!firstLine[1].equals("200")) {
        throw new ProtocolException("HTTP request failed with code " + firstLine[1]);
      }

      // entity
      String outputFileName = extractFileName(url.getPath());
      BufferedWriter outputFile = Files.newBufferedWriter(Paths.get(outputFileName));

      for (String line = in.readLine(); line != null; line = in.readLine()) {
        outputFile.write(line);
      }

      outputFile.flush();
    } catch (ProtocolException ex) {
      System.err.println(ex.getMessage());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static String extractFileName(String path) {
    String[] tmp = path.split("/");

    if (tmp.length == 0) {
      return "index.html";
    }

    return tmp[tmp.length - 1];
  }
}
