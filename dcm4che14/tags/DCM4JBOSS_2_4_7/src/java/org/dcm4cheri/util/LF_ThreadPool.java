/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.util;

import org.apache.log4j.Logger;

/**
 * Leader/Follower Thread Pool 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3852 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020519 gunter zeilinger:</b>
 * <ul>
 * <li> initial import 
 * </ul>
 */
public class LF_ThreadPool
{
   // Constants -----------------------------------------------------
   private static final Logger log =
         Logger.getLogger(LF_ThreadPool.class);
   
   // Attributes ----------------------------------------------------
   private final Handler handler;
   private boolean shutdown = false;
   private Thread leader = null;
   private Object mutex = new Object();
   private int waiting = 0;
   private int running = 0;
   private int maxRunning = 0;
   private int maxWaiting = -1;
   private final int instNo = ++instCount;
   private int threadNo = 0;
   private final String name;
   
   // Static --------------------------------------------------------
   private static int instCount = 0;
   
   // Constructors --------------------------------------------------
   public LF_ThreadPool(Handler handler, String name) {
      if (handler == null)
         throw new NullPointerException();
      
      this.handler = handler;
      this.name = name;
   }
   
   // Public --------------------------------------------------------
   public int waiting()
   {
      return waiting;
   }
   
   public int running()
   {
      return running;
   }
   
   public boolean isShutdown()
   {
      return shutdown;
   }
   
   public int getMaxRunning()
   {
      return maxRunning;
   }
   
   public void setMaxRunning(int maxRunning)
   {
      if (maxRunning < 0)
         throw new IllegalArgumentException("maxRunning: " + maxRunning);
            
      this.maxRunning = maxRunning;
   }
   
   public int getMaxWaiting()
   {
      return maxWaiting;
   }
   
   public void setMaxWaiting(int maxWaiting)
   {
      if (maxWaiting < -1)
         throw new IllegalArgumentException("maxWaiting: " + maxWaiting);
            
      this.maxWaiting = maxWaiting;
   }
   
   public String toString()
   {
      return "LF_ThreadPool-" + instNo + "[leader:"
            + (leader == null ? "null" : leader.getName())
            + ", waiting:" + waiting
            + ", running: " + running + "(" + maxRunning
            + "), shutdown: " + shutdown + "]";
   }
   
   public void join()
   {
      log.debug("Thread: " + Thread.currentThread().getName() + " JOIN ThreadPool " + name);
      try {
	      while (!shutdown && (running == 0 || maxWaiting == -1 || waiting < maxWaiting)
	              && (maxRunning == 0 || (waiting + running) < maxRunning))
	      {
	         synchronized (mutex)
	         {
	            while (leader != null)
	            {
	               if (log.isDebugEnabled())
	                  log.debug("" + this + " - "
	                     + Thread.currentThread().getName() + " enter wait()");
	               ++waiting;
	               try { mutex.wait(); }
	               catch (InterruptedException ie)
	               {
	                  ie.printStackTrace();
	               }
	               finally { --waiting; }
	               if (log.isDebugEnabled())
	                  log.debug("" + this + " - "
	                     + Thread.currentThread().getName() + " awaked");
	            }
	            if (shutdown)
	               return;
	
	            leader = Thread.currentThread();
	            if (log.isDebugEnabled())
	               log.debug("" + this + " - New Leader"); 
	         }
	         ++running;
	         try {  
	            do {
	               handler.run(this);
	            } while (!shutdown && leader == Thread.currentThread());
	         } catch (Throwable th) {
	            log.warn("Exception thrown in " + Thread.currentThread().getName(), th);
	            shutdown();
	         } finally { --running; }
	      }
      } finally {
          log.debug("Thread: " + Thread.currentThread().getName() + " LEFT ThreadPool " + name);
      }
   }
   
   public boolean promoteNewLeader()
   {
      if (shutdown)
         return false;
      
      // only the current leader can promote the next leader
      if (leader != Thread.currentThread())
         throw new IllegalStateException();
      
      leader = null;
      
      // notify (one) waiting thread in join()
      synchronized (mutex) {
         if (waiting > 0)
         {
            if (log.isDebugEnabled())
               log.debug("" + this + " - promote new leader by notify"); 
            mutex.notify();
            return true;
         }
      }
            
      // if there is no waiting thread,
      // and the maximum number of running threads is not yet reached,
      if (maxRunning != 0 && running >= maxRunning) {
         if (log.isDebugEnabled())
            log.debug("" + this + " - Max number of threads reached"); 
         return false;
      }
      
      // start a new one
      if (log.isDebugEnabled())
         log.debug("" + this + " - promote new leader by add new Thread");
      addThread(
         new Runnable() {
            public void run() { join(); }
         }
      );
      
      return true;
   }
   
   public void shutdown() {
      if (log.isDebugEnabled())
         log.debug("" + this + " - shutdown"); 
      shutdown = true;
      leader = null;
      synchronized (mutex)
      {
         mutex.notifyAll();
      }
   }
         
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   // may be overloaded to take new thread from convential thread pool
   protected void addThread(Runnable r) {
       new Thread(r, name + "-" + (++threadNo)).start();
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   public interface Handler {
      void run(LF_ThreadPool pool);
   }
}
