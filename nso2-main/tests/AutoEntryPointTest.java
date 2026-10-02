import java.lang.reflect.Field;

public final class AutoEntryPointTest {
    public static void main(String[] args) throws Exception {
        Field taThuField = NSOT_MOB.class.getDeclaredField("ay");
        taThuField.setAccessible(true);
        TaskTaThuAuto taThu = (TaskTaThuAuto) taThuField.get(null);
        FakeAuto main = new FakeAuto();
        FakeAuto daily = new FakeAuto();
        daily.l = main;
        taThu.l = daily;
        NSOT_MOB.b = taThu;
        NSOT_MOB.mod_nst.f();
        check(taThu.l == daily && daily.l == main, "repeated Ta Thu menu start must preserve Daily resume");

        Char me = Char.getMyChar();
        me.cName = "member";
        Char.eg = true;
        NSOT_MOB.d = "leader";
        NSOT_MOB.mod_nst.b("leader", "att 1 5 0");
        check(taThu.l == daily && daily.l == main, "repeated party att must preserve Daily resume");
        NSOT_MOB.d();
        check(NSOT_MOB.b == daily, "Ta Thu completion returns to Daily");
        NSOT_MOB.d();
        check(NSOT_MOB.b == main, "Daily completion returns to original main auto");
        NSOT_MOB.g();
        System.out.println("AutoEntryPointTest PASS");
    }

    private static final class FakeAuto extends Auto {
        public void update() { }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
