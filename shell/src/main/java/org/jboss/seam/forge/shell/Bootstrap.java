/*
 * JBoss, by Red Hat.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.seam.forge.shell;

import org.jboss.seam.forge.shell.events.AcceptUserInput;
import org.jboss.seam.forge.shell.events.ReinitializeEnvironment;
import org.jboss.seam.forge.shell.events.Shutdown;
import org.jboss.seam.forge.shell.events.Startup;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Singleton
public class Bootstrap
{
   @Inject
   private BeanManager manager;

   static {
      try {
         // check to see if we have something to work with.
         Class.forName("sun.misc.SignalHandler");

         SignalHandler signalHandler = new SignalHandler()
         {
            @Override
            public void handle(Signal signal)
            {
               if (signal.getName().equals("INT")) {
                  System.out.println("CTRL-C TRAPPED BITCHES!");
               }
            }

         };

         Signal.handle(new Signal("INT"), signalHandler);
      }
      catch (ClassNotFoundException e) {
        // signal trapping not supported. Oh well, switch to a Sun-based JVM, loser!
      }
   }

   public static void main(final String[] args)
   {
      loadPlugins();
      init(new File("").getAbsoluteFile(), false);
   }


   private static void init(final File workingDir, final boolean restartEvent)
   {

      System.out.println("foo");
      Runtime.getRuntime().addShutdownHook(new Thread()
      {
         @Override
         public void run()
         {
            System.out.println("WTF!");
            init(workingDir, true);
         }
      });
      initLogging();
      Weld weld = new Weld();
      WeldContainer container = weld.initialize();
      BeanManager manager = container.getBeanManager();
      manager.fireEvent(new Startup(workingDir, restartEvent));
      manager.fireEvent(new AcceptUserInput());
      weld.shutdown();
   }


   public void observeReinitialize(@Observes ReinitializeEnvironment event, Shell shell)
   {
      manager.fireEvent(new Shutdown());
      init(shell.getCurrentDirectory().getUnderlyingResourceObject(), true);
   }


   private static void initLogging()
   {
      String[] loggerNames = new String[]{"", "main", Logger.GLOBAL_LOGGER_NAME};
      for (String loggerName : loggerNames)
      {
         Logger globalLogger = Logger.getLogger(loggerName);
         Handler[] handlers = globalLogger.getHandlers();
         for (Handler handler : handlers)
         {
            handler.setLevel(Level.SEVERE);
            globalLogger.removeHandler(handler);
         }
      }
   }

   private static void loadPlugins()
   {
      try
      {
         File pluginsDir = new File(ShellImpl.FORGE_CONFIG_DIR + "/plugins/");
         if (pluginsDir.exists())
         {
            File[] files = pluginsDir.listFiles(new FilenameFilter()
            {
               @Override
               public boolean accept(File dir, String name)
               {
                  return name.endsWith(".jar");
               }
            });

            URL[] jars = new URL[files.length];

            for (int i = 0; i < files.length; i++)
            {
               jars[i] = files[i].toURI().toURL();
            }

            URLClassLoader classLoader = new URLClassLoader(jars, Bootstrap.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Deprecated
   public void observeStartup(@Observes final ContainerInitialized event)
   {
      manager.fireEvent(new Startup());
      manager.fireEvent(new AcceptUserInput());
   }
}
