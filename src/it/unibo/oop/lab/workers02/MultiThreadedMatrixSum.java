package it.unibo.oop.lab.workers02;

import java.util.Arrays;
import java.util.stream.IntStream;

public class MultiThreadedMatrixSum implements SumMatrix {
    
    private final int nthread;
    
   
    public MultiThreadedMatrixSum(final int inputThread) {
        this.nthread = inputThread;
    }
    
    
    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nrows;
        private double res;

     
        Worker(final double[][] inMatrix, final int startpos, final int nrows) {
            this.matrix = inMatrix;
            this.startpos = startpos;
            this.nrows = nrows;
        }

        @Override
        public void run() {
            for (int i = this.startpos; i < matrix.length && i < this.startpos + this.nrows; i++) {
                this.res += Arrays.stream(this.matrix[i]).sum();
            }
        }

        public double getResult() {
            return this.res;
        }
    }
    
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;
        return IntStream.iterate(0, start -> start + size)
                .limit(nthread)
                .mapToObj(start -> new Worker(matrix, start, size))
                .peek(Thread::start)
                .peek(MultiThreadedMatrixSum::joinUninterruptibly)
                .mapToDouble(Worker::getResult)
                .sum();
    }

}
