import java.io.*;
import java.util.*;

/**
 * Thí nghiệm đo thời gian MergeSort theo CUTOFF.
 * Cách dùng: Experiment <thư mục dữ liệu> <file csv kết quả>
 * Thiết kế:
 *  - Với mỗi file dữ liệu và mỗi lượt (trial), chạy TẤT CẢ các cutoff theo THỨ TỰ NGẪU NHIÊN
 *    (tránh sai lệch do JIT/GC/nhiệt độ CPU thiên vị một cutoff nào đó).
 *  - Mỗi lần sort đều copy dữ liệu gốc sang mảng mới; chỉ đo thời gian MergeSort.sort (System.nanoTime).
 *  - Có giai đoạn khởi động (warm-up) để JIT biên dịch xong trước khi đo.
 *  - Sau mỗi lần sort kiểm tra mảng đã được sắp xếp (ngoài đoạn tính giờ).
 */
public class Experiment {
    static final int[] CUTOFFS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 20,
                                  24, 28, 32, 40, 48, 64, 96, 128};

    static int trialsFor(String name, int n) {
        if (!name.startsWith("random")) return 15;      // các dạng dữ liệu khác (n = 10^6)
        if (n <= 1_000) return 1000;
        if (n <= 10_000) return 300;
        if (n <= 100_000) return 60;
        return 25;
    }

    public static void main(String[] args) throws IOException {
        String dir = args[0];
        String out = args[1];
        File[] files = new File(dir).listFiles((d, f) -> f.endsWith(".txt"));
        Arrays.sort(files, Comparator.comparing(File::getName));

        warmUp(files);

        try (PrintWriter w = new PrintWriter(new FileWriter(out))) {
            w.println("dataset,n,cutoff,trial,nanos");
            for (File f : files) {
                String name = f.getName().replace(".txt", "");
                int[] data = read(f);
                int n = data.length;
                int trials = trialsFor(name, n);
                Random rnd = new Random(42);
                List<Integer> order = new ArrayList<>();
                for (int c : CUTOFFS) order.add(c);

                System.out.printf("Đang đo %s (n=%d, %d lượt x %d cutoff)...%n", name, n, trials, CUTOFFS.length);
                for (int t = 0; t < trials; t++) {
                    Collections.shuffle(order, rnd);
                    for (int c : order) {
                        int[] a = data.clone();
                        long t0 = System.nanoTime();
                        MergeSort.sort(a, c);
                        long t1 = System.nanoTime();
                        if (!MergeSort.isSorted(a)) throw new IllegalStateException("Sort sai! " + name + " cutoff=" + c);
                        w.println(name + "," + n + "," + c + "," + t + "," + (t1 - t0));
                    }
                }
                w.flush();
            }
        }
        System.out.println("Xong. Kết quả: " + out);
    }

    static void warmUp(File[] files) throws IOException {
        System.out.println("Khởi động JIT...");
        for (File f : files) {
            if (!f.getName().equals("random_100000.txt") && !f.getName().equals("random_1000000.txt")) continue;
            int[] data = read(f);
            for (int round = 0; round < 3; round++)
                for (int c : CUTOFFS) { int[] a = data.clone(); MergeSort.sort(a, c); }
        }
    }

    static int[] read(File f) throws IOException {
        List<int[]> chunks = new ArrayList<>();
        int[] buf = new int[1 << 16]; int len = 0, total = 0;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (len == buf.length) { chunks.add(buf); buf = new int[1 << 16]; len = 0; }
                buf[len++] = Integer.parseInt(line.trim()); total++;
            }
        }
        int[] res = new int[total]; int pos = 0;
        for (int[] c : chunks) { System.arraycopy(c, 0, res, pos, c.length); pos += c.length; }
        System.arraycopy(buf, 0, res, pos, len);
        return res;
    }
}
