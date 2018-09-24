import java.util.Random;

public class DotProdThreaded {
    public final static int SIZE = 700_000_000;
    public final static int THRESHOLD = SIZE / 32;
    public final static double TICKS = 1_000_000_000.0;

    public static class LongRes {
        public long value = 0;
    }

    public static void main(String[] args) {
        int[] x = new int[SIZE];
        int[] y = new int[SIZE];

        Random r = new Random();

        long start = System.nanoTime();

        for (int i=0; i<x.length; ++i) {
            x[i] = (int)(r.nextInt(100));
            y[i] = (int)(r.nextInt(100));
        }
        double sec = (System.nanoTime() - start) / TICKS;
        System.out.println("Init took " + sec + " sec.");

        start = System.nanoTime();
            long res2 = dotprodThread(x, y, 0, SIZE);
        sec = (System.nanoTime() - start) / TICKS;
        System.out.println("result: " + res2);
        System.out.println("\t\tasynch took " + sec + " sec");

        start = System.nanoTime();
            long res = dotprod(x, y, 0, SIZE);
        sec = (System.nanoTime() - start) / TICKS;
        System.out.println("result: " + res);
        System.out.println("\t\tsynch took " + sec + " sec");
    }

    private static long dotprod(int[] x, int[] y, int start, int end) {
        long r = 0;
        for (int i=start; i<end; ++i)
            r += Math.tan(x[i]*y[i]);
        return r;
    }

    private static long dotprodThread(int[] x, int[] y, int start, int end) {
        if (end - start < THRESHOLD)
            return dotprod(x, y, start, end);

        try {
            LongRes xRes = new LongRes();
            LongRes yRes = new LongRes();

            int mid = (start + end)/2;

            Thread th1 = new Thread(() -> {
                xRes.value = dotprodThread(x, y, start, mid);
            });

            th1.start();

            yRes.value = dotprodThread(x, y, mid, end);

            th1.join();

            return xRes.value + yRes.value;
        }
        catch (InterruptedException e) {
            return 0;
        }
    }
}
