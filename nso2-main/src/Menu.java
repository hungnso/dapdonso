import javax.microedition.lcdui.Image;

public final class Menu implements IActionListener {
   public boolean showMenu;
   private MyVector menuItems;
   public int menuSelectedItem;
   private int menuX;
   private int menuY;
   private int menuW;
   private int menuH;
   private int i;
   private static int cmtoX;
   private static int cmx;
   private static int cmdy;
   private static int cmvy;
   private Command1 left;
   private Command1 right;
   private Command1 center;
   private static Image btnlBig0;
   private static Image btnlBig1;
   boolean c;
   private int s;
   private int t;
   private int u;
   private int[] menuTemY;
   private boolean w;
   private boolean x;
   private int y;
   private int z;
   private int aa;
   private int ab;

   public static void init() {
      btnlBig0 = GameCanvas.loadImage("/hd/btnlBig0.png");
      btnlBig1 = GameCanvas.loadImage("/hd/btnlBig1.png");
   }

   public Menu() {
      this.left = new Command1(mResources.dz, 0);
      this.right = GameCanvas.isTouch ? null : new Command1(mResources.aq, GameCanvas.z - 71, GameCanvas.aa - mScreen.fp + 1);
      this.center = null;
      this.menuTemY = new int[3];
   }

   public final void startAt(MyVector var1) {
      this.c = false;
      ChatPopup.b = null;
      InfoDlg.hide();
      if (var1.size() != 0) {
         this.menuItems = var1;
         this.menuW = 60;
         this.menuH = 60;

         for(int var2 = 0; var2 < var1.size(); ++var2) {
            Command1 var3 = (Command1)var1.elementAt(var2);
            if (mFont.tahoma_7_yellow.a(var3.caption) > this.menuW - 8) {
               var3.subCaption = mFont.tahoma_7_yellow.splitFontArray(var3.caption, this.menuW - 8);
            }
         }

         this.menuX = (GameCanvas.z - var1.size() * this.menuW) / 2;
         if (this.menuX <= 0) {
            this.menuX = 1;
         }

         this.menuY = GameCanvas.aa - this.menuH - (Paint.hTab + 1);
         if (GameCanvas.isTouch) {
            this.menuY -= 3;
         }

         this.i = this.menuY;
         this.showMenu = true;
         this.menuSelectedItem = 0;
         if ((cmdy = this.menuItems.size() * this.menuW - GameCanvas.z) < 0) {
            cmdy = 0;
         }

         cmtoX = 0;
         cmx = 0;
         cmvy = 50;
         this.s = var1.size() * this.menuW - 1;
         if (this.s > GameCanvas.z - 2) {
            this.s = GameCanvas.z - 2;
         }

         if (GameCanvas.isTouch) {
            this.menuSelectedItem = -1;
         }
      }

   }

