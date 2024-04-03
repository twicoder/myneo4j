package org.myneo4j.impl;

import org.myneo4j.api.core.EmbeddedNeo;
import org.myneo4j.api.core.RelationshipType;
import org.myneo4j.impl.command.CommandManager;
import org.myneo4j.impl.transaction.LockManager;

public class Main
{
    private enum MyRelTypes implements RelationshipType
    {
    }

    private static EmbeddedNeo neo;

    private static void startupKernel()
    {
        neo = new EmbeddedNeo( MyRelTypes.class, "var/nioneo", true );
    }


    public static void main( String[] args )
    {
        startupKernel();
        Runtime.getRuntime().addShutdownHook( new ShutdownHook() );
    }

    private static class ShutdownHook extends Thread
    {
        ShutdownHook()
        {
        }

        public void run()
        {
            // Then dump lock information
            try
            {
                CommandManager.getManager().dumpStack();
                LockManager.getManager().dumpRagStack();
                LockManager.getManager().dumpAllLocks();
            }
            catch ( Throwable t )
            {
                // Don't use logging module, explicitly use stderr
                System.err.println( "Unable to dump Neo command stack and " +
                    "lock information: " + t );
            }
            neo.shutdown();
        }
    }

}
