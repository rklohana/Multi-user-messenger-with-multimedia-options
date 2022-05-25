
package com.mycompany.cnproj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class clientThread extends Thread{
     private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
static ArrayList<MyFile> myFiles = new ArrayList<>();
  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("The name should not contain '@' character.");
        }
      }
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println("*** A new user " + name
                + " entered the chat room !!! ***");
          }
        }
      }
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        if (line.startsWith("/file")) {
          try {
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                int fileNameLength = dataInputStream.readInt();
                System.out.println(fileNameLength);
                if (fileNameLength > 0) {
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);               
                    String fileName = new String(fileNameBytes);
                    System.out.println(fileName);
                    int fileContentLength = dataInputStream.readInt();
                    if (fileContentLength > 0) {
                        System.out.println("forwarding");
                        byte[] fileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
                         synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                   DataOutputStream dataOutputStream = new DataOutputStream(threads[i].clientSocket.getOutputStream());
                  threads[i].os.println("/file");
                  dataOutputStream.flush();
                        dataOutputStream.writeInt(fileNameBytes.length);
                        dataOutputStream.flush();
                        dataOutputStream.write(fileNameBytes);
                        dataOutputStream.writeInt(fileContentBytes.length);
                        dataOutputStream.flush();
                        dataOutputStream.write(fileContentBytes);
                        dataOutputStream.flush();
                         System.out.println("File Forwarded");

                      
              }
                         }
          }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(words[0])) {
                    threads[i].os.println("<" + name + "> " + words[1]);
                    this.os.println(">" + name + "> " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        } else {
          synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println("<" + name + "> " + line);
              }
            }
          }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("*** The user " + name
                + " is leaving the chat room !!! ***");
          }
        }
      }
      os.println("*** Bye " + name + " ***");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}
