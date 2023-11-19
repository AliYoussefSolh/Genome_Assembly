import java.util.Arrays;

public class SuffixArrayConstruction {
    static final int N = 3_000_007; // Adjust according to the maximum input size

    static char[] s = new char[N];
    static int[] sa = new int[N], rk = new int[N << 1], oldRk = new int[N], oldSa = new int[N], cnt = new int[N];

    public static int[] buildSuffix(String input) {
        int m = 127; // Assuming ASCII characters

        // Reading input
        // String input = "banana"; // Replace with your input
        int n = input.length();
        input.getChars(0, n, s, 1);

        // Counting sort of the original array begins
        for (int i = 1; i <= n; ++i)
            ++cnt[rk[i] = s[i]];
        for (int i = 1; i <= m; ++i)
            cnt[i] += cnt[i - 1];
        for (int i = n; i >= 1; --i)
            sa[cnt[rk[i]]--] = i;

        for (int d = 1; d < n; d <<= 1, m = n) {
            int rank = 0;
            System.arraycopy(sa, 1, oldSa, 1, n);
            for (int i = n - d + 1; i <= n; i++)
                sa[++rank] = i;

            for (int i = 1; i <= n; i++)
                if (oldSa[i] > d)
                    sa[++rank] = oldSa[i] - d;

            Arrays.fill(cnt, 0);
            System.arraycopy(sa, 1, oldSa, 1, n);
            for (int i = 1; i <= n; ++i)
                ++cnt[rk[oldSa[i]]];
            for (int i = 1; i <= m; ++i)
                cnt[i] += cnt[i - 1];
            for (int i = n; i >= 1; --i)
                sa[cnt[rk[oldSa[i]]]--] = oldSa[i];

            System.arraycopy(rk, 1, oldRk, 1, n);
            rank = 0;
            for (int i = 1; i <= n; ++i) {
                if (oldRk[sa[i]] == oldRk[sa[i - 1]] &&
                        oldRk[sa[i] + d] == oldRk[sa[i - 1] + d])
                    rk[sa[i]] = rank;
                else
                    rk[sa[i]] = ++rank;
            }

            if (rank == n)
                break;
        }

        for (int i = 1; i <= n; ++i) {
            sa[i] -= 1;
            // System.out.print(sa[i] + " ");
        }
        return sa;

    }

    // public static void main(String[] args) {

    // }
}
