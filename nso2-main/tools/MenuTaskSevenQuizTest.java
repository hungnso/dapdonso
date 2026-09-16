import java.lang.reflect.Field;

/** Reproduces Task 7 captions as received from the server, including '?' replacements. */
public final class MenuTaskSevenQuizTest {
    private static int selectedAction = -1;

    private static final IActionListener RECORDER = new IActionListener() {
        public void perform(int action, Object parameter) {
            selectedAction = action;
        }
    };

    public static void main(String[] args) throws Exception {
        checkSelection(new String[]{"Ki?m, phi tiêu, qu?t", "Phi tiêu, qu?t, cung", "Kunai, cung, ?ao"}, 201,
                "inner-power");
        checkSelection(new String[]{"Phi tiêu, ?ao, cung", "Qu?t, Ki?m, Kunai", "Ki?m, Kunai, ?ao"}, 202,
                "outer-power");
    }

    private static void checkSelection(String[] captions, int expectedAction, String quizName) throws Exception {
        selectedAction = -1;
        Menu menu = new Menu();
        MyVector items = new MyVector();
        for (int i = 0; i < captions.length; ++i) {
            items.addElement(new Command1(captions[i], RECORDER, 200 + i, null));
        }

        Field menuItems = Menu.class.getDeclaredField("menuItems");
        menuItems.setAccessible(true);
        menuItems.set(menu, items);
        menu.showMenu = true;

        if (!menu.selectTaskForAuto(0, null)) {
            throw new AssertionError("Task 7 " + quizName + " menu was not selected");
        }
        if (selectedAction != expectedAction) {
            throw new AssertionError("Task 7 " + quizName + " expected action=" + expectedAction
                    + ", actual=" + selectedAction);
        }
    }
}
