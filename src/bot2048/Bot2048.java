/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot2048;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 *
 * @author anderson
 */
public class Bot2048 {

    public static final int UP = KeyEvent.VK_UP;
    public static final int DOWN = KeyEvent.VK_DOWN;
    public static final int LEFT = KeyEvent.VK_LEFT;
    public static final int RIGHT = KeyEvent.VK_RIGHT;
    public static final int EMPTY = 1;

    Robot robot = null;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle screenRectangle = new Rectangle(screenSize);
    int[][] matrix = new int[4][4];
    int backgroundRGB = Color.decode("#bbada0").getRGB();
    String[] tileColorCodes = new String[]{"#ccc0b2",//ccc0b2 ou ccc0b3
        "#eee4da",//2
        "#ede0c8",//4
        "#f2b179",//8
        "#f59563",//16
        "#f67c5f",//32
        "#f65e3b",//64
        "#edcf72",//128
        "#edcc61",//256
        "#edc850",//512
        "#",//1024
        "#"};//2028
    int[] tileColors = new int[tileColorCodes.length];
    int[] moves = new int[]{DOWN, LEFT, UP, RIGHT};

    public Bot2048() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {

        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = 0;
            }
        }

        for (int i = 0; i < tileColors.length; i++) {
            try {
                tileColors[i] = Color.decode(tileColorCodes[i]).getRGB();
            } catch (Exception e) {
                System.err.println("unknown tile color: " + (int) Math.pow(2, i));
                tileColors[i] = 0;
            }
        }
    }

    private static Point findOrigin(BufferedImage capture, int backgroundColor) {
        int x, y = 0, color;
        for (x = 0; x < capture.getWidth(); x++) {
            for (y = 0; y < capture.getHeight(); y++) {
                color = capture.getRGB(x, y);
                if (color == backgroundColor) {
                    return new Point(x, y);
                }
            }
        }
        throw new Error("Game not found!");
    }

    private static String colorToString(int color) {
        String hex = Integer.toHexString(color & 0xffffff);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return "#" + hex;
    }

    private boolean updateMatrix(BufferedImage capture, Point origin, int matrix[][], int[] tileColors) {
        int x, y, color;
        boolean change = false;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                x = origin.x + i * 120 + 30;
                y = origin.y + j * 120 + 20;
                //color = robot.getPixelColor(x, y).getRGB();
                color = capture.getRGB(x, y);
                boolean search = false;
                for (int k = 0; k < tileColors.length; k++) {
                    if (color == tileColors[k]) {
                        int tmp = (int) Math.pow(2, k);
                        if (matrix[j][i] != tmp) {
                            matrix[j][i] = tmp;
                            change = true;
                        }
                        search = true;
                        break;
                    }
                }
                if (!search) {
                    String hex = colorToString(color);
                    //out : ede2d3 || 
                    if (!hex.equals("#ccc0b1") && !hex.equals("#ccc0b2") && !hex.equals("#ccc0b3")) {
                        color = capture.getRGB(origin.x, origin.y);
                        hex = colorToString(color);
                        if (hex.equals("#d5c9bd")) {
                            throw new Error("Game Over!");
                        } else {
                            throw new Error("[" + i + "," + j + "]Color not found: " + hex);
                        }
                    } else {
                        matrix[j][i] = 1;
                    }
                }
            }
        }

        return change;
    }

    private static void printMatrix(int matrix[][]) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println("");
        }
    }

    private static int calcMove(int matrix[][], int dir) {
        int p = 0;
        switch (dir) {
            case UP:
            case DOWN:
                //coluna
                for (int j = 0; j < 4; j++) {
                    //linha
                    for (int i = 0; i < 3; i++) {
                        if (matrix[i][j] == EMPTY) {
                            continue;
                        }
                        for (int k = i + 1; k < 4; k++) {
                            if (matrix[i][j] == matrix[k][j]) {
                                p += matrix[i][j] * 2;
                                i = k;
                                break;
                            } else if (matrix[k][j] != EMPTY) {
                                break;
                            }
                        }
                    }
                }
                break;
            case LEFT:
            case RIGHT:
                //linha
                for (int i = 0; i < 4; i++) {
                    //coluna
                    for (int j = 0; j < 3; j++) {
                        if (matrix[i][j] == EMPTY) {
                            continue;
                        }
                        for (int k = j + 1; k < 4; k++) {
                            if (matrix[i][j] == matrix[i][k]) {
                                p += matrix[i][j] * 2;
                                j = k;
                                break;
                            } else if (matrix[i][k] != EMPTY) {
                                break;
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        return p;
    }

    public int step(Point origin, int delay) {
        //epera a imagem estabilizar
        robot.delay(delay);
        //capitura a tela
        BufferedImage capture = robot.createScreenCapture(screenRectangle);
        //atualiza a matrix
        updateMatrix(capture, origin, matrix, tileColors);
        int p;
        int max = 0;
        int move = 0;
        //escolhe o movimento de maior pontuação
        for (int m = 0; m < 2; m++) {
            p = calcMove(matrix, moves[m]);

            if (p > max) {
                max = p;
                move = m;
            }
        }
        if (max > 0) {
            robot.keyPress(moves[move]);
            robot.keyRelease(moves[move]);
            return max;
        } else {
            move = 0;
            while (!updateMatrix(capture, origin, matrix, tileColors)) {
                robot.keyPress(moves[move]);
                robot.keyRelease(moves[move]);
                robot.delay(delay);
                capture = robot.createScreenCapture(screenRectangle);
                move = (move == 3) ? 0 : move + 1;
            }
            return 0;
        }
    }

    public void best() {
        robot.delay(2000);
        robot.keyPress(KeyEvent.VK_R);
        robot.keyRelease(KeyEvent.VK_R);

        BufferedImage capture = robot.createScreenCapture(screenRectangle);
        Point origin = findOrigin(capture, backgroundRGB);

        boolean r = true;
        int w = 0;
        int m = 0;
        while (r) {
            try {
                w += step(origin, 50);
                m++;
            } catch (Error e) {
                if (e.getMessage().startsWith("G")) {
                    System.out.println("Game Over. Points: " + w + ", moves: " + m);
                    w = m = 0;
                    robot.keyPress(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_R);
                } else {
                    System.err.println("ERROR: " + e.getMessage());
                    System.exit(0);
                }
            }
        }
    }

    public void run() {

        best();

//        matrix = new int[][]{
//            {2, 64, 1, 2},
//            {1, 64, 2, 1},
//            {32, 32, 2, 1},
//            {61, 64, 2, 2}};
//
//        int p0;
//        int max0 = 0;
//        int move0 = 0;
//        for (int m = 0; m < 2; m++) {
//            p0 = calcMove(matrix, moves[m]);
//            if (p0 > max0) {
//                max0 = p0;
//                move0 = m;
//            }
//        }
//
//        System.out.println("(" + move0 + ") + " + max0);
//
//        System.exit(0);
//        robot.keyPress(KeyEvent.VK_ALT);
//        robot.keyPress(KeyEvent.VK_TAB);
//        robot.delay(10);
//        robot.keyRelease(KeyEvent.VK_ALT);
//        robot.keyRelease(KeyEvent.VK_TAB);
//        robot.delay(1000);
//
//        BufferedImage capture = robot.createScreenCapture(screenRectangle);
//        Point origin = findOrigin(capture, backgroundRGB);
//
//
//        int points = 0;
//        boolean forceMove = false;
//        if (origin != null) {
//            for (int i = 0; i < 500; i++) {
//                robot.delay(1000);
//                capture = robot.createScreenCapture(screenRectangle);
//                if (updateMatrix(capture, origin, matrix, tileColors) || forceMove) {
//                    System.out.println(".");
//                    int p;
//                    int max = 0;
//                    int move = 0;
//                    //escolhe o movimento de maior pontuação
//                    for (int m = 0; m < 2; m++) {
//                        p = calcMove(matrix, moves[m]);
//                        if (p > max) {
//                            max = p;
//                            move = m;
//                        }
//                    }
//                    forceMove = false;
//                    if (max == 0 || true) {
//                        i--;
//                        continue;
//                    }
//                    System.out.println("#" + i);
//                    printMatrix(matrix);
//
//                    points += max;
//                    //realiza o movimento
//                    robot.delay(1000);
//                    //Toolkit.getDefaultToolkit().beep();
//                    robot.keyPress(moves[move]);
//                    robot.keyRelease(moves[move]);
//                    System.out.println("(" + move + ") + " + max + " -> p:" + points);
//                } else {
//                    int move = 0;
//                    while (!updateMatrix(capture, origin, matrix, tileColors)) {
//                        System.out.println("~" + i + "." + move);
////                        printMatrix(matrix);
//
//                        robot.delay(1000);
//                        robot.keyPress(moves[move]);
//                        robot.keyRelease(moves[move]);
//                        robot.delay(1000);
//                        capture = robot.createScreenCapture(screenRectangle);
//                        move = (move == 3) ? 0 : move + 1;
//                    }
//                    System.out.println("#" + i + ".");
//                    //Toolkit.getDefaultToolkit().beep();
//                    forceMove = true;
//                }
//            }
//            System.out.println("end:");
//            printMatrix(matrix);
//        } else {
//            System.err.println("Game not found!");
//        }
//        System.exit(0);
//
//        int x, y = 0, color;
//        search:
//        for (x = 0; x < capture.getWidth(); x++) {
//            for (y = 0; y < capture.getHeight(); y++) {
//                color = capture.getRGB(x, y);
//                if (color == backgroundRGB) {
//                    break search;
//                }
//            }
//        }
//
//        final int x0 = x;
//        final int y0 = y;
//
//        JFrame frame = new JFrame();
//        frame.add(new JPanel() {
//
//            {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        while (true) {
//                            repaint();
//                            try {
//                                Thread.sleep(50);
//                            } catch (InterruptedException ex) {
//
//                            }
//                        }
//                    }
//                }.start();
//            }
//
//            @Override
//            public void paintComponent(Graphics g) {
//                g.drawImage(capture, 0, 0, this);
//                g.setColor(Color.getHSBColor((float) Math.random(), 1, 1));
//                g.fillRect(x0 - 2, y0 - 2, 4, 4);
//
//                int x, y;
//
//                for (int i = 0; i < 4; i++) {
//                    for (int j = 0; j < 4; j++) {
//                        x = x0 + i * 120 + 30;
//                        y = y0 + j * 120 + 20;
//
//                        g.fillRect(x - 2, y - 2, 4, 4);
//                    }
//                }
//            }
//
//        });
//
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setPreferredSize(new Dimension(500, 500));
//        frame.pack();
//        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Bot2048().run();
    }

}
