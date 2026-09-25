import java.lang.reflect.Field;

public final class MenuTaskCompletionPriorityTest {
    public static void main(String[] args) throws Exception {
        SelectionRecorder recorder = new SelectionRecorder();
        MyVector items = new MyVector();
        items.addElement(new Command1("Hoan thanh nhiem vu", recorder, 101, null));
        items.addElement(new Command1("Noi chuyen", recorder, 202, null));

        Menu menu = new Menu();
        Field menuItems = Menu.class.getDeclaredField("menuItems");
        menuItems.setAccessible(true);
        menuItems.set(menu, items);
        menu.showMenu = true;

        assertTrue("completion menu is selected", menu.selectTaskForAuto(0, null));
        assertEquals("completion command wins over talk", 101, recorder.action);
    }

    private static void assertTrue(String name, boolean value) {
        if (!value) {
            throw new AssertionError(name);
        }
    }

    private static void assertEquals(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static final class SelectionRecorder implements IActionListener {
        int action = -1;

        public void perform(int action, Object data) {
            this.action = action;
        }
    }
}
