package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton increment = new JButton("increment");
    private final JButton decrement = new JButton("decrement");


    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Interface construction : 
        final JPanel contentPanel = new JPanel();
        display.setText("0");
        contentPanel.add(display);
        contentPanel.add(decrement);
        contentPanel.add(stop);
        contentPanel.add(increment);
        this.getContentPane().add(contentPanel);
        this.setVisible(true);

        final CounterAgent counterAgent = new CounterAgent();
        final WatcherAgent watcherAgent = new WatcherAgent(counterAgent);
        new Thread(counterAgent).start();
        new Thread(watcherAgent).start();

        stop.addActionListener((e) -> counterAgent.stopExecution());
        increment.addActionListener((e) -> counterAgent.setDirection(Directions.INC));
        decrement.addActionListener((e) -> counterAgent.setDirection(Directions.DEC));
    }

    private class WatcherAgent implements Runnable {

        private static final int STOPPING_COUNTDOWN = 10;
        private final CounterAgent targetCounter;

        WatcherAgent(final CounterAgent counterAgent) {
            this.targetCounter = counterAgent;
        }
        public void run() {
            try {
                Thread.sleep(STOPPING_COUNTDOWN * 1000);
                targetCounter.stopExecution();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class CounterAgent implements Runnable {

        private volatile Directions direction;
        private volatile boolean stop;
        private int counter; 

        CounterAgent() {
            /**
             * Default values sets to stop the counter
             */
            this.direction = Directions.STEADY;
            this.counter = 0;
            this.stop = false;
        }

        public void run() {
            try {
                while (!this.stop) {
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(Integer.toString(this.counter)));
                    this.calcCounterValue();
                    Thread.sleep(100);
                }
            } catch (InvocationTargetException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        private void calcCounterValue() {
            this.counter = this.counter + this.direction.getStep();
        }

        private void setDirection(final Directions newDirection) {
            this.direction = newDirection;
        }

        private void stopExecution() {
            this.stop = true;
            AnotherConcurrentGUI.this.stop.setEnabled(false);
            AnotherConcurrentGUI.this.increment.setEnabled(false);
            AnotherConcurrentGUI.this.decrement.setEnabled(false);
        }
    }

    private enum Directions {
        INC("increment", 1), 
        DEC("decrement", -1),
        STEADY("steady", 0);

        private final String name; 
        private final int step; 

        Directions(final String inName, final int inStep) {
            this.name = inName;
            this.step = inStep;
        }

        public String getName() {
            return this.name;
        }

        public int getStep() {
            return this.step;
        }
    }
}
