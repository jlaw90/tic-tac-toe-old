import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Copyright 2006 James Lawrence
 * Date: 16-Dec-2006
 * Time: 23:19:03
 * Modification and redistribution without explicit permission by the creator(s) is prohibited
 * This source may be modified for personal use as long as the original author is accredited
 */
public class Main {
    public static void main(String[] args) {
        String opponent = "The CPU";
        ClientConnectionHandler cch = null;
        boolean AI = true;
        int size = 3;
        int type = 1;
        boolean client = false;

        //Check arguments...
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--help")) {
                System.out.println("USAGE: Main [option], where option can be:");
                System.out.println("\t-l [port]        : Listening mode, so another Tic-Tac-Toe client can connect and play.");
                System.out.println("\t-r [host] [port] : Remote mode, attempts to connect to host:port and initiate a game");
                System.out.println("\t--help           : Prints this message");
                System.out.println("\t[board]          : Specifies board dimensions (note, one int, e.g. 4, for a board-size of 4x4)");
                return;
            } else if (arg.equalsIgnoreCase("-l")) {
                if (i + 1 >= args.length) {
                    System.err.println("No port specified!");
                    System.exit(-1);
                } else if (!args[++i].matches("\\d+")) {
                    System.err.println("A port is a number you moron.");
                    System.exit(-1);
                }
                int port = Integer.parseInt(args[i]);
                System.out.println("Binding to port " + port + "...");
                try {
                    ServerSocket ss = new ServerSocket(port);
                    System.out.println("We are bound, waiting for a connection...");
                    Socket s = ss.accept();
                    System.out.println("We have received a connection from " + s.getInetAddress().getHostName() + "["
                            + s.getInetAddress().getHostAddress() + "], initiating game...");
                    AI = false;
                    byte[] name = new byte[20];
                    s.getInputStream().read(name);
                    opponent = new String(name).trim();
                    String oname = JOptionPane.showInputDialog("Please enter your name.");
                    if (oname.length() > 20)
                        oname = oname.substring(0, 20);
                    while (oname.length() < 20)
                        oname += " ";
                    s.getOutputStream().write(oname.getBytes());
                    type = s.getInputStream().read();
                    System.out.println("Client identified as " + opponent + ", they want to be " +
                            (type == 1 ? "crosses" : "noughts"));
                    type = type == 1? 2: 1;
                    size = s.getInputStream().read();
                    cch = new ClientConnectionHandler(s, null);
                } catch (IOException ioe) {
                    System.err.println("I/O Exception encountered!\n" + ioe.toString());
                    System.exit(-1);
                }
            } else if (arg.equalsIgnoreCase("-r")) {
                if (i + 1 >= args.length) {
                    System.err.println("No host specified!");
                    System.exit(-1);
                } else if (i + 2 >= args.length) {
                    System.err.println("No port specified!");
                    System.exit(-1);
                }
                int port = Integer.parseInt(args[++i + 1]);
                try {
                    System.out.println("Connecting to client...");
                    Socket s = new Socket(args[i++], port);
                    System.out.println("Connected!");
                    AI = false;
                    OutputStream out = s.getOutputStream();
                    String name = JOptionPane.showInputDialog("Please enter your name.");
                    if (name.length() > 20)
                        name = name.substring(0, 20);
                    while (name.length() < 20)
                        name += " ";
                    out.write(name.getBytes());
                    byte[] tname = new byte[20];
                    s.getInputStream().read(tname);
                    opponent = new String(tname).trim();
                    //Confusing thing is that Board reverses them seeing as it resets at the beginning
                    //You have to remember this!
                    type = (JOptionPane.showConfirmDialog(null, "Do you wish to be noughts (Yes) or Crosses (No)", "Well?",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) ? 2 : 1;
                    out.write(type);
                    out.write(size);
                    client = true;
                    cch = new ClientConnectionHandler(s, null);
                    if (size == 3) {
                        System.out.println("No size was specified before -r, so default is used.");
                        break;
                    }
                } catch (IOException ioe) {
                    System.err.println("Error connecting to client!\n" + ioe.toString());
                    System.exit(-1);
                }
            } else if (arg.matches("\\d+")) {
                System.out.println("Using " + arg + " as the board size.");
                size = Integer.parseInt(arg);
            }
        }

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (size * 100 > d.width || size * 100 > d.height) {
            System.err.println("Your screen is not big enough to have that size a board, exiting!");
            System.exit(1);
        }
        System.out.println("Tic-Tac-Toe v1 ~ copyright 2006 newbiehacker");
        System.out.println("use --help for more info.");
        System.out.println("Any bugs, comments, suggestions? please tell me on IRC: irc.freenode.com, #mopar");
        System.out.println();
        JFrame jf = new JFrame("Tic Tac Toe - You vs. " + opponent);
        Board board;
        if (cch != null) {
            board = new Board(size, cch);
            cch.board = board;
            cch.start();
        } else
            board = new Board(size);
        board.opponent = opponent;
        board.useAI = AI;
        board.myType = type;
        jf.add(board);
        jf.pack();
        jf.setResizable(false);
        jf.setVisible(true);
        new Thread(board).start();
    }
}