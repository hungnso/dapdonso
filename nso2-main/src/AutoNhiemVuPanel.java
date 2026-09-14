import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

public final class AutoNhiemVuPanel extends Form implements CommandListener {
   private static final String RMS_CLASS = "AutoNVCClass";
   private static final String RMS_OPTIONS = "AutoNVCOptions";
   private static final String RMS_FOOD_COUNT = "AutoNVCFoodCount";
   private static final String RMS_OPTIMAL_MAP = "AutoNVCOptimalMap";
   private static final String RMS_PICK_QUEST = "AutoNVCPickQuest";
   private static final String RMS_PICK_NORMAL = "AutoNVCPickNormal";
   private static final String RMS_AUTO_SELL = "AutoNVCAutoSell";
   private static final String RMS_AUTO_UPGRADE = "AutoNVCAutoUpgrade";
   private static final String RMS_UPGRADE_TARGET = "AutoNVCUpgradeTarget";
   public static int selectedClass = loadClass();
   public static boolean autoPotential = loadOption(1);
   public static boolean autoSkill = loadOption(2);
   public static boolean autoFood = loadOption(4);
   public static boolean autoOptimalMap = loadOptimalMap();
   public static boolean autoPickQuest = loadBoolean(RMS_PICK_QUEST);
   public static boolean autoPickNormal = loadBoolean(RMS_PICK_NORMAL);
   public static boolean autoSellUnsuitable = loadBoolean(RMS_AUTO_SELL);
   public static boolean autoUpgradeWeapon = loadBoolean(RMS_AUTO_UPGRADE);
   public static int upgradeTarget = loadUpgradeTarget();
   public static int foodCount = loadFoodCount();
   private final ChoiceGroup classChoice;
   private final ChoiceGroup options;
   private final TextField foodCountField;
   private final TextField upgradeTargetField;
   private final Command saveCommand;

   public AutoNhiemVuPanel() {
      super("Cai dat Auto NV Lv1-50");
      this.classChoice = new ChoiceGroup("Chon phai", 1, new String[]{
            "Kiem - Hirosaki", "Tieu - Hirosaki", "Kunai - Haruna",
            "Cung - Haruna", "Dao - Ookaza", "Quat - Ookaza"
      }, (Image[])null);
      this.classChoice.setSelectedIndex(selectedClass - 1, true);
      this.append(this.classChoice);

      this.options = new ChoiceGroup("Tu dong", 2, new String[]{
            "Cong tiem nang", "Cong ky nang", "Mua va dung thuc an theo level", "Tim map/khu danh quai toi uu",
            "Nhat vat pham nhiem vu", "Nhat vat pham thuong",
            "Ban trang bi khong phu hop", "Tu dap da nang vu khi"
      }, (Image[])null);
      this.options.setSelectedIndex(0, autoPotential);
      this.options.setSelectedIndex(1, autoSkill);
      this.options.setSelectedIndex(2, autoFood);
      this.options.setSelectedIndex(3, autoOptimalMap);
      this.options.setSelectedIndex(4, autoPickQuest);
      this.options.setSelectedIndex(5, autoPickNormal);
      this.options.setSelectedIndex(6, autoSellUnsuitable);
      this.options.setSelectedIndex(7, autoUpgradeWeapon);
      this.append(this.options);
      this.foodCountField = new TextField("So thuc an mua khi het", String.valueOf(foodCount), 2, 2);
      this.append(this.foodCountField);
      this.upgradeTargetField = new TextField("Nang vu khi toi da +", String.valueOf(upgradeTarget), 2, 2);
      this.append(this.upgradeTargetField);
      this.append("Tiem nang: Kiem/Kunai/Dao uu tien suc manh; Tieu/Cung/Quat uu tien than phap; moi 100 diem chia them 40 sinh luc.");
      this.addCommand(this.saveCommand = new Command("Luu", 4, 0));
      this.addCommand(new Command("Thoat", 7, 1));
      this.setCommandListener(this);
   }

