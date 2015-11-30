import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Copyright 2006 James Lawrence
 * Date: 17-Dec-2006
 * Time: 02:09:50
 * Modification and redistribution without explicit permission by the creator(s) is prohibited
 * This source may be modified for personal use as long as the original author is accredited
 */
public class ClientConnectionHandler extends Thread {
    private InputStream in;
    private OutputStream out;
    public Board board;


    public ClientConnectionHandler(Socket s, Board board) throws IOException {
        in = s.getInputStream();
        out = s.getOutputStream();
        this.board = board;
    }

    public void makeMove(int x, int y) throws IOException {
        out.write(new byte[]{(byte) x, (byte) y});
    }

    public void run() {
        Thread.currentThread().setPriority(1);
        try {
            byte[] data = new byte[2];
            int off = 0;
            while (true) {
                Thread.sleep(10);
                int i = in.read(data, off, data.length - off);
                if (i == -1)
                    break;
                if (i == 0)
                    continue;
                if (i != 2) {
                    off = 1;
                    continue;
                }
                off = 0;
                int x = data[0];
                int y = data[1];
                if (x > board.board.length || y > board.board.length || x < 0 || y < 0) {
                    System.err.println("Invalid locations specified (not on board: " + x + ", " + y + ")");
                    System.exit(-1);
                }
                if (board.board[x][y] != 0) {
                    System.err.println("Invalid locations specified (already filled: " + x + ", " + y + ")");
                    System.exit(-1);
                }
                board.board[x][y] = board.myType == 1 ? 2 : 1;
                board.drawStringWait = false;
            }
        } catch (Exception e) {
            System.out.println("Client disconected.");
            System.exit(-1);
        }
        System.out.println("Connection ended.");
        System.exit(0);
    }
}