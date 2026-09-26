public final class VdmqZonePolicyTest {
    public static void main(String[] args) {
        choosesLeastPopulatedConfiguredZoneBelowLimit();
        choosesConfiguredFallbackWhenAllConfiguredZonesAreFull();
        ignoresInvalidConfiguredZones();
    }

    private static void choosesLeastPopulatedConfiguredZoneBelowLimit() {
        int zone = VdmqZonePolicy.chooseZone(new int[]{12, 8, 3, 6}, new int[]{0, 2, 3}, 10, 0);
        assertEquals("chooses least populated configured zone below limit", 2, zone);
    }

    private static void choosesConfiguredFallbackWhenAllConfiguredZonesAreFull() {
        int zone = VdmqZonePolicy.chooseZone(new int[]{13, 12, 11}, new int[]{0, 2}, 10, 1);
        assertEquals("uses deterministic random fallback among configured zones", 2, zone);
    }

    private static void ignoresInvalidConfiguredZones() {
        int zone = VdmqZonePolicy.chooseZone(new int[]{15, 4}, new int[]{-1, 9, 1}, 10, 0);
        assertEquals("ignores configured zone outside server list", 1, zone);
    }

    private static void assertEquals(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}
