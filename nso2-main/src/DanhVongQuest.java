public final class DanhVongQuest {
   public static final int UNKNOWN = 0;
   public static final int NORMAL_MOB = 1;
   public static final int YEN_FROM_MOB = 2;
   public static final int ELITE_MOB = 3;
   public static final int UPGRADE_ITEM = 4;

   public String title;
   public String content;
   public String requiredItemName;
   public int type;
   public int current;
   public int max;
   public int upgradeTarget;

   public static DanhVongQuest parse(String title, String content) {
      if (content == null) {
         return null;
      }
      String lower = content.toLowerCase();
      int type = UNKNOWN;
      if (contains(lower, new String[]{"kiếm", "yên", "quái"}) || contains(lower, new String[]{"kiem", "yen", "quai"})) {
         type = YEN_FROM_MOB;
      } else if (lower.indexOf("tinh anh") >= 0) {
         type = ELITE_MOB;
      } else if (lower.indexOf("nâng cấp") >= 0 || lower.indexOf("nang cap") >= 0 || lower.indexOf("upgrade") >= 0) {
         type = UPGRADE_ITEM;
      } else if ((lower.indexOf("quái") >= 0 || lower.indexOf("quai") >= 0)
            && (lower.indexOf("tiêu diệt") >= 0 || lower.indexOf("tieu diet") >= 0
               || lower.indexOf("đánh") >= 0 || lower.indexOf("danh") >= 0)) {
         type = NORMAL_MOB;
      }
      if (type == UNKNOWN) {
         return null;
      }

      DanhVongQuest quest = new DanhVongQuest();
      quest.title = title == null ? "Nhiem vu danh vong" : title;
      quest.content = content;
      quest.type = type;
      quest.requiredItemName = findRequiredItem(content);
      int[] progress = findProgress(content);
      quest.current = progress[0];
      quest.max = progress[1];
      quest.upgradeTarget = type == UPGRADE_ITEM ? findUpgradeTarget(content) : 0;
      if (quest.max <= 0) {
         quest.current = 0;
         quest.max = type == YEN_FROM_MOB ? 1000000 : 1;
      }
      if (quest.upgradeTarget <= 0 && type == UPGRADE_ITEM) {
         quest.upgradeTarget = 1;
      }
      return quest;
   }

   private static boolean contains(String value, String[] words) {
      for (int i = 0; i < words.length; ++i) {
         if (value.indexOf(words[i]) < 0) {
            return false;
         }
      }
      return true;
   }

   private static String findRequiredItem(String content) {
      String[] prefixes = new String[]{"Sử dụng ", "Su dung ", "sử dụng ", "su dung "};
      int start = -1;
      int prefixLength = 0;
      for (int i = 0; i < prefixes.length; ++i) {
         start = content.indexOf(prefixes[i]);
         if (start >= 0) {
            prefixLength = prefixes[i].length();
            break;
         }
      }
      if (start < 0) {
         return null;
      }
      start += prefixLength;
      int end = content.indexOf('\n', start);
      if (end < 0) {
         end = content.length();
      }
      String result = content.substring(start, end).trim();
      while (result.length() > 0) {
         char last = result.charAt(result.length() - 1);
         if (last == '.' || last == ',' || last == ';' || last == '-' || last == '\r') {
            result = result.substring(0, result.length() - 1).trim();
         } else {
            break;
         }
      }
      return result.length() == 0 ? null : result;
   }

   private static int[] findProgress(String content) {
      int slash = content.indexOf('/');
      while (slash >= 0) {
         int left = slash - 1;
         while (left >= 0 && Character.isDigit(content.charAt(left))) {
            --left;
         }
         int right = slash + 1;
         while (right < content.length() && Character.isDigit(content.charAt(right))) {
            ++right;
         }
         if (left < slash - 1 && right > slash + 1) {
            try {
               int current = Integer.parseInt(content.substring(left + 1, slash));
               int max = Integer.parseInt(content.substring(slash + 1, right));
               return new int[]{current, max};
            } catch (Exception ignored) {
            }
         }
         slash = content.indexOf('/', slash + 1);
      }
      return new int[]{0, 0};
   }

   private static int findUpgradeTarget(String content) {
      int plus = content.indexOf('+');
      while (plus >= 0 && plus + 1 < content.length()) {
         int end = plus + 1;
         while (end < content.length() && Character.isDigit(content.charAt(end))) {
            ++end;
         }
         if (end > plus + 1) {
            try {
               return Integer.parseInt(content.substring(plus + 1, end));
            } catch (Exception ignored) {
            }
         }
         plus = content.indexOf('+', plus + 1);
      }
      return 1;
   }

   public final String typeName() {
      switch (this.type) {
      case NORMAL_MOB:
         return "quai thuong";
      case YEN_FROM_MOB:
         return "kiem Yen";
      case ELITE_MOB:
         return "Tinh Anh";
      case UPGRADE_ITEM:
         return "nang cap";
      default:
         return "khong ro";
      }
   }
}
