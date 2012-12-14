package hymas.m1.collecter;

/**
 *
 * @author Chirila Alexandru
 */
public enum Label {

    walk, run, bike, car, buss, tram, train, rollerskate, skateboard, ski, flying;

    public static String[] getNames() {
        Label[] values = values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            Label label = values[i];
            result[i] = label.name();
        }
        return result;
    }
}
