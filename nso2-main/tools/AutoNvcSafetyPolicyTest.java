public final class AutoNvcSafetyPolicyTest {
    private static void check(boolean expected, boolean actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        check(false, AutoNvcSafetyPolicy.shouldSuicideAfterMpDepletion(true, false),
                "disabled TS khi het MP must block suicide");
        check(true, AutoNvcSafetyPolicy.shouldSuicideAfterMpDepletion(true, true),
                "enabled TS khi het MP may use the existing recovery");
        check(false, AutoNvcSafetyPolicy.shouldSuicideAfterMpDepletion(false, true),
                "inactive auto must never trigger MP suicide");

        check(true, AutoNvcSafetyPolicy.shouldHoldJaianAtMapBoundary(17, 1, false, 1100, 1200, 1),
                "active NV17 escort must wait at the right map boundary");
        check(true, AutoNvcSafetyPolicy.shouldHoldJaianAtMapBoundary(17, 1, false, 90, 1200, -1),
                "active NV17 escort must wait at the left map boundary");
        check(false, AutoNvcSafetyPolicy.shouldHoldJaianAtMapBoundary(17, 1, true, 1100, 1200, 1),
                "report step may leave the map after server confirmation");
        check(false, AutoNvcSafetyPolicy.shouldHoldJaianAtMapBoundary(17, 1, false, 700, 1200, 1),
                "escort must keep leading before reaching the map boundary");

        check(true, AutoNvcSafetyPolicy.shouldChangeZoneBeforeJaianEscort(false, true),
                "leader present before Jaian escort must change zone");
        check(false, AutoNvcSafetyPolicy.shouldChangeZoneBeforeJaianEscort(false, false),
                "clean zone may start Jaian escort");
        check(false, AutoNvcSafetyPolicy.shouldChangeZoneBeforeJaianEscort(true, true),
                "active Jaian escort must never change zone");
    }
}
