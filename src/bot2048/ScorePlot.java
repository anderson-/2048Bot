/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot2048;

import static bot2048.Bot2048.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 *
 * @author anderson
 */
public class ScorePlot {

    private final ArrayList<ArrayList<int[]>> scoreList;
    private int s1 = 0;
    private int s2 = 0;
    private JFrame window = null;
    private double sx = 1, sy = 1;
    private int game = -1;

    public ScorePlot(int width, int height, int time, int maxScore) {
        scoreList = new ArrayList<>();
        createFrame(width, height);
        sx = (double) width / time;
        sy = (double) height / maxScore;

    }

    private void createFrame(int width, int height) {
        window = new JFrame("Score x Time");
        window.add(new JPanel() {

            {
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            repaint();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {

                            }
                        }
                    }
                }.start();
            }

            @Override
            public void paintComponent(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                g.setColor(Color.black);
                g.fillRect(0, 0, width, height);
                for (int i = 0; i < scoreList.size(); i++) {
                    int k = 0;
                    for (int j = 0; j < scoreList.get(i).size(); j++) {
                        int[] value = scoreList.get(i).get(j);
                        int s = value[0];
                        int type = value[1];

                        int y = (int) (s * sy);
                        int x = (int) (j * sx);

                        if (i != game) {
                            g.setColor(Color.red);
                        } else {
                            switch (type) {
                                case MOVE_LOCAL:
                                    g.setColor(Color.magenta);
                                    break;
                                case MOVE_FUTURE:
                                    g.setColor(Color.yellow);
                                    break;
                                case MOVE_NONE:
                                    g.setColor(Color.blue);
                                    break;
                                case 3:
                                    g.setColor(Color.green);
                                    break;
                                case 4:
                                    g.setColor(Color.cyan);
                                    g.drawString("[" + k + "]", x, height - y);
                                    k++;
                                    break;
                                default:
                                    g.setColor(Color.orange);
                                    break;
                            }
                        }
                        g.fillRect(x, height - y, 2, 2);

                        if (j == scoreList.get(i).size() - 1) {
                            g.drawString("" + s, x, height - y);
                        }
                    }
                }
            }

        });

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setPreferredSize(new Dimension(width, height));
        window.pack();

        Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        Dimension windowSize = new Dimension(window.getPreferredSize());
        int wdwLeft = screenSize.width / 2 - windowSize.width / 2 - 600;
        int wdwTop = screenSize.height / 2 - windowSize.height / 2 - 300;
        window.setLocation(wdwLeft, wdwTop);
    }

    public void show() {
        window.setVisible(true);
    }

    public void hide() {
        window.setVisible(false);
    }

    public void add(int score) {
        add(score, 0);
    }

    int k = 20;

    public void add(int score, int type) {
        if (game < scoreList.size()) {
            int size = scoreList.get(game).size();
            if (size > 10) {
                s1 = scoreList.get(game).get(size - 10)[0];
            }
            s2 = score;
            k--;
            if (s2 > s1 + 128 && k >= 0) {
                k = 20;
                scoreList.get(game).add(new int[]{score, 4});
            } else {
                scoreList.get(game).add(new int[]{score, type});
            }
        }
    }

    void pushGame() {
        scoreList.add(new ArrayList<int[]>());
        game++;
    }

}
