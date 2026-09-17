public final class JaianEscortSafetyTest {
    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
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
        assertTrue(JaianEscortSafety.shouldReturnTownToRestock(49, false),
                "NV17 must return to town before Jaian appears when fewer than 50 HP potions remain");
        assertTrue(!JaianEscortSafety.shouldReturnTownToRestock(50, false),
                "NV17 must start escorting once it has the minimum 50 HP potions");
        assertTrue(!JaianEscortSafety.shouldReturnTownToRestock(0, true),
                "NV17 must never abandon an already-active Jaian escort to restock");
        assertTrue(JaianEscortSafety.shouldKeepRestocking(49, false),
                "NV17 must not begin escorting with fewer than 50 HP potions after a failed purchase attempt");
        assertTrue(!JaianEscortSafety.shouldKeepRestocking(49, true),
                "NV17 must not leave an active escort to retry a town restock");
        assertTrue(JaianEscortSafety.shouldAttackJaianAggressor(true, true, true),
                "NV17 must attack a monster actively targeting Jaian when the guard is beside Jaian and in skill range");
        assertTrue(!JaianEscortSafety.shouldAttackJaianAggressor(false, true, true),
                "NV17 must ignore a nearby monster that is not targeting Jaian");
        assertTrue(!JaianEscortSafety.shouldAttackJaianAggressor(true, false, true),
                "NV17 must hold position instead of chasing an aggro monster outside skill range");
        assertTrue(!JaianEscortSafety.shouldAttackJaianAggressor(true, true, false),
                "NV17 must return beside Jaian before attacking");
        assertTrue(JaianEscortSafety.guardOffsetForDirection(1) == 18,
                "NV17 must stand one short step ahead of Jaian while moving right");
        assertTrue(JaianEscortSafety.guardOffsetForDirection(-1) == -18,
                "NV17 must stand one short step ahead of Jaian while moving left");
        assertEquals(1, JaianEscortSafety.initialEscortDirection(100, 110),
                "a guard behind Jaian must immediately move ahead to the right");
        assertEquals(-1, JaianEscortSafety.initialEscortDirection(120, 110),
                "a guard ahead of Jaian must preserve the leftward escort direction");
        assertTrue(JaianEscortSafety.shouldUseSingleTargetEscortAttack(true, true, true),
                "NV17 must use a single-target packet for an in-range Jaian aggressor");
        assertTrue(!JaianEscortSafety.shouldUseSingleTargetEscortAttack(true, true, false),
                "NV17 must regain the lead before attacking");
        assertTrue(JaianEscortSafety.isInsideDefenseRange(140, 90),
                "NV17 must proactively clear every monster at the edge of Jaian's defense range");
        assertTrue(!JaianEscortSafety.isInsideDefenseRange(141, 90),
                "NV17 must not chase monsters outside Jaian's horizontal defense range");
        assertTrue(!JaianEscortSafety.isInsideDefenseRange(140, 91),
                "NV17 must not chase monsters outside Jaian's vertical defense range");
        assertEquals(0, JaianEscortSafety.directionFromJaianDelta(5),
                "NV17 must wait for Jaian's real movement instead of guessing a direction");
        assertEquals(1, JaianEscortSafety.directionFromJaianDelta(6),
                "NV17 must lead to the right as soon as Jaian starts moving right");
        assertEquals(-1, JaianEscortSafety.directionFromJaianDelta(-6),
                "NV17 must lead to the left as soon as Jaian starts moving left");
        assertTrue(JaianEscortSafety.isInsideDefenseRange(180, 110, 79),
                "NV17 must expand the defense range when Jaian HP is in danger");
        assertTrue(!JaianEscortSafety.isInsideDefenseRange(180, 110, 80),
                "NV17 must keep the normal range while Jaian HP is safe");
        assertTrue(JaianEscortSafety.shouldApplyEscortFreeze(false, true),
                "a partially unlocked mob must be frozen once");
        assertTrue(!JaianEscortSafety.shouldApplyEscortFreeze(true, true),
                "an already frozen mob must not be written every render tick");
        assertTrue(!JaianEscortSafety.shouldRescanDefense(1119L, 1000L),
                "NV17 must reuse the latest defense scan within 120ms");
        assertTrue(JaianEscortSafety.shouldRescanDefense(1120L, 1000L),
                "NV17 must refresh defense targets after 120ms");
    }
}
