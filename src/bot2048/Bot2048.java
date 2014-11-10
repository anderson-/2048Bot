/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot2048;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
    public static final int MOVE_LOCAL = 0;
    public static final int MOVE_FUTURE = 1;
    public static final int MOVE_NONE = 2;

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
        "#edc53f",//1024
        "#"};//2048
    int[] tileColors = new int[tileColorCodes.length];
    int[] moves = new int[]{DOWN, LEFT, UP, RIGHT};
    //int[] moves = new int[]{DOWN, LEFT, RIGHT, UP};
    static Logger logger;

    static {
        logger = Logger.getLogger("Bot2048");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("logs/Bot2048-" + System.currentTimeMillis() + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                LOG(Level.SEVERE, "unknown tile color: " + (int) Math.pow(2, i));
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

    private static boolean updateMatrix(Robot robot, Point origin, int matrix[][], int[] tileColors) {
        int x, y, color;
        boolean change = false;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                x = origin.x + i * 120 + 30;
                y = origin.y + j * 120 + 20;
                color = robot.getPixelColor(x, y).getRGB();
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
                        if (colorToString(robot.getPixelColor(origin.x, origin.y).getRGB()).equals("#d5c9bd")) {
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
            System.out.println();
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

    public static int binlog(int bits) // returns 0 for bits=0
    {
        int log = 0;
        if ((bits & 0xffff0000) != 0) {
            bits >>>= 16;
            log = 16;
        }
        if (bits >= 256) {
            bits >>>= 8;
            log += 8;
        }
        if (bits >= 16) {
            bits >>>= 4;
            log += 4;
        }
        if (bits >= 4) {
            bits >>>= 2;
            log += 2;
        }
        return log + (bits >>> 1);
    }

    public static int step(Point origin, int delay, Robot robot, int[][] matrix, int[][] tmpMatrix, int[] tileColors, int[] moves, int score, ScorePlot sp) {
        //epera a imagem estabilizar
        robot.delay(delay);
        //atualiza a matrix
        updateMatrix(robot, origin, matrix, tileColors);

        int tmpScore = 0;
        int maxScore = 0;
        int chosenMove = 0;

        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < moves.length; b++) {
                tmpScore = calcAfterMove(matrix, tmpMatrix, moves[a], moves[b]);
                if (tmpScore > maxScore) {
                    //guarda a pontuação aproximada
                    maxScore = tmpScore;
                    chosenMove = a;
                }
            }
        }

        int threshold = 64;//binlog(score) * 3;

        int maxScore2 = 0;
        int chosenMove2 = 0;
        chosenMove2 = 0;
        maxScore2 = 0;
        for (int a = 0; a < moves.length; a++) {
            tmpScore = calcMove(matrix, moves[a]);

            if (tmpScore > maxScore2) {
                maxScore2 = tmpScore;
                chosenMove2 = a;
            }
        }

        if (maxScore < maxScore2 + 8 || maxScore < threshold || chosenMove != chosenMove2) {
            chosenMove = chosenMove2;
            maxScore = maxScore2;
            if (sp != null) {
                sp.add(score, MOVE_LOCAL);
            }
        } else {
            //calcula a ponuação real
            maxScore = calcMove(matrix, moves[chosenMove]);
            if (sp != null) {
                sp.add(score, MOVE_FUTURE);
            }
        }

        if (maxScore > 0) {
            robot.keyPress(moves[chosenMove]);
            robot.keyRelease(moves[chosenMove]);
            return maxScore;
        } else {
            chosenMove = 0;
            while (!updateMatrix(robot, origin, matrix, tileColors)) {
                robot.keyPress(moves[chosenMove]);
                robot.keyRelease(moves[chosenMove]);
                robot.delay(delay);
                chosenMove = (chosenMove == 3) ? 0 : chosenMove + 1;
            }

            if (sp != null) {
                sp.add(score, MOVE_NONE);
            }
            return 0;
        }
    }

    public static void rotateLeft(int[][] matrix) {
        int n = 4; //tamanho
        int tmp;
        for (int i = 0; i < n / 2; i++) {
            for (int j = i; j < n - i - 1; j++) {
                tmp = matrix[i][j];
                matrix[i][j] = matrix[j][n - i - 1];
                matrix[j][n - i - 1] = matrix[n - i - 1][n - j - 1];
                matrix[n - i - 1][n - j - 1] = matrix[n - j - 1][i];
                matrix[n - j - 1][i] = tmp;
            }
        }
    }

    public static int calcAfterMove(int[][] matrix, int[][] tmpMatrix, int move, int nextMove) {

        for (int i = 0; i < 4; i++) {
            System.arraycopy(matrix[i], 0, tmpMatrix[i], 0, 4);
        }

        switch (move) {
            case UP:
                break;
            case DOWN:
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                break;
            case LEFT:
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                break;
            case RIGHT:
                rotateLeft(tmpMatrix);
                break;
        }

        int moveScore = 0;
        boolean validMove = false;
        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int m = -1; //mesclar ou mover
                for (int f = i - 1; f >= 0; f--) {
                    //permuta
                    if (tmpMatrix[f][j] == EMPTY) {
                        m = f;
                    } else if (tmpMatrix[f][j] == tmpMatrix[i][j]) {
                        m = f;
                        break;
                    } else {
                        break;
                    }
                }
                if (m != -1) {
                    if (tmpMatrix[m][j] == EMPTY) {
                        tmpMatrix[m][j] = tmpMatrix[i][j];
                        tmpMatrix[i][j] = EMPTY;
                    } else {
                        moveScore += tmpMatrix[m][j] = tmpMatrix[i][j] * 2;
                        tmpMatrix[i][j] = EMPTY;
                    }
                    validMove = true;
                }

            }
        }

        if (!validMove) {
            return -1;
        }

        switch (move) {
            case UP:
                break;
            case DOWN:
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                break;
            case LEFT:
                rotateLeft(tmpMatrix);
                break;
            case RIGHT:
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                rotateLeft(tmpMatrix);
                break;
        }

        int nextMoveScoreAprox = calcMove(tmpMatrix, nextMove);

        return moveScore + nextMoveScoreAprox;
    }

    public void playGame(int[] strategyMoves, boolean reset, ScorePlot sp) {
        if (sp != null) {
            sp.pushGame();
        }
        //espera seleção da tela
        robot.delay(2000);
        //reseta o jogo
        if (reset) {
            robot.keyPress(KeyEvent.VK_R);
            robot.keyRelease(KeyEvent.VK_R);
        }
        robot.delay(1000);

        //encontra a posição das celulas da grade
        BufferedImage capture = robot.createScreenCapture(screenRectangle);
        Point origin = findOrigin(capture, backgroundRGB);

        int[][] tmpMatrix = new int[4][4];

        boolean r = true;
        int score = 0;
        int steps = 0;
        while (r) {
            try {
                score += step(origin, 60, robot, matrix, tmpMatrix, tileColors, strategyMoves, score, sp);
                steps++;
            } catch (Error e) {
                if (e.getMessage().startsWith("G")) {
                    LOG(Level.INFO, "Game Over. Points: " + score + ", moves: " + steps);
                    return;
                    //w = m = 0;
                    //robot.keyPress(KeyEvent.VK_R);
                    //robot.keyRelease(KeyEvent.VK_R);
                } else {
                    LOG(Level.SEVERE, "ERROR: " + e.getMessage());
                    //System.exit(0);
                }
            }
        }
    }

    public void perfectMoveTest() {
        ArrayList<int[]> permutations = permutations(moves);

        for (int k = 6; k < permutations.size(); k++) {
            for (int i = 0; i < 4; i++) {
                LOG(Level.INFO, ">" + permutations.get(k)[i]);
            }
            for (int i = 0; i < 10; i++) {
                playGame(permutations.get(k), true, null);
            }
        }
    }

    public void run() {

//        //espera seleção da tela
//        robot.delay(2000);
//        //encontra a posição das celulas da grade
//        BufferedImage capture = robot.createScreenCapture(screenRectangle);
//        Point origin = findOrigin(capture, backgroundRGB);
//        //atualiza a matrix
//        updateMatrix(robot, origin, matrix, tileColors);
//        int friendlyMove = friendlyMove(matrix, 0, 32);
//        if (friendlyMove >= 0) {
//            System.out.println("fm: " + friendlyMove);
//        }
        ScorePlot sp = new ScorePlot(600, 300, 500, 8000);
        sp.show();
        for (int i = 0; i < 1000; i++) {
            playGame(moves, true, sp);
        }

//                matrix = new int[][]{
//                    {2, 64, 1, 2},
//                    {1, 64, 2, 1},
//                    {32, 32, 2, 1},
//                    {61, 64, 2, 2}};
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
        //        LOG(Level.INFO,"(" + move0 + ") + " + max0);
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
        //                    LOG(Level.INFO,".");
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
        //                    LOG(Level.INFO,"#" + i);
        //                    printMatrix(matrix);
        //
        //                    points += max;
        //                    //realiza o movimento
        //                    robot.delay(1000);
        //                    //Toolkit.getDefaultToolkit().beep();
        //                    robot.keyPress(moves[move]);
        //                    robot.keyRelease(moves[move]);
        //                    LOG(Level.INFO,"(" + move + ") + " + max + " -> p:" + points);
        //                } else {
        //                    int move = 0;
        //                    while (!updateMatrix(capture, origin, matrix, tileColors)) {
        //                        LOG(Level.INFO,"~" + i + "." + move);
        ////                        printMatrix(matrix);
        //
        //                        robot.delay(1000);
        //                        robot.keyPress(moves[move]);
        //                        robot.keyRelease(moves[move]);
        //                        robot.delay(1000);
        //                        capture = robot.createScreenCapture(screenRectangle);
        //                        move = (move == 3) ? 0 : move + 1;
        //                    }
        //                    LOG(Level.INFO,"#" + i + ".");
        //                    //Toolkit.getDefaultToolkit().beep();
        //                    forceMove = true;
        //                }
        //            }
        //            LOG(Level.INFO,"end:");
        //            printMatrix(matrix);
        //        } else {
        //            LOG(Level.SEVERE,"Game not found!");
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
        {

        }
    }

    public static void main(String[] args) throws IOException {
        String htmlFilePath = "2048-master/index.html"; // path to your new file
        File htmlFile = new File(htmlFilePath);

        // open the default web browser for the HTML page
        Desktop.getDesktop().browse(htmlFile.toURI());
        new Bot2048().run();
    }

    static ArrayList<int[]> permutations(int[] a) {
        ArrayList<int[]> ret = new ArrayList<int[]>();
        permutation(a, 0, ret);
        return ret;
    }

    public static void permutation(int[] arr, int pos, ArrayList<int[]> list) {
        if (arr.length - pos == 1) {
            list.add(arr.clone());
        } else {
            for (int i = pos; i < arr.length; i++) {
                swap(arr, pos, i);
                permutation(arr, pos + 1, list);
                swap(arr, pos, i);
            }
        }
    }

    public static void swap(int[] arr, int pos1, int pos2) {
        int h = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = h;
    }

    private static void LOG(Level level, String string) {
        logger.log(level, string);
    }

}
