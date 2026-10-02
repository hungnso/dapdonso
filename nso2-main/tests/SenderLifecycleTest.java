import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public final class SenderLifecycleTest {
    public static void main(String[] args) throws Exception {
        waitsUntilKeyThenSendsInOrder();
        clearDropsPendingPackets();
        reconnectDoesNotReuseOldWorker();
        System.out.println("SenderLifecycleTest PASS");
    }

    private static void waitsUntilKeyThenSendsInOrder() throws Exception {
        Session_ME session = new Session_ME();
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        session.outputStream = new DataOutputStream(wire);
        session.connected = true;
        Thread worker = start(session);
        try {
            awaitWaiting(worker);
            session.sendMessage(new Message((byte) 11));
            session.sendMessage(new Message((byte) 22));
            Thread.sleep(30);
            check(wire.size() == 0, "payload must wait for server key");
            session.key = new byte[]{0};
            Session_ME.a(session, true);
            awaitSize(wire, 6);
            byte[] data = wire.toByteArray();
            check(data[0] == 11 && data[3] == 22, "FIFO command order");
            awaitWaiting(worker);
            session.sendMessage(new Message((byte) 33));
            awaitSize(wire, 9);
            check(wire.toByteArray()[6] == 33, "enqueue wakes a waiting worker");
        } finally {
            session.cleanNetwork();
            worker.join(2000);
            check(!worker.isAlive(), "disconnect must stop worker");
        }
    }

    private static void clearDropsPendingPackets() throws Exception {
        Session_ME session = new Session_ME();
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        session.outputStream = new DataOutputStream(wire);
        session.connected = true;
        Thread worker = start(session);
        try {
            session.sendMessage(new Message((byte) 44));
            awaitWaiting(worker);
            Session_ME.sender(session).a();
            session.key = new byte[]{0};
            Session_ME.a(session, true);
            session.sendMessage(new Message((byte) 55));
            awaitSize(wire, 3);
            check(wire.toByteArray()[0] == 55, "cleared packet must not be sent");
            worker.interrupt();
            worker.join(2000);
            check(!worker.isAlive(), "interrupt alone stops sender");
        } finally {
            session.cleanNetwork();
            worker.join(2000);
        }
    }

    private static void reconnectDoesNotReuseOldWorker() throws Exception {
        Session_ME session = new Session_ME();
        for (int i = 0; i < 50; i++) {
            session.connected = true;
            session.outputStream = new DataOutputStream(new ByteArrayOutputStream());
            Thread old = start(session);
            session.sendMessage(new Message((byte) 66));
            session.cleanNetwork();
            ByteArrayOutputStream newWire = new ByteArrayOutputStream();
            session.outputStream = new DataOutputStream(newWire);
            session.connected = true;
            session.key = new byte[]{0};
            Thread next = start(session);
            try {
                Session_ME.a(session, true);
                session.sendMessage(new Message((byte) 77));
                awaitSize(newWire, 3);
                check(newWire.size() == 3 && newWire.toByteArray()[0] == 77,
                        "old connection queue must not cross reconnect");
                old.join(2000);
                check(!old.isAlive(), "old worker survives reconnect " + i);
            } finally {
                session.cleanNetwork();
                next.join(2000);
                check(!next.isAlive(), "new worker stops cleanly");
            }
        }
    }

    private static Thread start(Session_ME session) {
        Thread worker = new Thread(Session_ME.sender(session), "sender-test");
        worker.setDaemon(true);
        Session_ME.b(session, worker);
        worker.start();
        return worker;
    }

    private static void awaitWaiting(Thread worker) throws Exception {
        long deadline = System.currentTimeMillis() + 2000;
        while (worker.getState() != Thread.State.WAITING && System.currentTimeMillis() < deadline) {
            Thread.sleep(1);
        }
        check(worker.getState() == Thread.State.WAITING, "idle sender must WAIT instead of polling");
    }

    private static void awaitSize(ByteArrayOutputStream wire, int bytes) throws Exception {
        long deadline = System.currentTimeMillis() + 2000;
        while (wire.size() < bytes && System.currentTimeMillis() < deadline) Thread.sleep(1);
        check(wire.size() == bytes, "expected " + bytes + " wire bytes, got " + wire.size());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