   public static void show() {
      Display.getDisplay(GameMidlet.instance).setCurrent(new AutoNhiemVuPanel());
   }

   public final void commandAction(Command command, Displayable displayable) {
      if (command == this.saveCommand) {
         selectedClass = this.classChoice.getSelectedIndex() + 1;
         autoPotential = this.options.isSelected(0);
         autoSkill = this.options.isSelected(1);
         autoFood = this.options.isSelected(2);
         autoOptimalMap = this.options.isSelected(3);
         autoPickQuest = this.options.isSelected(4);
         autoPickNormal = this.options.isSelected(5);
         autoSellUnsuitable = this.options.isSelected(6);
         autoUpgradeWeapon = this.options.isSelected(7);

         try {
            foodCount = Integer.parseInt(this.foodCountField.getString());
         } catch (Exception error) {
            foodCount = 2;
         }

         if (foodCount < 1) {
            foodCount = 1;
         } else if (foodCount > 20) {
            foodCount = 20;
         }

         try {
            upgradeTarget = Integer.parseInt(this.upgradeTargetField.getString());
         } catch (Exception error) {
            upgradeTarget = 4;
         }
         if (upgradeTarget < 1) {
            upgradeTarget = 1;
         } else if (upgradeTarget > 16) {
            upgradeTarget = 16;
         }

         int flags = (autoPotential ? 1 : 0) | (autoSkill ? 2 : 0) | (autoFood ? 4 : 0)
               | (autoOptimalMap ? 8 : 0);
         mResources.a(RMS_CLASS, selectedClass);
         mResources.a(RMS_OPTIONS, flags);
         mResources.a(RMS_FOOD_COUNT, foodCount);
         mResources.a(RMS_OPTIMAL_MAP, autoOptimalMap ? 1 : 0);
         mResources.a(RMS_PICK_QUEST, autoPickQuest ? 1 : 0);
         mResources.a(RMS_PICK_NORMAL, autoPickNormal ? 1 : 0);
         mResources.a(RMS_AUTO_SELL, autoSellUnsuitable ? 1 : 0);
         mResources.a(RMS_AUTO_UPGRADE, autoUpgradeWeapon ? 1 : 0);
         mResources.a(RMS_UPGRADE_TARGET, upgradeTarget);
         GameScr.addChatPopup("Da luu Auto NV: " + getSelectedClassName());
      }

      Display.getDisplay(GameMidlet.instance).setCurrent(MotherCanvas.gI());
   }

   public static String getSelectedClassName() {
      if (GameScr.nClasss != null && selectedClass >= 0 && selectedClass < GameScr.nClasss.length
            && GameScr.nClasss[selectedClass] != null && GameScr.nClasss[selectedClass].name != null) {
         return GameScr.nClasss[selectedClass].name;
      }

      String[] names = new String[]{"", "Kiem", "Tieu", "Kunai", "Cung", "Dao", "Quat"};
      return names[selectedClass];
   }

   private static int loadClass() {
      int value = mResources.d(RMS_CLASS);
      return value >= 1 && value <= 6 ? value : 1;
   }

   private static boolean loadOption(int flag) {
      int value = mResources.d(RMS_OPTIONS);
      return value < 0 || (value & flag) != 0;
   }

   private static int loadFoodCount() {
      int value = mResources.d(RMS_FOOD_COUNT);
      return value >= 1 && value <= 20 ? value : 2;
   }

   private static boolean loadOptimalMap() {
      int value = mResources.d(RMS_OPTIMAL_MAP);
      return value < 0 || value == 1;
   }

   private static boolean loadBoolean(String key) {
      int value = mResources.d(key);
      return value < 0 || value == 1;
   }

   private static int loadUpgradeTarget() {
      int value = mResources.d(RMS_UPGRADE_TARGET);
      return value >= 1 && value <= 16 ? value : 4;
   }
}
