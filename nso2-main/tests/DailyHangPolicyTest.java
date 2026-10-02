public final class DailyHangPolicyTest {
    public static void main(String[] args) {
        int[][] cases = {{1, -1}, {29, -1}, {30, 91}, {39, 91}, {40, 94}, {49, 94},
                {50, 105}, {59, 105}, {60, 114}, {69, 114}, {70, 125}, {89, 125}, {90, 157}, {130, 157}};
        for (int i = 0; i < cases.length; i++) {
            int actual = DailyHangPolicy.mapForLevel(cases[i][0]);
            if (actual != cases[i][1]) {
                throw new AssertionError("Wrong daily cave at level " + cases[i][0] + ": " + actual);
            }
        }
        System.out.println("DailyHangPolicyTest PASS");
    }
}
