public final class JaianEscortSafetyTest {
    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        assertTrue(JaianEscortSafety.isEscortStep(17, 1, false),
                "NV17 escort step must be recognized as an escort state");
        assertTrue(!JaianEscortSafety.isEscortStep(17, 1, true),
                "NV17 report step must leave escort mode");
        assertTrue(JaianEscortSafety.mayRouteTo(17, 1, false, 33),
                "NV17 escort may only route into the Jaian map");
        assertTrue(!JaianEscortSafety.mayRouteTo(17, 1, false, 22),
                "NV17 escort must not route to the HP shop");
        assertTrue(!JaianEscortSafety.mayRouteTo(17, 1, false, 22, true),
                "NV17 must not route to the shop after Jaian is active");
        assertTrue(JaianEscortSafety.shouldUsePotion(69),
                "NV17 should heal before HP drops below the danger threshold");
        assertTrue(!JaianEscortSafety.shouldUsePotion(70),
                "NV17 should not spend a potion at the safety threshold");
    }
}
