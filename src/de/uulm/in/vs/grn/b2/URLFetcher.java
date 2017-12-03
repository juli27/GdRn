package de.uulm.in.vs.grn.b2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
      InputStream in = connection.getInputStream();
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
      ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
      boolean newLine = false;

      for (int b = in.read(); b != -1; b = in.read()) {
        if (b == '\r') {
          // skip over \n
          in.read();

          if (newLine) {
            break;
          }

          responseHeader.add(new String(bufferStream.toByteArray()));
          bufferStream.reset();
          newLine = true;
        } else {
          bufferStream.write(b);
          if (newLine) {
            newLine = false;
          }
        }
      }

      /*for (String s : responseHeader) {
        System.out.println(s);
      }*/

      String[] firstLine = responseHeader.get(0).split(" ");

      if (!firstLine[1].equals("200")) {
        throw new ProtocolException("HTTP request failed with code " + firstLine[1]);
      }

      // entity
      String outputFileName = extractFileName(url.getPath());
      OutputStream outputFile = Files.newOutputStream(Paths.get(outputFileName));

      byte[] buffer = new byte[8192];
      int len;
      while ((len = in.read(buffer)) != -1) {
        outputFile.write(buffer, 0, len);
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
