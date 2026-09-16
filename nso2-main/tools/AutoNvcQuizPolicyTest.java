public final class AutoNvcQuizPolicyTest {
    private static void check(int expected, int actual, String message) {
        if (expected != actual) throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
    }

    public static void main(String[] args) {
        check(1, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"3 lớp", "6 lớp", "12 lớp"}),
                "task 7 class-count answer must select 6 classes");
        check(1, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"Kiếm, phi tiêu, quạt", "Phi tiêu, quạt, cung", "Kunai, cung, đao"}),
                "task 7 chakra-class answer must select shuriken fan bow");
        check(2, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"Kiếm, phi tiêu, quạt", "Phi tiêu, quạt, cung", "Kiếm, Kunai, đao"}),
                "task 7 strength-class answer must select sword kunai blade");
        check(1, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"Kiáº¿m, phi tiÃªu, quáº¡t", "Phi tiÃªu, quáº¡t, cung", "Kunai, cung, Ä‘ao"}),
                "mojibake inner-class captions must still select option 2");
        check(2, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"Phi tiÃªu, Ä‘ao, cung", "Quáº¡t, kiáº¿m, kunai", "Kiáº¿m, kunai, Ä‘ao"}),
                "mojibake outer-class captions must still select option 3");
        check(-1, AutoNvcQuizPolicy.findTaskSevenAnswer(new String[]{"Nhận", "Nói chuyện"}),
                "ordinary NPC menus must not be treated as task 7 quizzes");
    }
}
