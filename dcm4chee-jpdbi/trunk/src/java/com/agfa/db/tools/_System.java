// $Id: _System.java 13140 2010-04-10 17:58:33Z kianusch $

package com.agfa.db.tools;

public class _System {
   static void exit(int ExitCode) {
       System.out.flush();
       System.out.close();
       System.err.flush();
       System.err.close();
       System.exit(ExitCode);
   }

   static void exit(int ExitCode, String ErrorText) {
       System.err.println(ErrorText);
       exit(ExitCode);
   }
}