   public final void updateMenuKey() {
      if (this.showMenu) {
         boolean var1 = false;
         if (!GameCanvas.k[2] && !GameCanvas.k[4]) {
            if (!GameCanvas.k[8] && !GameCanvas.k[6]) {
               if (GameCanvas.k[5]) {
                  if (this.center != null) {
                     if (this.center.idAction > 0) {
                        if (this.center.c == GameScr.gI()) {
                           GameScr.gI().b(this.center.idAction, this.center.p);
                        } else {
                           this.perform(this.center.idAction, this.center.p);
                        }
                     }
                  } else {
                     this.y = 2;
                  }
               } else if (GameCanvas.k[12]) {
                  if (this.left.idAction > 0) {
                     this.perform(this.left.idAction, this.left.p);
                  } else {
                     this.y = 2;
                  }
               } else if (!this.c && (GameCanvas.k[13] || mScreen.a(this.right))) {
                  this.showMenu = false;
                  InfoDlg.hide();
               }
            } else {
               var1 = true;
               ++this.menuSelectedItem;
               if (this.menuSelectedItem > this.menuItems.size() - 1) {
                  this.menuSelectedItem = 0;
               }
            }
         } else {
            var1 = true;
            --this.menuSelectedItem;
            if (this.menuSelectedItem < 0) {
               this.menuSelectedItem = this.menuItems.size() - 1;
            }
         }

         this.center = null;
         if (GameScr.cc && !GameCanvas.isTouch && this.menuSelectedItem != -1) {
            Command1 var2 = (Command1)this.menuItems.elementAt(this.menuSelectedItem);
            ChatTab var3;
            if ((var3 = ChatManager.gI().findTab(var2.caption)) != null && var3.type == 2) {
               this.center = new Command1(mResources.ar, this, 1000, var3);
            }
         }

         if (var1) {
            if ((cmtoX = this.menuSelectedItem * this.menuW + this.menuW - GameCanvas.z / 2) > cmdy) {
               cmtoX = cmdy;
            }

            if (cmtoX < 0) {
               cmtoX = 0;
            }

            if (this.menuSelectedItem == this.menuItems.size() - 1 || this.menuSelectedItem == 0) {
               cmx = cmtoX;
            }
         }

         if (!this.c && GameCanvas.o && !GameCanvas.c(this.menuX, this.menuY, this.s, this.menuH) && !this.w) {
            this.t = this.u = 0;
            this.w = false;
            this.showMenu = false;
            GameCanvas.o = false;
            return;
         }

         int var5;
         int var6;
         if (GameCanvas.m) {
            if (!this.w && GameCanvas.c(this.menuX, this.menuY, this.s, this.menuH)) {
               for(var5 = 0; var5 < this.menuTemY.length; ++var5) {
                  this.menuTemY[0] = GameCanvas.p;
               }

               this.u = GameCanvas.p;
               this.w = true;
               this.x = this.z != 0;
               this.z = 0;
            } else if (this.w) {
               ++this.t;
               if (this.t > 5 && this.u == GameCanvas.p && !this.x) {
                  this.u = -1000;
                  this.menuSelectedItem = (cmtoX + GameCanvas.p - this.menuX) / this.menuW;
               }

               if ((var5 = GameCanvas.p - this.menuTemY[0]) != 0 && this.menuSelectedItem != -1) {
                  this.menuSelectedItem = -1;
               }

               for(var6 = this.menuTemY.length - 1; var6 > 0; --var6) {
                  this.menuTemY[var6] = this.menuTemY[var6 - 1];
               }

               this.menuTemY[0] = GameCanvas.p;
               if ((cmtoX -= var5) < 0) {
                  cmtoX = 0;
               }

               if (cmtoX > cmdy) {
                  cmtoX = cmdy;
               }

               if (cmx < 0 || cmx > cmdy) {
                  var5 /= 2;
               }

               cmx -= var5;
            }
         }

         if (GameCanvas.o && this.w) {
            var5 = GameCanvas.p - this.menuTemY[0];
            GameCanvas.o = false;
            if (Res.abs(var5) < 20 && Res.abs(GameCanvas.p - this.u) < 20 && !this.x) {
               this.z = 0;
               cmtoX = cmx;
               this.u = -1000;
               this.menuSelectedItem = (cmtoX + GameCanvas.p - this.menuX) / this.menuW;
               this.t = 0;
               this.y = 10;
            } else if (this.menuSelectedItem != -1 && this.t > 5) {
               this.t = 0;
               this.y = 1;
            } else if (this.menuSelectedItem == -1 && !this.x) {
               if (cmx < 0) {
                  cmtoX = 0;
               } else if (cmx > cmdy) {
                  cmtoX = cmdy;
               } else {
                  byte var4;
                  if ((var6 = GameCanvas.p - this.menuTemY[0] + (this.menuTemY[0] - this.menuTemY[1]) + (this.menuTemY[1] - this.menuTemY[2])) > 10) {
                     var4 = 10;
                  } else if (var6 < -10) {
                     var4 = -10;
                  } else {
                     var4 = 0;
                  }

                  this.z = -var4 * 100;
               }
            }

            this.w = false;
            this.t = 0;
            GameCanvas.o = false;
         }

         GameCanvas.l();
         GameCanvas.m();
      }

   }

