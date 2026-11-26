import java.util.Arrays;

/**
 * Mergesort top-down dựng theo bài giảng (Sedgewick & Wayne, Algorithms 4th ed.):
 *  - mảng phụ aux được cấp phát MỘT LẦN, dùng chung cho mọi lời gọi đệ quy;
 *  - merge() copy a[lo..hi] sang aux rồi trộn ngược về a;
 *  - cải tiến CUTOFF: đoạn có độ dài <= cutoff được sắp xếp bằng Insertion sort.
 * Các cải tiến khác (bỏ qua merge nếu đã có thứ tự, đổi vai trò a/aux) CỐ Ý KHÔNG dùng,
 * để thí nghiệm chỉ đo ảnh hưởng của CUTOFF.
 */
public class MergeSort {

    /** Sắp xếp a[] tăng dần; đoạn con có độ dài <= cutoff dùng insertion sort. */
    public static void sort(int[] a, int cutoff) {
        int[] aux = new int[a.length];
        sort(a, aux, 0, a.length - 1, cutoff);
    }

    private static void sort(int[] a, int[] aux, int lo, int hi, int cutoff) {
        if (hi <= lo) return;                       // 0 hoặc 1 phần tử
        if (hi - lo + 1 <= cutoff) {                // đoạn đủ nhỏ -> insertion sort
            insertionSort(a, lo, hi);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid, cutoff);
        sort(a, aux, mid + 1, hi, cutoff);
        merge(a, aux, lo, mid, hi);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi) {
        for (int k = lo; k <= hi; k++) aux[k] = a[k];
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if      (i > mid)          a[k] = aux[j++];
            else if (j > hi)           a[k] = aux[i++];
            else if (aux[j] < aux[i])  a[k] = aux[j++];   // ổn định: bằng nhau lấy bên trái
            else                       a[k] = aux[i++];
        }
    }

    /** Insertion sort (dạng đổi chỗ liền kề như bài giảng) trên a[lo..hi]. */
    static void insertionSort(int[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            for (int j = i; j > lo && a[j] < a[j - 1]; j--) {
                int t = a[j]; a[j] = a[j - 1]; a[j - 1] = t;
            }
        }
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i] < a[i - 1]) return false;
        return true;
    }

    /** Kiểm thử tính đúng đắn: so với Arrays.sort trên nhiều cỡ mảng và nhiều cutoff. */
    public static void main(String[] args) {
        java.util.Random rnd = new java.util.Random(1);
        int[] cutoffs = {1, 2, 3, 7, 10, 16, 50};
        for (int n : new int[]{0, 1, 2, 3, 5, 8, 17, 100, 1001, 10000}) {
            for (int c : cutoffs) {
                int[] a = new int[n];
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(50) - 25;   // nhiều trùng lặp
                int[] expected = a.clone();
                Arrays.sort(expected);
                sort(a, c);
                if (!Arrays.equals(a, expected)) {
                    System.out.println("SAI: n=" + n + ", cutoff=" + c);
                    System.exit(1);
                }
            }
        }
        System.out.println("MergeSort: tất cả test đúng.");
    }
}
