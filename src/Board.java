import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * Copyright 2006 James Lawrence
 * Date: 16-Dec-2006
 * Time: 23:09:31
 * Modification and redistribution without explicit permission by the creator(s) is prohibited
 * This source may be modified for personal use as long as the original author is accredited
 */
public class Board extends Canvas implements Runnable, MouseListener {
    public final int[][] board;
    public int myType;
    public String opponent;
    public boolean useAI;
    public boolean end = false;
    private ClientConnectionHandler cch;
    private BufferedImage buffer;
    private long drawStringStart;
    private boolean drawString;
    public boolean drawStringWait;
    private String text;

    public Board(int size) {
        board = new int[size][size];
        setPreferredSize(new Dimension(size * 100, (size * 100) + 20));
        addMouseListener(this);
        text = "Welcome to Tic-Tac-Toe!";
        drawString = true;
        drawStringStart = -1;
    }

    public Board(int size, ClientConnectionHandler cch) {
        this(size);
        this.cch = cch;
    }

    public void update(Graphics g) {
        paint(g); // Override this method so we paint everytime
    }

    public void paint(Graphics g1) {
        // We buffer the screen so we don't get that annoying flicker when drawing
        if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight())
            buffer = new BufferedImage(getWidth(), getHeight(), 1);//We don't need no alpha transparency
        Graphics g = buffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.DARK_GRAY);
        int size = board.length;
        int rx, ry;
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++) {
                rx = x * 100;
                ry = y * 100;
                g.drawRect(rx, ry, 100, 100);
                if (board[x][y] == 1) { // X
                    g.setColor(Color.BLUE);
                    // Hacked up line drawing method, DOESNT WORK PROPERLY :(
                    for (int i = -3; i < 3; i++) {
                        g.drawLine(rx + 5 + i, ry + 5 - i, rx + 95 + i, ry + 95 - i);
                        g.drawLine(rx + 95 + i, ry + 5 + i, rx + 5 + i, ry + 95 + i);
                    }
                    g.setColor(Color.DARK_GRAY);
                } else if (board[x][y] == 2) { // O
                    g.setColor(Color.RED);
                    //Fill in a circle
                    g.fillOval(rx + 5, ry + 5, 90, 90);
                    g.setColor(Color.WHITE);
                    //Take away the middle bit!
                    g.fillOval(rx + 25, ry + 25, 50, 50);
                    g.setColor(Color.GRAY);
                }
            }
        if (drawString || drawStringWait) {
            Font f = g.getFont().deriveFont(Font.BOLD, 24);
            g.setFont(f);
            g.setColor(Color.GRAY);
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2;
            g.drawString(text, x, y);
        }
        //We draw our buffer to the screen
        g1.drawImage(buffer, 0, 0, null);
    }

    public void run() {
        Thread.currentThread().setPriority(1);
        try {
            while (true) {
                if (drawString && drawStringStart == -1) {
                    drawStringStart = System.currentTimeMillis();
                } else if (drawString && System.currentTimeMillis() - drawStringStart >= 3000) {
                    reset();
                    drawStringStart = -1;
                    drawString = false;
                } else if (!drawString && boardFull() && !end) {
                    text = "Draw!";
                    drawString = true;
                }
                repaint();// Make sure we keep repainting :)
                checkStatus();
                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.err.println("Unknown exception!");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void reset() {

        drawStringWait = false;
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board.length; y++)
                board[x][y] = 0;
        myType = myType == 2 ? 1 : 2;
        end = false;
        if (myType == 1) {
            if (useAI)
                playAI();
            else {
                text = "Waiting...";
                drawStringWait = true;
            }
        }
    }

    public void checkStatus() {
        if (drawString)
            return;//Nothing to do.
        // Check for any wins on the x-axis.
        int win = 0;
        for (int y = 0; y < board.length; y++) {
            win = 0;
            for (int[] b : board) {
                if ((win != 0 && b[y] != win) || b[y] == 0) {// If last has been set and we don't equal it, then no point continuing.
                    win = 0;
                    break;
                }
                win = b[y];
            }
            if (win != 0) {
                end = true;
                text = (win == myType ? "You" : opponent) + " won!";
                drawString = true;
                return;
            }
        }
        // Check for any wins on the y-axis.
        for (int[] b : board) {
            win = 0;
            for (int y = 0; y < board.length; y++) {
                if ((win != 0 && b[y] != win) || b[y] == 0) {// If last has been set and we don't equal it, then no point continuing.
                    win = 0;
                    break;
                }
                win = b[y];
            }
            if (win != 0) {
                end = true;
                text = (win == myType ? "You" : opponent) + " won!";
                drawString = true;
                return;
            }
        }

        //Check for any wins diagonally from top-left
        for (int i = 0; i < board.length; i++)
            if ((win != 0 && board[i][i] != win) || board[i][i] == 0) {
                win = 0;
                break;
            } else
                win = board[i][i];
        if (win != 0) {
            end = true;
            text = (win == myType ? "You" : opponent) + " won!";
            drawString = true;
        }

        //Check for any wins diagonally from the top-right
        for (int i = 0; i < board.length; i++)
            if ((win != 0 && board[board.length - i - 1][i] != win) || board[board.length - i - 1][i] == 0) {
                win = 0;
                break;
            } else
                win = board[board.length - i - 1][i];
        if (win != 0) {
            end = true;
            text = (win == myType ? "You" : opponent) + " won!";
            drawString = true;
        }
    }

    public boolean boardFull() {
        for (int[] b : board)
            for (int y = 0; y < board.length; y++)
                if (b[y] == 0)
                    return false;
        return true;
    }

    public void playAI() {
        if (boardFull() || end)
            return;
        //First, check if we can win with one move, if we can, then lets do it!
        //Check the x axis for a 1-move win
        boolean foundSpace;
        boolean found = true;
        int mx = 0, my = 0;
        for (int y = 0; y < board.length; y++) {
            foundSpace = false;
            for (int x = 0; x < board.length; x++) {
                found = true;
                if (board[x][y] == 0)
                    if (foundSpace) {
                        found = false;
                        break;
                    } else {
                        mx = x;
                        my = y;
                        foundSpace = true;
                    }
                else if (board[x][y] == myType) {// Damn X's took our spot!
                    found = false;
                    break;
                }
            }
            if (found && foundSpace) {
                System.out.println("AI calculated that " + mx + ", " + my + " would win!");
                board[mx][my] = myType == 2 ? 1 : 2;
                return;
            }
        }
        //Check the y axis for a 1-move win
        for (int x = 0; x < board.length; x++) {
            foundSpace = false;
            for (int y = 0; y < board.length; y++) {
                found = true;
                if (board[x][y] == 0)
                    if (foundSpace) {
                        found = false;
                        break;
                    } else {
                        mx = x;
                        my = y;
                        foundSpace = true;
                    }
                else if (board[x][y] == myType) {// Damn X's took our spot!
                    found = false;
                    break;
                }
            }
            if (found && foundSpace) {
                System.out.println("AI calculated that " + mx + ", " + my + " would win!");
                board[mx][my] = myType == 2 ? 1 : 2;
                return;
            }
        }
        //Check if a move diagonally from top-left can win
        foundSpace = false;
        for (int i = 0; i < board.length; i++) {
            found = true;
            if (board[i][i] == 0)
                if (foundSpace) {
                    found = false;
                    break;
                } else {
                    mx = i;
                    my = i;
                    foundSpace = true;
                }
            else if (board[i][i] == myType) {// Damn X's took our spot!
                found = false;
                break;
            }
        }
        if (found && foundSpace) {
            System.out.println("AI calculated that " + mx + ", " + my + " would win!");
            board[mx][my] = myType == 2 ? 1 : 2;
            return;
        }
        //Check if a move diagonally from top-right can win
        foundSpace = false;
        for (int i = 0; i < board.length; i++) {
            found = true;
            if (board[i][board.length - i - 1] == 0)
                if (foundSpace) {
                    found = false;
                    break;
                } else {
                    mx = i;
                    my = board.length - i - 1;
                    foundSpace = true;
                }
            else if (board[i][board.length - i - 1] == myType) {// Damn X's took our spot!
                found = false;
                break;
            }
        }
        if (found && foundSpace) {
            System.out.println("AI calculated that " + mx + ", " + my + " would win!");
            board[mx][my] = myType == 2 ? 1 : 2;
            return;
        }

        //Now we've checked if we can win, we'll check if the player can win in one move :O
        //If they can, we'll block them :)
        //Check the x-axis
        for (int y = 0; y < board.length; y++) {
            foundSpace = false;
            for (int x = 0; x < board.length; x++) {
                found = true;
                if (board[x][y] == 0)
                    if (foundSpace) {
                        found = false;
                        break;
                    } else {
                        mx = x;
                        my = y;
                        foundSpace = true;
                    }
                else if (board[x][y] == (myType == 2 ? 1 : 2)) {// We took their spot already :)
                    found = false;
                    break;
                }
            }
            if (found && foundSpace) {
                System.out.println("AI calculated that " + mx + ", " + my + " would block!");
                board[mx][my] = myType == 2 ? 1 : 2;
                return;
            }
        }
        //Check the y axis for a 1-move win
        for (int x = 0; x < board.length; x++) {
            foundSpace = false;
            for (int y = 0; y < board.length; y++) {
                found = true;
                if (board[x][y] == 0)
                    if (foundSpace) {
                        found = false;
                        break;
                    } else {
                        mx = x;
                        my = y;
                        foundSpace = true;
                    }
                else if (board[x][y] == (myType == 2 ? 1 : 2)) {// We took their spot already :)
                    found = false;
                    break;
                }
            }
            if (found && foundSpace) {
                System.out.println("AI calculated that " + mx + ", " + my + " would block!");
                board[mx][my] = myType == 2 ? 1 : 2;
                return;
            }
        }
        //Check if a move diagonally from top-left can win
        foundSpace = false;
        for (int i = 0; i < board.length; i++) {
            found = true;
            if (board[i][i] == 0)
                if (foundSpace) {
                    found = false;
                    break;
                } else {
                    mx = i;
                    my = i;
                    foundSpace = true;
                }
            else if (board[i][i] == (myType == 2 ? 1 : 2)) {// We took their spot already :)
                found = false;
                break;
            }
        }
        if (found && foundSpace) {
            System.out.println("AI calculated that " + mx + ", " + my + " would block!");
            board[mx][my] = myType == 2 ? 1 : 2;
            return;
        }
        //Check if a move diagonally from top-right can win
        foundSpace = false;
        for (int i = 0; i < board.length; i++) {
            found = true;
            if (board[i][board.length - i - 1] == 0)
                if (foundSpace) {
                    found = false;
                    break;
                } else {
                    mx = i;
                    my = board.length - i - 1;
                    foundSpace = true;
                }
            else if (board[i][board.length - i - 1] == (myType == 2 ? 1 : 2)) {// We took their spot already :)
                found = false;
                break;
            }
        }
        if (found && foundSpace) {
            System.out.println("AI calculated that " + mx + ", " + my + " would block!");
            board[mx][my] = myType == 2 ? 1 : 2;
            return;
        }

        //Finally, as a last resort we have to move randomly.
        System.out.println("No blocks or instant-wins available, moving randomly...");
        int x, y;
        Random r = new Random();
        do {
            x = r.nextInt(board.length);
            y = r.nextInt(board.length);
        } while (board[x][y] != 0);
        board[x][y] = myType == 2 ? 1 : 2;
    }

    public void mouseClicked(MouseEvent e) {
        if (drawString || drawStringWait)
            return;
        int rx = e.getX();
        int ry = e.getY();
        int x = rx / 100; // x position within the board[] of this click
        int y = ry / 100; // y position within board[] of this click

        if (x >= board.length || y >= board.length || x < 0 || y < 0)
            return;

        // Uncomment these lines to mess around :)
        /*
        int type = e.getButton() == 1? 1: e.getButton() == 3? 2: 0;
        if(board[x][y] == type)
            board[x][y] = 0;
        else
            board[x][y] = type;
        if(true) // A simple return would throw a compilation error because the code below is redundant
            return;
        */
        if (e.getButton() != 1)
            return;
        if (board[x][y] == 0) {
            board[x][y] = myType;
            if(end)
                return;
            if (useAI)
                playAI();
            else {
                try {
                    cch.makeMove(x, y);
                } catch (IOException ioe) {
                    System.err.println("Could not send move to client.");
                    System.exit(-1);
                }
                text = "Waiting...";
                drawStringWait = true;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}