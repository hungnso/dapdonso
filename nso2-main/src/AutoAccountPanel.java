import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/** In-game editor for the 20 accounts owned by the current emulator tab. */
public final class AutoAccountPanel extends Form implements CommandListener {
    private static AutoAccountPanel instance;
    private final TextField[] usernames = new TextField[AutoAccountStore.MAX_ACCOUNTS];
    private final TextField[] passwords = new TextField[AutoAccountStore.MAX_ACCOUNTS];
    private final ChoiceGroup enabled;
    private final TextField startIndex;
    private final Command save = new Command("Lưu", Command.OK, 0);
    private final Command cancel = new Command("Thoát", Command.BACK, 1);
    private final Command reset = new Command("Reset tiến trình", Command.ITEM, 2);

    private AutoAccountPanel() {
        super("Auto tài khoản");
        AutoAccountEntry[] entries = AutoAccountStore.load();
        this.enabled = new ChoiceGroup("Tự chuyển tài khoản", ChoiceGroup.MULTIPLE,
                new String[]{"Bật"}, null);
        this.enabled.setSelectedIndex(0, AutoAccountStore.isEnabled());
        this.append(this.enabled);
        this.startIndex = new TextField("Bắt đầu từ acc (1-20)",
                String.valueOf(AutoAccountStore.startIndex() + 1), 2, TextField.NUMERIC);
        this.append(this.startIndex);
        for (int i = 0; i < AutoAccountStore.MAX_ACCOUNTS; ++i) {
            AutoAccountEntry entry = entries[i];
            this.usernames[i] = new TextField("Acc " + (i + 1), entry.username, 40, TextField.ANY);
            this.passwords[i] = new TextField("MK " + (i + 1), entry.password, 40, TextField.PASSWORD);
            this.append(this.usernames[i]);
            this.append(this.passwords[i]);
        }
        this.addCommand(this.save);
        this.addCommand(this.cancel);
        this.addCommand(this.reset);
        this.setCommandListener(this);
    }

    public static void show() {
        instance = new AutoAccountPanel();
        Display.getDisplay(GameMidlet.instance).setCurrent(instance);
    }

    public static boolean isRotationEnabled() {
        return AutoAccountStore.isEnabled();
    }

    public static int startIndex() {
        return AutoAccountStore.startIndex();
    }

    public final void commandAction(Command command, Displayable displayable) {
        if (command == this.reset) {
            AutoAccountStore.resetStatuses();
            GameScr.addChatPopup("Đã reset tiến trình Auto tài khoản");
            return;
        }
        if (command == this.save) {
            saveValues();
        }
        Display.getDisplay(GameMidlet.instance).setCurrent(MotherCanvas.instance);
        MotherCanvas.instance.setFullScreenMode(true);
        instance = null;
    }

    private void saveValues() {
        AutoAccountEntry[] entries = new AutoAccountEntry[AutoAccountStore.MAX_ACCOUNTS];
        for (int i = 0; i < entries.length; ++i) {
            String username = usernames[i].getString().trim();
            String password = passwords[i].getString();
            entries[i] = new AutoAccountEntry(username, password, username.length() > 0,
                    AutoAccountEntry.PENDING);
        }
        int start = 1;
        try {
            start = Integer.parseInt(startIndex.getString());
        } catch (Exception ignored) {
        }
        if (start < 1) start = 1;
        if (start > AutoAccountStore.MAX_ACCOUNTS) start = AutoAccountStore.MAX_ACCOUNTS;
        AutoAccountStore.save(entries);
        AutoAccountStore.setEnabled(enabled.isSelected(0));
        AutoAccountStore.setStartIndex(start - 1);
        AutoAccountStore.clearCheckpoint();
    }
}