   public final void paintMenu(mGraphics var1) {
      try {
         var1.translate(-var1.b(), -var1.c());
         var1.translate(-cmx, 0);
         int var2;
         String[] var3;
         int var4;
         int var5;
         if (GameCanvas.isTouch) {
            for(var2 = 0; var2 < this.menuItems.size(); ++var2) {
               if (var2 == this.menuSelectedItem) {
                  var1.a(btnlBig1, this.menuX + var2 * this.menuW + 1, this.i + 1, 0);
               } else {
                  var1.a(btnlBig0, this.menuX + var2 * this.menuW + 1, this.i + 1, 0);
               }

               if ((var3 = ((Command1)this.menuItems.elementAt(var2)).subCaption) == null) {
                  var3 = new String[]{((Command1)this.menuItems.elementAt(var2)).caption};
               }

               var4 = this.i + (this.menuH - var3.length * 14) / 2 + 1;

               for(var5 = 0; var5 < var3.length; ++var5) {
                  if (GameScr.cc) {
                     if (ChatManager.gI().findWaitPerson(var3[var5])) {
                        if (GameCanvas.u % 10 > 5) {
                           mFont.tahoma_7_red.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                        } else {
                           mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                        }
                     } else {
                        mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                     }
                  } else {
                     mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                  }
               }
            }
         } else {
            for(var2 = 0; var2 < this.menuItems.size(); ++var2) {
               if (var2 == this.menuSelectedItem) {
                  var1.a(btnlBig1, this.menuX + var2 * this.menuW + 1, this.i + 1 - 23, 0);
               } else {
                  var1.a(btnlBig0, this.menuX + var2 * this.menuW + 1, this.i + 1 - 23, 0);
               }

               if ((var3 = ((Command1)this.menuItems.elementAt(var2)).subCaption) == null) {
                  var3 = new String[]{((Command1)this.menuItems.elementAt(var2)).caption};
               }

               var4 = this.i + (this.menuH - var3.length * 14) / 2 + 1 - 23;

               for(var5 = 0; var5 < var3.length; ++var5) {
                  if (GameScr.cc) {
                     if (ChatManager.gI().findWaitPerson(var3[var5])) {
                        if (GameCanvas.u % 10 > 5) {
                           mFont.tahoma_7_red.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                        } else {
                           mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                        }
                     } else {
                        mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                     }
                  } else {
                     mFont.tahoma_7_yellow.a(var1, var3[var5], this.menuX + var2 * this.menuW + this.menuW / 2 - 2, var4 + var5 * 14, 2);
                  }
               }
            }
         }

         var1.translate(-var1.b(), -var1.c());
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public final void moveCamera() {
      if (this.z != 0 && !this.w) {
         if ((cmtoX += this.z / 100) < 0) {
            cmtoX = 0;
         } else if (cmtoX > cmdy) {
            cmtoX = cmdy;
         } else {
            cmx = cmtoX;
         }

         this.z = this.z * 9 / 10;
         if (this.z < 100 && this.z > -100) {
            this.z = 0;
         }
      }

      if (cmx != cmtoX && !this.w) {
         this.aa = cmtoX - cmx << 2;
         this.ab += this.aa;
         cmx += this.ab >> 4;
         this.ab &= 15;
      }

      if (this.i > this.menuY) {
         int var1;
         if ((var1 = this.i - this.menuY >> 1) <= 0) {
            var1 = 1;
         }

         this.i -= var1;
      }

      if (cmvy != 0 && (cmvy >>= 1) < 0) {
         cmvy = 0;
      }

      if (this.y > 0) {
         --this.y;
         GameScr.cc = false;
         if (this.y == 0) {
            this.showMenu = false;
            Command1 var2;
            if (this.menuSelectedItem >= 0 && (var2 = (Command1)this.menuItems.elementAt(this.menuSelectedItem)) != null) {
               var2.a();
            }
         }
      }

   }

   public final boolean selectTaskForAuto(int optionAttempt, String preferredCaption) {
      if (!this.showMenu || this.menuItems == null || this.menuItems.size() == 0) {
         return false;
      }

      boolean completeTask = "#complete-task".equals(preferredCaption);
      boolean joinClass = preferredCaption != null && preferredCaption.startsWith("#join-class:");
      boolean upgradeItem = "#upgrade-item".equals(preferredCaption);
      boolean talkOnly = "#talk-only".equals(preferredCaption);
      int joinClassId = joinClass ? preferredCaption.charAt(preferredCaption.length() - 1) - '0' : 0;
      int talk = -1;
      int preferred = -1;
      int joinSchool = -1;
      int acceptTask = -1;
      int namedMainTask = -1;
      int completeCaption = -1;
      int serverTaskCount = 0;
      int answerCount = 0;
      int bothCasesAnswer = -1;
      int strengthClassesAnswer = -1;
      int classCombinationAnswerCount = 0;
      int popupTaskCount = 0;
      int classChoiceCount = 0;
      String[] taskSevenCaptions = new String[this.menuItems.size()];
      for(int index = 0; index < this.menuItems.size(); ++index) {
         Command1 item = (Command1)this.menuItems.elementAt(index);
         if (item != null) {
            String caption = item.caption == null ? "" : item.caption.toLowerCase();
            taskSevenCaptions[index] = item.caption;
            System.out.println("AutoNVC menu[" + index + "] caption=" + item.caption + " action=" + item.idAction);
            // Quiz captions can arrive with a server-specific action id or
            // broken Vietnamese encoding. Detect the explicit final answer
            // independently from the action-id classification below.
            if (bothCasesAnswer < 0 && (isBothCasesAnswerCaption(caption)
                  || this.menuItems.size() == 3 && index == 2 && caption.indexOf("2") >= 0)) {
               bothCasesAnswer = index;
            }
            // Early tutorial quiz at Tabemono asks which classes should
            // prioritise Strength.  Some servers send these answer buttons
            // with action IDs that are not 88821, so action-id based quiz
            // detection below never selects anything.  Detect the visible
            // three-class answer groups directly; the correct group is
            // Kiem + Kunai + Dao (same rule shown by the client tip text).
            if (isThreeClassCombinationCaption(caption)) {
               ++classCombinationAnswerCount;
               if (strengthClassesAnswer < 0 && isStrengthClassesAnswerCaption(caption)) {
                  strengthClassesAnswer = index;
               }
            }
            if (preferred < 0 && completeTask && isCompleteTaskCaption(caption)) {
               preferred = index;
            } else if (preferred < 0 && joinClass && isClassCaption(caption, joinClassId)) {
               preferred = index;
            } else if (preferred < 0 && upgradeItem
                  && (isUpgradeItemCaption(caption) || isNormalUpgradeCaption(caption))) {
               preferred = index;
            } else if (preferred < 0 && !completeTask && !joinClass && !upgradeItem && !talkOnly
                  && preferredCaption != null && preferredCaption.length() > 0
                  && caption.indexOf(preferredCaption.toLowerCase()) >= 0) {
               preferred = index;
            }

            if (talk < 0 && (caption.indexOf("nói chuyện") >= 0 || caption.indexOf("noi chuyen") >= 0 || caption.indexOf("talk") >= 0)) {
               talk = index;
            }

            // Unicode-safe fallback for the explicit talk command.
            if (talk < 0 && isTalkCaption(caption)) {
               talk = index;
            }

            if (joinSchool < 0 && isJoinSchoolCaption(caption)) {
               joinSchool = index;
            }

            // Some Ronin task popups (notably "Ren luyen the luc") use a
            // server-specific action ID instead of 88822/11057.  The visible
            // Nhận button remains the reliable signal; prefer it over a
            // generic "Noi chuyen" fallback.
            if (acceptTask < 0 && isAcceptTaskCaption(caption)) {
               acceptTask = index;
            }

            if (namedMainTask < 0 && isMainTaskCaption(caption)) {
               namedMainTask = index;
            }

            if (completeCaption < 0 && isCompleteTaskCaption(caption)) {
               completeCaption = index;
            }

            if (isAnyClassCaption(caption)) {
               ++classChoiceCount;
            }

            // Joining a class uses two menus: "Nhap hoc" first, followed by
            // the actual class name requested through preferredCaption.
            if (joinSchool < 0 && (caption.indexOf("nhập học") >= 0 || caption.indexOf("nhap hoc") >= 0)) {
               joinSchool = index;
            }

            if (item.idAction == 88822) {
               ++serverTaskCount;
            } else if (item.idAction == 88821) {
               ++answerCount;
            } else if (item.idAction == 11057) {
               ++popupTaskCount;
            }
         }
      }

      // Recover from an old/wrong "Nhap hoc" click while doing a normal
      // school-tour conversation. A real class-join flow carries #join-class.
      if (!joinClass && preferredCaption == null && classChoiceCount > 0
            && classChoiceCount == this.menuItems.size()) {
         this.showMenu = false;
         System.out.println("AutoNVC menu close-stale-class-submenu choices=" + classChoiceCount);
         return false;
      }

      int action = serverTaskCount > 0 ? 88822 : (answerCount > 0 ? 88821 : (popupTaskCount > 0 ? 11057 : -1));
      int candidateCount = serverTaskCount > 0 ? serverTaskCount : (answerCount > 0 ? answerCount : popupTaskCount);
      int wanted = candidateCount > 0 ? optionAttempt % candidateCount : 0;
      int selected = -1;
      int taskSevenAnswer = AutoNvcQuizPolicy.findTaskSevenAnswer(taskSevenCaptions);
      if (talkOnly) {
         // NV0 step 4 shares an NPC menu with "Nhan thuong nang cap".
         // Only the explicit talk caption is valid; never fall back to a
         // generic Nhận/action-id candidate from that menu.
         selected = talk;
      } else if (preferred >= 0) {
         selected = preferred;
      } else if (this.menuItems.size() == 1 && completeCaption == 0) {
         // Also handle a report step whose server metadata did not identify
         // it as #complete-task but whose sole visible caption is explicit.
         selected = 0;
      } else if (taskSevenAnswer >= 0) {
         selected = taskSevenAnswer;
         System.out.println("AutoNVC quiz=task7 selected=" + selected);
      } else if (bothCasesAnswer >= 0) {
         // Tutorial question: "Tien Yen kiem duoc bang cach nao?"
         // Ronin's correct answer is the third option, "Ca 2 truong hop".
         selected = bothCasesAnswer;
      } else if (classCombinationAnswerCount >= 2 && strengthClassesAnswer >= 0) {
         // Tabemono tutorial quiz.  Require multiple three-class choices so
         // this never hijacks a normal NPC menu containing one class label.
         selected = strengthClassesAnswer;
         System.out.println("AutoNVC quiz=strength-classes selected=" + selected);
      } else if (namedMainTask >= 0) {
         // A visible "NV ..." entry is the server's actual next quest.
         // It must win over shop/free-item entries such as "Nhan kiem go".
         selected = namedMainTask;
      } else if (completeTask) {
         // Some village report menus (seen around main tasks 25-27) expose
         // only one server command and its caption can be mojibake/truncated.
         // The caller has already identified this as a report step, so the
         // sole non-null command is the safe "Hoan thanh nhiem vu" fallback.
         if (this.menuItems.size() == 1 && this.menuItems.elementAt(0) != null) {
            selected = 0;
            System.out.println("AutoNVC menu complete-task single-option fallback");
         }
      } else if (joinClass) {
         selected = joinSchool;
      } else if (upgradeItem) {
         // Ronin's second upgrade menu contains exactly:
         // Thuong, Can than, Huong dan. The normal mode is item zero.
         selected = this.menuItems.size() == 3 ? 0 : -1;
      } else if (preferredCaption != null && preferredCaption.length() > 0 && joinSchool >= 0) {
         selected = joinSchool;
      } else if (acceptTask >= 0) {
         selected = acceptTask;
      } else if (talk >= 0) {
         // Normal NPC menus often put "Noi chuyen" in the final slot after
         // reward/shop commands. Those commands can share generic server
         // action IDs, so choose the explicit talk caption first.
         selected = talk;
      } else if (action >= 0) {
         int candidate = 0;
         for(int index = 0; index < this.menuItems.size(); ++index) {
            Command1 item = (Command1)this.menuItems.elementAt(index);
            if (item != null && item.idAction == action) {
               if (candidate == wanted) {
                  selected = index;
                  break;
               }

               ++candidate;
            }
         }
      }

      if (selected < 0 || selected >= this.menuItems.size()) {
         if (completeTask || joinClass || upgradeItem || talkOnly) {
            // Bo menu con sot lai tu NPC truoc de lan sau mo dung menu bao cao.
            this.showMenu = false;
            System.out.println("AutoNVC menu special ignored: khong co nut phu hop " + preferredCaption);
         }
         System.out.println("AutoNVC menu ignored: khong co lenh nhiem vu/noi chuyen an toan");
         return false;
      }

      this.menuSelectedItem = selected;
      Command1 command = (Command1)this.menuItems.elementAt(selected);
      this.showMenu = false;
      if (command != null) {
         System.out.println("AutoNVC menu selected=" + selected + " caption=" + command.caption + " action=" + command.idAction);
         command.a();
         return true;
      }

      return false;
   }

   private static boolean isThreeClassCombinationCaption(String caption) {
      if (caption == null) return false;
      int classes = 0;
      if (hasAny(caption, "kiếm", "kiem", "ki?m")) ++classes;
      if (hasAny(caption, "kunai")) ++classes;
      if (hasAny(caption, "đao", "dao", "?ao")) ++classes;
      if (hasAny(caption, "phi tiêu", "phi tieu", "phi ti?u", "tiêu", "tieu")) ++classes;
      if (hasAny(caption, "cung")) ++classes;
      if (hasAny(caption, "quạt", "quat", "qu?t")) ++classes;
      return classes >= 3;
   }

   private static boolean isStrengthClassesAnswerCaption(String caption) {
      return caption != null
            && hasAny(caption, "kiếm", "kiem", "ki?m")
            && hasAny(caption, "kunai")
            && hasAny(caption, "đao", "dao", "?ao");
   }

   private static boolean hasAny(String value, String... candidates) {
      if (value == null || candidates == null) return false;
      for (int i = 0; i < candidates.length; ++i) {
         if (candidates[i] != null && value.indexOf(candidates[i]) >= 0) return true;
      }
      return false;
   }

   private static boolean isBothCasesAnswerCaption(String caption) {
      if (caption == null) return false;
      if ((caption.indexOf("c\u1ea3 2") >= 0 || caption.indexOf("ca 2") >= 0)
            && (caption.indexOf("tr\u01b0\u1eddng h\u1ee3p") >= 0
                  || caption.indexOf("truong hop") >= 0)) {
         return true;
      }
      return (caption.indexOf("cả 2") >= 0 || caption.indexOf("ca 2") >= 0
            || caption.indexOf("c? 2") >= 0)
            && (caption.indexOf("trường hợp") >= 0 || caption.indexOf("truong hop") >= 0
                  || caption.indexOf("tr") >= 0 && caption.indexOf("ng h") >= 0);
   }

   /** Select an exact server menu slot when a task uses an unknown action ID. */
   public final boolean selectMenuIndexForAuto(int index) {
      if (!this.showMenu || this.menuItems == null || index < 0 || index >= this.menuItems.size()) {
         return false;
      }
      Command1 command = (Command1)this.menuItems.elementAt(index);
      if (command == null) {
         return false;
      }
      this.menuSelectedItem = index;
      this.showMenu = false;
      System.out.println("AutoNVC menu direct-selected=" + index + " caption="
            + command.caption + " action=" + command.idAction);
      command.a();
      return true;
   }

   private static boolean isAcceptTaskCaption(String caption) {
      if (caption == null) return false;
      String value = caption.trim();
      // "Nhap hoc" is not a task-accept command.
      if (isJoinSchoolCaption(value)) return false;
      // Free starter equipment is not a task acceptance button. Selecting it
      // repeatedly fills the bag with wooden swords while the real "NV ..."
      // entry remains unselected.
      if (value.indexOf("ki?m") >= 0 || value.indexOf("kiem") >= 0
            || value.indexOf("vũ khí") >= 0 || value.indexOf("vu khi") >= 0) {
         return false;
      }
      // Covers UTF-8, the old client font's "nh?n" fallback, and English.
      return value.startsWith("nh") && value.indexOf("huy") < 0 && value.indexOf("huỷ") < 0
            || value.indexOf("accept") >= 0 || value.indexOf("receive") >= 0
            // This Ronin task is itself the selectable task caption rather
            // than a separate Nhận button.
            || value.indexOf("ren luyen") >= 0 || value.indexOf("rèn luyện") >= 0
            || value.indexOf("the luc") >= 0 || value.indexOf("thể lực") >= 0;
   }

   private static boolean isTalkCaption(String caption) {
      if (caption == null) return false;
      if (caption.indexOf("n\u00f3i chuy\u1ec7n") >= 0
            || caption.indexOf("n\u00f3i chuy") >= 0) {
         return true;
      }
      return caption.indexOf("nói chuy") >= 0 || caption.indexOf("noi chuy") >= 0
            || caption.indexOf("nÃ³i chuy") >= 0 || caption.indexOf("talk") >= 0;
   }

   private static boolean isOkanechanFunctionCaption(String caption) {
      if (caption == null) return false;
      return caption.indexOf("nhận thưởng") >= 0 || caption.indexOf("nh?n thưởng") >= 0
            || caption.indexOf("đổi lượng") >= 0 || caption.indexOf("doi luong") >= 0
            || caption.indexOf("mã quà") >= 0 || caption.indexOf("ma qua") >= 0;
   }

   private static boolean isMainTaskCaption(String caption) {
      if (caption == null) return false;
      String value = caption.trim().toLowerCase();
      return value.startsWith("nv ") || value.startsWith("nv:")
            || value.startsWith("nhiem vu") || value.startsWith("nhiá»‡m vá»¥");
   }

   public final boolean selectDailyTaskForAuto(int option) {
      if (!this.showMenu || this.menuItems == null || this.menuItems.size() == 0
            || Char.getMyChar() == null || Char.getMyChar().npcFocus == null
            || Char.getMyChar().npcFocus.template == null
            || Char.getMyChar().npcFocus.template.npcTemplateId != 25) {
         return false;
      }

      int selected = -1;
      int popupTaskCount = 0;
      int dailyCaptionCount = 0;
      for(int index = 0; index < this.menuItems.size(); ++index) {
         Command1 item = (Command1)this.menuItems.elementAt(index);
         if (item == null) {
            continue;
         }

         String caption = item.caption == null ? "" : item.caption.toLowerCase();
         if (item.idAction == 11057) {
            ++popupTaskCount;
         }
         if (isDailyTaskCaption(caption, 0) || isDailyTaskCaption(caption, 2)
               || isDailyTaskCaption(caption, 3) || caption.indexOf("huy") >= 0
               || caption.indexOf("há»§y") >= 0) {
            ++dailyCaptionCount;
         }
         if (selected < 0 && isDailyTaskCaption(caption, option)) {
            selected = index;
         }
      }

      // Menu nhiem vu hang ngay cua server Ronin gom dung bon nut va duoc
      // gui bang action 11057. Dung index lam fallback neu text bi loi font.
      if (selected < 0 && this.menuItems.size() == 4
            && (popupTaskCount == 4 || dailyCaptionCount >= 2)
            && option >= 0 && option < this.menuItems.size()) {
         Command1 fallback = (Command1)this.menuItems.elementAt(option);
         if (fallback != null) {
            selected = option;
         }
      }

      if (selected < 0) {
         return false;
      }

      this.menuSelectedItem = selected;
      Command1 command = (Command1)this.menuItems.elementAt(selected);
      this.showMenu = false;
      System.out.println("AutoNVHN menu selected=" + selected + " caption=" + command.caption
            + " action=" + command.idAction + " option=" + option);
      command.a();
      return true;
   }

   /** Select Rikudou's top-level "NV moi ngay" entry before its 4-button submenu. */
   public final boolean selectDailyRootForAuto() {
      if (!this.showMenu || this.menuItems == null || this.menuItems.size() == 0
            || Char.getMyChar() == null || Char.getMyChar().npcFocus == null
            || Char.getMyChar().npcFocus.template == null
            || Char.getMyChar().npcFocus.template.npcTemplateId != 25) {
         return false;
      }
      for (int index = 0; index < this.menuItems.size(); ++index) {
         Command1 item = (Command1)this.menuItems.elementAt(index);
         String caption = item == null || item.caption == null ? "" : item.caption.trim().toLowerCase();
         // Covers proper UTF-8, unaccented text and the client's '?' glyph.
         boolean dailyRoot = caption.startsWith("nv ")
               && (caption.indexOf("mỗi") >= 0 || caption.indexOf("moi") >= 0
                     || caption.indexOf("m?i") >= 0)
               && (caption.indexOf("ngày") >= 0 || caption.indexOf("ngay") >= 0
                     || caption.indexOf("ng") >= 0);
         if (!dailyRoot) continue;
         this.menuSelectedItem = index;
         this.showMenu = false;
         System.out.println("AutoNVHN root selected=" + index + " caption=" + item.caption
               + " action=" + item.idAction);
         item.a();
         return true;
      }
      return false;
   }

   /** Select the first server menu command whose caption contains a keyword. */
   public final boolean selectCaptionForStandaloneAuto(String[] keywords, String logTag) {
      if (!this.showMenu || this.menuItems == null || keywords == null) {
         return false;
      }
      for (int index = 0; index < this.menuItems.size(); ++index) {
         Command1 command = (Command1)this.menuItems.elementAt(index);
         String caption = command == null || command.caption == null ? "" : command.caption.toLowerCase();
         for (int k = 0; k < keywords.length; ++k) {
            String keyword = keywords[k] == null ? "" : keywords[k].toLowerCase();
            if (keyword.length() == 0 || caption.indexOf(keyword) < 0) continue;
            this.menuSelectedItem = index;
            this.showMenu = false;
            System.out.println(logTag + " menu selected=" + index + " caption=" + command.caption + " action=" + command.idAction);
            command.a();
            return true;
         }
      }
      return false;
   }

   private static boolean isDailyTaskCaption(String caption, int option) {
      if (caption == null) {
         return false;
      }

      switch(option) {
      case 0:
         return caption.indexOf("nh\u1eadn") >= 0 || caption.indexOf("nhan") >= 0
               || caption.indexOf("receive") >= 0;
      case 2:
         return caption.indexOf("ho\u00e0n th\u00e0nh") >= 0 || caption.indexOf("hoan thanh") >= 0
               || caption.indexOf("complete") >= 0;
      case 3:
         return caption.indexOf("\u0111i l\u00e0m") >= 0 || caption.indexOf("di lam") >= 0
               || caption.indexOf("l\u00e0m nv") >= 0 || caption.indexOf("lam nv") >= 0
               || caption.indexOf("do task") >= 0;
      default:
         return false;
      }
   }

   private static boolean isCompleteTaskCaption(String caption) {
      if (caption == null) {
         return false;
      }

      // Ho tro text Unicode, khong dau va text bi mojibake tren client Java cu.
      return caption.indexOf("ho\u00e0n th\u00e0nh nhi\u1ec7m v\u1ee5") >= 0
            || caption.indexOf("hoan thanh nhiem vu") >= 0
            || caption.indexOf("complete task") >= 0
            || caption.startsWith("ho") && caption.indexOf("nhi") >= 0;
   }

   private static boolean isJoinSchoolCaption(String caption) {
      if (caption == null) {
         return false;
      }

      return caption.indexOf("nh\u1eadp h\u1ecdc") >= 0
            || caption.indexOf("nhap hoc") >= 0
            || caption.indexOf("gia nh\u1eadp") >= 0
            || caption.indexOf("gia nhap") >= 0;
   }

   private static boolean isClassCaption(String caption, int classId) {
      if (caption == null) {
         return false;
      }

      switch(classId) {
      case 1:
         return caption.indexOf("ki\u1ebfm") >= 0 || caption.indexOf("kiem") >= 0;
      case 2:
         return caption.indexOf("ti\u00eau") >= 0 || caption.indexOf("tieu") >= 0;
      case 3:
         return caption.indexOf("kunai") >= 0;
      case 4:
         return caption.indexOf("cung") >= 0;
      case 5:
         return caption.indexOf("\u0111ao") >= 0 || caption.indexOf("dao") >= 0;
      case 6:
         return caption.indexOf("qu\u1ea1t") >= 0 || caption.indexOf("quat") >= 0;
      default:
         return false;
      }
   }

   private static boolean isAnyClassCaption(String caption) {
      for (int classId = 1; classId <= 6; ++classId) {
         if (isClassCaption(caption, classId)) return true;
      }
      return false;
   }

   private static boolean isUpgradeItemCaption(String caption) {
      if (caption == null) {
         return false;
      }

      return caption.indexOf("n\u00e2ng c\u1ea5p") >= 0
            || caption.indexOf("nang cap") >= 0
            || caption.indexOf("upgrade") >= 0;
   }

   private static boolean isNormalUpgradeCaption(String caption) {
      if (caption == null) {
         return false;
      }

      return caption.indexOf("th\u01b0\u1eddng") >= 0
            || caption.indexOf("thuong") >= 0
            || caption.indexOf("normal") >= 0;
   }

   public final void perform(int var1, Object var2) {
      if (var1 == 1000) {
         ChatTab var3 = (ChatTab)var2;
         this.menuItems.removeAllElements();
         ChatManager.gI().removeFromWaitList(var3.ownerName);
         ChatManager.gI().chatTabs.removeElement(var3);

         for(var1 = 0; var1 < ChatManager.gI().chatTabs.size(); ++var1) {
            ChatTab var4 = (ChatTab)ChatManager.gI().chatTabs.elementAt(var1);
            this.menuItems.addElement(new Command1(var4.ownerName, (IActionListener)null, 12001, new Integer(var1)));
         }

         this.menuItems.addElement(new Command1(mResources.pz, (IActionListener)null, 12006, (Object)null));
         this.menuItems.addElement(new Command1(mResources.qa, (IActionListener)null, 12008, (Object)null));

         for(var1 = 0; var1 < this.menuItems.size(); ++var1) {
            Command1 var5 = (Command1)this.menuItems.elementAt(var1);
            if (mFont.tahoma_7_yellow.a(var5.caption) > this.menuW - 8) {
               var5.subCaption = mFont.tahoma_7_yellow.splitFontArray(var5.caption, this.menuW - 8);
            }
         }

         cmdy = this.menuItems.size() * this.menuW - GameCanvas.z;
         if ((cmtoX = this.menuSelectedItem * this.menuW + this.menuW - GameCanvas.z / 2) > cmdy) {
            cmtoX = cmdy;
         }

         if (cmtoX < 0) {
            cmtoX = 0;
         }

         if (this.menuSelectedItem == this.menuItems.size() - 1 || this.menuSelectedItem == 0) {
            cmx = cmtoX;
         }
      }

   }

   public static void d() {
      cmtoX = 0;
      cmx = 0;
      cmdy = 0;
      cmvy = 0;
      btnlBig0 = null;
      btnlBig1 = null;
   }

   static {
      Main.main(46);
      init();
   }
}
