/** Caption-based Rikudou menu navigation used by Ta Thu receive/complete actions. */
public final class TaThuMenuNavigator {
   private static final String[] ROOT_KEYWORDS = new String[]{
         "tà thú", "ta thu", "bắt tà", "bat ta", "truy bắt", "truy bat"
   };
   private static boolean rootSelected;
   private static int mode = -1;
   private static long lastActionAt;

   private TaThuMenuNavigator() {
   }

   /**
    * @return true only after the requested submenu command was actually fired.
    */
   public static boolean tick(int option) {
      long now = System.currentTimeMillis();
      if (mode != option) {
         reset(option, now);
      }
      if (Char.getMyChar() == null || GameScr.i(25) == null) {
         return false;
      }

      if (!rootSelected) {
         // Recover cleanly if the Ta Thu submenu is already visible.
         if (GameCanvas.menu.selectDailyTaskForAuto(option)) {
            completed();
            return true;
         }
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(ROOT_KEYWORDS, "ATT root")) {
            rootSelected = true;
            lastActionAt = now;
            return false;
         }
         if (GameCanvas.menu.showMenu) {
            // Never use an index fallback here: index 2 is Chien truong.
            if (now - lastActionAt >= 1800L) {
               GameCanvas.menu.showMenu = false;
               lastActionAt = now;
            }
            return false;
         }
         if (now - lastActionAt >= 900L) {
            GameScr.h(25);
            lastActionAt = now;
            System.out.println("ATT menu=open-root option=" + option);
         }
         return false;
      }

      if (GameCanvas.menu.selectDailyTaskForAuto(option)) {
         completed();
         return true;
      }
      if (GameCanvas.menu.showMenu) {
         if (now - lastActionAt >= 2500L) {
            GameCanvas.menu.showMenu = false;
            reset(option, now);
         }
         return false;
      }
      if (now - lastActionAt >= 2500L) {
         reset(option, 0L);
      }
      return false;
   }

   private static void completed() {
      rootSelected = false;
      mode = -1;
      lastActionAt = System.currentTimeMillis();
   }

   private static void reset(int newMode, long now) {
      rootSelected = false;
      mode = newMode;
      lastActionAt = now;
   }
}
