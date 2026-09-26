public final class VdmqZonePolicy {
    private VdmqZonePolicy() {
    }

    public static int chooseZone(int[] population, int[] configuredZones, int limit, int fallbackIndex) {
        if (population == null || population.length == 0) {
            return -1;
        }

        int[] candidates = configuredZones;
        if (candidates == null || candidates.length == 0) {
            candidates = new int[population.length];
            for (int i = 0; i < population.length; ++i) {
                candidates[i] = i;
            }
        }

        int bestZone = -1;
        int bestPopulation = Integer.MAX_VALUE;
        int validCount = 0;
        for (int i = 0; i < candidates.length; ++i) {
            int zone = candidates[i];
            if (zone < 0 || zone >= population.length) {
                continue;
            }
            ++validCount;
            if (population[zone] < limit && population[zone] < bestPopulation) {
                bestZone = zone;
                bestPopulation = population[zone];
            }
        }

        if (bestZone >= 0) {
            return bestZone;
        }
        if (validCount == 0) {
            return -1;
        }

        int selected = Math.abs(fallbackIndex) % validCount;
        for (int i = 0; i < candidates.length; ++i) {
            int zone = candidates[i];
            if (zone < 0 || zone >= population.length) {
                continue;
            }
            if (selected-- == 0) {
                return zone;
            }
        }
        return -1;
    }
}
