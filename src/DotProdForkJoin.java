import java.util.Random;
import java.util.concurrent.*;

public class DotProdForkJoin {
    public final static int SIZE = 200_000_000;
    public final static int THRESHOLD = SIZE / 32;
    public final static double TICKS = 1_000_000_000.0;

    public static class DotProdFJ extends RecursiveTask<Long> {
        final int[] x;
        final int[] y;
        final int start;
        final int end;

        public DotProdFJ(int[] x, int[] y, int start, int end) {
            this.x = x;
            this.y = y;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start < THRESHOLD)
                return dotprod();

            int mid = (start + end)/2;

            DotProdFJ left = new DotProdFJ(x, y, start, mid);
            left.fork();
            DotProdFJ right = new DotProdFJ(x, y, mid, end);
            return right.compute() + left.join();

        }

        public long dotprod() {
            long r = 0;
            for (int i=start; i<end; ++i)
                r += (long)Math.tan(x[i]*y[i]);
            return r;
        }
    }

    public static class RandInit extends RecursiveAction {
        int[] x;
        int start;
        int end;
        Random r;

        RandInit(int[] x, int start, int end) {
            this.x = x;
            this.start = start;
            this.end = end;
            r = new Random();
        }

        public void initty() {
            for (int i=start; i<end; ++i) {
                x[i] = (int) (r.nextInt(100));
            }
        }

        @Override
        protected void compute() {
            if (end - start < THRESHOLD)
                initty();
            else {
                int mid = (start + end) / 2;
                RandInit left = new RandInit(x, start, mid);
                left.fork();
                RandInit right = new RandInit(x, mid, end);
                right.compute();
                left.join();
            }
        }
    }

    public static void main(String[] args) {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Processors: " + processors);

        int[] x = new int[SIZE];
        int[] y = new int[SIZE];

        ForkJoinPool pool = new ForkJoinPool();

        long start = System.nanoTime();
        Future<Void> f1 = pool.submit(new RandInit(x, 0, SIZE));
        Future<Void> f2 = pool.submit(new RandInit(y, 0, SIZE));

        try {
            f1.get();
            f2.get();
        }
        catch (InterruptedException ignore) {
        }
        catch (ExecutionException ignore) {
        }

        System.out.println("init took " + (System.nanoTime() - start)/TICKS);

        DotProdFJ dp = new DotProdFJ(x, y,0, SIZE);

        start = System.nanoTime();
        long res2 = dp.dotprod();
        System.out.println("synch took " + (System.nanoTime() - start)/TICKS);
        System.out.println(res2);

        start = System.nanoTime();
        long res = pool.invoke(dp);
        System.out.println("fork join took " + (System.nanoTime() - start)/TICKS);
        System.out.println(res);



    }
}
