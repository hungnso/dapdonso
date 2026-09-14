import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

public final class AutoDanhVongPanel extends Form implements CommandListener {
   private static final String RMS_OPTIONS = "AutoDanhVongOptions";
   public static boolean autoBuyEquipment;
   public static boolean doNormalMob;
   public static boolean doYen;
   public static boolean doElite;
   public static boolean doUpgrade;
   public static boolean useDanhVongPhu;
   private final ChoiceGroup options;
   private final Command saveCommand;

   public AutoDanhVongPanel() {
      super("Cai dat Auto Danh Vong");
      this.options = new ChoiceGroup("Tu dong", 2, new String[]{
            "Tu mua + deo trang bi yeu cau",
            "Lam NV danh quai thuong",
            "Lam NV kiem Yen tu quai",
            "Lam NV giet Tinh Anh",
            "Lam NV nang cap trang bi",
            "Tu dung Danh vong phu khi het luot"
      }, (Image[])null);
      this.options.setSelectedIndex(0, autoBuyEquipment);
      this.options.setSelectedIndex(1, doNormalMob);
      this.options.setSelectedIndex(2, doYen);
      this.options.setSelectedIndex(3, doElite);
      this.options.setSelectedIndex(4, doUpgrade);
      this.options.setSelectedIndex(5, useDanhVongPhu);
      this.append(this.options);
      this.append("Auto chi xu ly 4 nhom: quai thuong, kiem Yen, Tinh Anh, nang cap. NV Loi dai/Chien truong/PK se tam dung de tranh lam sai.");
      this.append("Danh vong phu mac dinh TAT de tranh tu tieu vat pham.");
      this.addCommand(this.saveCommand = new Command("Luu", 4, 0));
      this.addCommand(new Command("Thoat", 7, 1));
      this.setCommandListener(this);
   }

   public static void show() {
      Display.getDisplay(GameMidlet.instance).setCurrent(new AutoDanhVongPanel());
   }

   public final void commandAction(Command command, Displayable displayable) {
      if (command == this.saveCommand) {
         autoBuyEquipment = this.options.isSelected(0);
         doNormalMob = this.options.isSelected(1);
         doYen = this.options.isSelected(2);
         doElite = this.options.isSelected(3);
         doUpgrade = this.options.isSelected(4);
         useDanhVongPhu = this.options.isSelected(5);
         save();
         GameScr.addChatPopup("Da luu Auto Danh Vong");
      }
      Display.getDisplay(GameMidlet.instance).setCurrent(MotherCanvas.gI());
   }

   private static void save() {
      int flags = (autoBuyEquipment ? 1 : 0)
            | (doNormalMob ? 2 : 0)
            | (doYen ? 4 : 0)
            | (doElite ? 8 : 0)
            | (doUpgrade ? 16 : 0)
            | (useDanhVongPhu ? 32 : 0);
      mResources.a(RMS_OPTIONS, flags);
   }

   static {
      int value = mResources.d(RMS_OPTIONS);
      if (value < 0) {
         autoBuyEquipment = true;
         doNormalMob = true;
         doYen = true;
         doElite = true;
         doUpgrade = true;
         useDanhVongPhu = false;
      } else {
         autoBuyEquipment = (value & 1) != 0;
         doNormalMob = (value & 2) != 0;
         doYen = (value & 4) != 0;
         doElite = (value & 8) != 0;
         doUpgrade = (value & 16) != 0;
         useDanhVongPhu = (value & 32) != 0;
      }
   }
}
