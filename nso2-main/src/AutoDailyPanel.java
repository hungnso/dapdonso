import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

public final class AutoDailyPanel extends Form implements CommandListener {
    private static final String RMS_OPTIONS = "AutoDailyOptions";
    private static final String RMS_TIME = "AutoDailyTime";
    private static final int OPTION_WEAPON_ONLY = 16;
    private static final long WEAPON_PICKUP_TOGGLE_DEBOUNCE_MS = 350L;
    private static long lastWeaponPickupToggleAt;

    public static boolean runDailyQuest;
    public static boolean runTaThu;
    public static boolean resumeAutoNvc;
    public static boolean scheduleEnabled;
    public static boolean weaponOnlyPickup;
    public static int scheduleHour;
    public static int scheduleMinute;

    private final ChoiceGroup options;
    private final TextField timeField;
    private final Command saveCommand;

    public AutoDailyPanel() {
        super("Cai dat Auto Hang Ngay");
        this.options = new ChoiceGroup("Luon chay theo thu tu", 2, new String[]{
                "Nhiem vu hang ngay",
                "Ta Thu",
                "Xong thi quay lai Auto 1-70 neu Lv < 70",
                "Bat lich tu dong moi ngay"
        }, (Image[]) null);
        this.options.setSelectedIndex(0, runDailyQuest);
        this.options.setSelectedIndex(1, runTaThu);
        this.options.setSelectedIndex(2, resumeAutoNvc);
        this.options.setSelectedIndex(3, scheduleEnabled);
        this.append((Item) this.options);

        this.timeField = new TextField("Gio chay HH:mm", formatTime(), 5, 0);
        this.append((Item) this.timeField);
        this.append("Thu tu: NV hang ngay -> Ta Thu. Neu dang chay VDMQ hoac Auto 1-70, he thong se tam dung va quay lai dung luong auto do sau khi xong. Game phai dang mo de lich tu dong hoat dong.");
        this.append("Tuy chon Auto 1-70 duoc dung khi bat dau Auto Hang Ngay ma truoc do khong co auto nao dang chay.");

        this.saveCommand = new Command("Luu", 4, 0);
        this.addCommand(this.saveCommand);
        this.addCommand(new Command("Thoat", 7, 1));
        this.setCommandListener(this);
    }

    public static void show() {
        Display.getDisplay((MIDlet) GameMidlet.instance).setCurrent((Displayable) new AutoDailyPanel());
    }

    public final void commandAction(Command command, Displayable displayable) {
        if (command == this.saveCommand) {
            runDailyQuest = this.options.isSelected(0);
            runTaThu = this.options.isSelected(1);
            resumeAutoNvc = this.options.isSelected(2);
            scheduleEnabled = this.options.isSelected(3);
            parseAndSetTime(this.timeField.getString());
            save();
            AutoDailyCoordinator.reloadSchedule();
            GameScr.addChatPopup("Da luu Auto Hang Ngay: " + formatTime());
        }
        Display.getDisplay((MIDlet) GameMidlet.instance).setCurrent((Displayable) MotherCanvas.gI());
    }

    public static String formatTime() {
        String hour = scheduleHour < 10 ? "0" + scheduleHour : String.valueOf(scheduleHour);
        String minute = scheduleMinute < 10 ? "0" + scheduleMinute : String.valueOf(scheduleMinute);
        return hour + ":" + minute;
    }

    private static void parseAndSetTime(String value) {
        int hour = 7;
        int minute = 0;
        try {
            int separator = value == null ? -1 : value.indexOf(58);
            if (separator > 0) {
                hour = Integer.parseInt(value.substring(0, separator).trim());
                minute = Integer.parseInt(value.substring(separator + 1).trim());
            }
        } catch (Exception ignored) {
            hour = 7;
            minute = 0;
        }
        if (hour < 0 || hour > 23) {
            hour = 7;
        }
        if (minute < 0 || minute > 59) {
            minute = 0;
        }
        scheduleHour = hour;
        scheduleMinute = minute;
    }

    private static void save() {
        int flags = (runDailyQuest ? 1 : 0)
                | (runTaThu ? 2 : 0)
                | (resumeAutoNvc ? 4 : 0)
                | (scheduleEnabled ? 8 : 0)
                | (weaponOnlyPickup ? OPTION_WEAPON_ONLY : 0);
        mResources.a(RMS_OPTIONS, flags);
        mResources.a(RMS_TIME, formatTime());
    }

    public static void toggleWeaponOnlyPickup() {
        long now = System.currentTimeMillis();
        // Tren desktop/QLTK, mot click co the di qua ca nhanh touch va nhanh
        // select-key cua GameScr. Neu toggle ca hai lan thi checkbox vua bat se
        // bi tat ngay. Chan event trung trong mot khoang rat ngan.
        if (now - lastWeaponPickupToggleAt < WEAPON_PICKUP_TOGGLE_DEBOUNCE_MS) {
            return;
        }
        lastWeaponPickupToggleAt = now;
        setWeaponOnlyPickup(!weaponOnlyPickup, true);
    }

    public static void disableWeaponOnlyPickupForEquipmentMode() {
        if (!weaponOnlyPickup) {
            return;
        }
        // Chuyen ve checkbox Nhat Trang Bi goc. Khong debounce o day de event
        // goc co the bat Char.doa ngay trong cung mot click.
        setWeaponOnlyPickup(false, false);
    }

    private static void setWeaponOnlyPickup(boolean enabled, boolean notify) {
        weaponOnlyPickup = enabled;
        if (enabled) {
            Char.doa = false;
            // "Khong nhat gi ca" xung dot truc tiep voi che do chi nhat vu khi.
            Char.dt = false;
        }
        save();
        if (notify) {
            GameScr.addChatPopup(weaponOnlyPickup ? "Chi nhat vu khi: Bat" : "Chi nhat vu khi: Tat");
        }
    }

    static {
        int flags = mResources.d(RMS_OPTIONS);
        if (flags < 0) {
            runDailyQuest = true;
            runTaThu = true;
            resumeAutoNvc = true;
            scheduleEnabled = false;
            weaponOnlyPickup = false;
        } else {
            runDailyQuest = (flags & 1) != 0;
            runTaThu = (flags & 2) != 0;
            resumeAutoNvc = (flags & 4) != 0;
            scheduleEnabled = (flags & 8) != 0;
            weaponOnlyPickup = (flags & OPTION_WEAPON_ONLY) != 0;
        }
        scheduleHour = 7;
        scheduleMinute = 0;
        String savedTime = mResources.c(RMS_TIME);
        if (savedTime != null) {
            parseAndSetTime(savedTime);
        }
    }
}
