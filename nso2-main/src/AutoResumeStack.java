/** Small bounded stack operations shared by manual starts and Daily children. */
public final class AutoResumeStack {
    private AutoResumeStack() { }

    public static boolean contains(Auto current, Auto next) {
        for (int depth = 0; current != null && depth < 32; depth++) {
            if (current == next) return true;
            current = current.l;
        }
        return false;
    }

    public static Auto push(Auto current, Auto next) {
        if (next == null || contains(current, next)) return current;
        next.l = current;
        return next;
    }

    public static Auto pop(Auto current) {
        if (current == null) return null;
        Auto previous = current.l;
        current.l = null;
        return previous == current ? null : previous;
    }

    public static void clear(Auto current) {
        for (int depth = 0; current != null && depth < 32; depth++) {
            Auto previous = current.l;
            current.l = null;
            current = previous;
        }
    }
}
