public class mScreen {
   public Command1 left;
   public Command1 center;
   public Command1 right;
   public static int fn;
   public static int fo;
   public static int fp;
   public static int fq;

   public static void aq() {
      fn = mFont.tahoma_8b.d() + 8;
      fo = 70 * mGraphics.b;
      fp = 22;
      fq = -1;
   }

   public void switchToMe() {
      GameCanvas.l();
      GameCanvas.m();
      if (GameCanvas.currentScreen != null) {
         GameCanvas.currentScreen.a_();
      }

      GameCanvas.currentScreen = this;
      MotherCanvas.instance.setFullScreenMode(true);
   }

   public void a_() {
   }

   public static void ar() {
      if (GameCanvas.isTouch) {
         fp = 26;
      } else {
         fp = 24;
      }

   }

   public void keyPress(int var1) {
   }

   public void update() {
   }

   public void updateKey() {
      if (GameCanvas.k[5] || a(GameCanvas.currentScreen.center)) {
         GameCanvas.k[5] = false;
         fq = -1;
         GameCanvas.o = false;
         if (this.center != null) {
            this.center.a();
         }
      }

      if (GameCanvas.k[12] || a(GameCanvas.currentScreen.left)) {
         GameCanvas.k[12] = false;
         fq = -1;
         GameCanvas.o = false;
         if (ChatTextField.gI().isShow) {
            if (ChatTextField.gI().left != null) {
               ChatTextField.gI().left.a();
            }
         } else if (this.left != null) {
            this.left.a();
         }
      }

      if (GameCanvas.k[13] || a(GameCanvas.currentScreen.right)) {
         GameCanvas.k[13] = false;
         fq = -1;
         GameCanvas.o = false;
         if (ChatTextField.gI().isShow) {
            if (ChatTextField.gI().right != null) {
               ChatTextField.gI().right.a();
               return;
            }
         } else if (this.right != null) {
            this.right.a();
         }
      }

   }

   public static boolean a(Command1 var0) {
      if (var0 == null) {
         return false;
      } else if (var0.x != 0 && var0.y != 0) {
         return var0.b();
      } else {
         if (GameCanvas.currentDialog != null) {
            if (GameCanvas.currentDialog.center != null && GameCanvas.b(GameCanvas.z - fo >> 1, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 1;
               if (var0 == GameCanvas.currentDialog.center && GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }

            if (GameCanvas.currentDialog.left != null && GameCanvas.b(0, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 0;
               if (var0 == GameCanvas.currentDialog.left && GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }

            if (GameCanvas.currentDialog.right != null && GameCanvas.b(GameCanvas.z - fo, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 2;
               if ((var0 == GameCanvas.currentDialog.right || var0 == ChatTextField.gI().right) && GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }
         } else {
            if (var0 == GameCanvas.currentScreen.left && GameCanvas.b(0, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 0;
               if (GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }

            if (var0 == GameCanvas.currentScreen.right && GameCanvas.b(GameCanvas.z - fo, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 2;
               if (GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }

            if ((var0 == GameCanvas.currentScreen.center || ChatPopup.b != null) && GameCanvas.b(GameCanvas.z - fo >> 1, GameCanvas.aa - fp - 5, fo, fp + 10)) {
               fq = 1;
               if (GameCanvas.n && GameCanvas.o) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void paint(mGraphics var1) {
      var1.translate(-var1.b(), -var1.c());
      var1.d(0, 0, GameCanvas.z, GameCanvas.aa + 1);
      Paint.a(var1);
      
         if (ChatPopup.b != null) {
            Paint.a(var1, (Command1) null, ChatPopup.b.a, (Command1) null);
         } else if (ChatTextField.gI().isShow) {
            Paint.a(var1, ChatTextField.gI().left, ChatTextField.gI().cmdChat, ChatTextField.gI().right);
         } else if (GameCanvas.currentDialog == null && !GameCanvas.menu.showMenu) {
            Paint.a(var1, this.left, this.center, this.right);
         }

   }

   public static void as() {
      fn = 0;
      fo = 0;
      fp = 0;
      fq = 0;
   }

   static {
      Main.main(8);
      aq();
   }
}
