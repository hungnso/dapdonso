public class ThreadUtil {
   public static void ThreadCatch(long var0) {
      try {
         Thread.sleep(var0);
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
      }

   }
}
