public final class Sender implements Runnable {
   private MyVector a;
   private Session_ME b;
   private Thread worker;

   public Sender(Session_ME var1) {
      this.b = var1;
      this.a = new MyVector();
   }

   public final void a() {
      synchronized (this.a) {
         this.a.removeAllElements();
         this.a.notifyAll();
      }
   }

   public final void a(Message var1) {
      synchronized (this.a) {
         if (!this.b.connected) return;
         this.a.addElement(var1);
         this.a.notifyAll();
      }
   }

   public final void wakeUp() {
      synchronized (this.a) { this.a.notifyAll(); }
   }

   final void bindWorker(Thread next) {
      synchronized (this.a) {
         if (this.worker != null && this.worker != next) this.worker.interrupt();
         this.worker = next;
         this.a.notifyAll();
      }
   }

   final void stopWorker() {
      synchronized (this.a) {
         if (this.worker != null) this.worker.interrupt();
         this.worker = null;
         this.a.removeAllElements();
         this.a.notifyAll();
      }
   }

   public final void run() {
      Thread current = Thread.currentThread();
      try {
         while (!current.isInterrupted()) {
            Message message;
            synchronized (this.a) {
               while (this.worker == current && this.b.connected && !current.isInterrupted()
                     && (!Session_ME.g(this.b) || this.a.size() == 0)) {
                  this.a.wait();
               }
               if (this.worker != current || !this.b.connected || current.isInterrupted()) return;
               message = (Message)this.a.elementAt(0);
               this.a.removeElementAt(0);
            }
            // Socket I/O must not hold the queue monitor: producers and
            // disconnect need to be able to clear/wake this queue.
            this.b.sendQueuedMessage(message, current);
         }
      } catch (InterruptedException stopped) {
         current.interrupt();
      }
   }
}
