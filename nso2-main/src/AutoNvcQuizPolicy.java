/** Exact answer recognition for the three introductory Task 7 school quizzes. */
public final class AutoNvcQuizPolicy {
    private AutoNvcQuizPolicy() {
    }

    public static int findTaskSevenAnswer(String[] captions) {
        if (captions == null || captions.length < 2) return -1;
        int sixClasses = -1;
        int chakraClasses = -1;
        int strengthClasses = -1;
        int classCountChoices = 0;
        int classGroupChoices = 0;
        for (int i = 0; i < captions.length; ++i) {
            String caption = lower(captions[i]);
            if (contains(caption, "lớp", "lop", "lá»›p")) {
                ++classCountChoices;
                if (contains(caption, "6 lớp", "6 lop", "6 lá»›p")) sixClasses = i;
            }
            boolean sword = contains(caption, "kiếm", "kiem", "kiáº¿m")
                    && contains(caption, "kunai") && contains(caption, "đao", "dao", "Ä‘ao");
            boolean chakra = contains(caption, "phi tiêu", "phi tieu", "phi tiÃªu")
                    && contains(caption, "quạt", "quat", "quáº¡t") && contains(caption, "cung");
            // Some server packets preserve the ASCII word stems but corrupt
            // accented characters. These patterns remain unique among the
            // three visible answers for the Task 7 inner/outer quiz.
            if (!sword) sword = caption.indexOf("ki") >= 0 && caption.indexOf("kunai") >= 0
                    && (caption.indexOf("dao") >= 0 || caption.indexOf("ao") >= 0);
            if (!chakra) chakra = caption.indexOf("phi") >= 0 && caption.indexOf("cung") >= 0
                    && caption.indexOf("qu") >= 0;
            if (sword || chakra) ++classGroupChoices;
            if (sword) strengthClasses = i;
            if (chakra) chakraClasses = i;
        }
        if (classCountChoices >= 2 && sixClasses >= 0) return sixClasses;
        if (strengthClasses >= 0) return strengthClasses;
        if (chakraClasses >= 0) return chakraClasses;
        return -1;
    }

    private static boolean contains(String text, String one, String two, String three) {
        return text.indexOf(one) >= 0 || text.indexOf(two) >= 0 || text.indexOf(three) >= 0;
    }

    private static boolean contains(String text, String one) {
        return text.indexOf(one) >= 0;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}
