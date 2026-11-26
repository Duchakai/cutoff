import java.io.*;
import java.util.Random;

/** Sinh bộ dữ liệu thí nghiệm (seed cố định để tái lập). Mỗi file: mỗi dòng một số nguyên. */
public class DataGenerator {
    static final long SEED = 20241113L;

    public static void main(String[] args) throws IOException {
        String dir = args.length > 0 ? args[0] : "data";
        new File(dir).mkdirs();
        Random rnd = new Random(SEED);

        // Nhóm 1: ngẫu nhiên đều, các cỡ n = 10^3 .. 10^6
        for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000})
            write(dir + "/random_" + n + ".txt", random(n, rnd));

        // Nhóm 2: các dạng dữ liệu khác nhau, n = 10^6
        int n = 1_000_000;
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) sorted[i] = i;
        write(dir + "/sorted_" + n + ".txt", sorted);

        int[] rev = new int[n];
        for (int i = 0; i < n; i++) rev[i] = n - i;
        write(dir + "/reversed_" + n + ".txt", rev);

        int[] near = sorted.clone();                       // gần có thứ tự: 1% cặp bị hoán đổi ngẫu nhiên
        for (int k = 0; k < n / 100; k++) {
            int i = rnd.nextInt(n), j = rnd.nextInt(n);
            int t = near[i]; near[i] = near[j]; near[j] = t;
        }
        write(dir + "/nearlysorted_" + n + ".txt", near);

        int[] dup = new int[n];                            // rất nhiều khóa trùng: chỉ 100 giá trị
        for (int i = 0; i < n; i++) dup[i] = rnd.nextInt(100);
        write(dir + "/duplicates_" + n + ".txt", dup);
    }

    static int[] random(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
        return a;
    }

    static void write(String path, int[] a) throws IOException {
        try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            for (int x : a) w.println(x);
        }
        System.out.println("Đã ghi " + path + " (" + a.length + " số)");
    }
}
