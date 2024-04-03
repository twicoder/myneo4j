package org.neo4j.util.shell.apps.extra;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.neo4j.util.shell.AbstractApp;
import org.neo4j.util.shell.CommandParser;
import org.neo4j.util.shell.Output;
import org.neo4j.util.shell.Session;
import org.neo4j.util.shell.ShellException;

/**
 * A way to execute groovy scripts from the shell. It doesn't use the groovy
 * classes directly, but instead purely via reflections... This gives the
 * advantage of not being dependent on groovy at compile-time.
 * 
 * So if the groovy classes is in the classpath at run-time then groovy scripts
 * can be executed, otherwise it will say that "Groovy isn't available".
 * 
 * It has the old style script/argument format of:
 * sh$ gsh --script1 arg1 arg2 arg3 --script2 arg1 arg2
 * 
 * The paths to look for groovy scripts is decided by the environment variable
 * GSH_PATH, also there are some default paths: ".", "script", "src/script".
 */
public class Gsh extends AbstractApp
{
	public static final String BINDING_CLASS = "groovy.lang.Binding";
	public static final String ENGINE_CLASS = "groovy.util.GroovyScriptEngine";
	public static final String PATH_STRING = "GSH_PATH";
	
	public String execute( CommandParser parser, Session session, Output out )
		throws ShellException
	{
		this.ensureGroovyIsInClasspath();
		if ( parser.getLineWithoutCommand().trim().length() == 0 )
		{
			throw new ShellException( "Need to supply groovy scripts" );
		}
		
		List<String> pathList = this.getEnvPaths( session );
		Object groovyScriptEngine = this.newGroovyScriptEngine(
			pathList.toArray( new String[ pathList.size() ] ) );
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put( "out", out );
		properties.put( "session", session );
		this.runGroovyScripts( groovyScriptEngine, properties, parser );
		return null;
	}
	
	private void runGroovyScripts( Object groovyScriptEngine,
		Map<String, Object> properties, CommandParser parser )
		throws ShellException
	{
		ArgReader reader = new ArgReader(
			CommandParser.tokenizeStringWithQuotes(
				parser.getLineWithoutCommand() ) );
		HashMap<String, Object> hashMap =
			( HashMap<String, Object> ) properties;
		while ( reader.hasNext() )
		{
			String arg = reader.next();
			if ( arg.startsWith( "--" ) )
			{
				String[] scriptArgs = getScriptArgs( reader );
				String scriptName = arg.substring( 2 );
				Map<String, Object> props =
					( Map<String, Object> ) hashMap.clone();
				props.put( "args", scriptArgs );
				this.runGroovyScript( groovyScriptEngine, scriptName,
					this.newGroovyBinding( props ) );
			}
		}
	}
	
	private void runGroovyScript( Object groovyScriptEngine,
		String scriptName, Object groovyBinding ) throws ShellException
	{
		try
		{
			Method runMethod = groovyScriptEngine.getClass().getMethod(
				"run", String.class, groovyBinding.getClass() );
			runMethod.invoke( groovyScriptEngine, scriptName + ".groovy",
				groovyBinding );
		}
		catch ( Exception e )
		{
			throw new ShellException( "Groovy exception: " +
				this.findProperMessage( e ), e );
		}
	}
	
	private String findProperMessage( Throwable e )
	{
		String message = e.getMessage();
		if ( ( message == null || message.length() == 0 ) &&
			e.getCause() != null )
		{
			message = this.findProperMessage( e.getCause() );
		}
		return message;
	}
	
	private String[] getScriptArgs( ArgReader reader )
	{
		reader.mark();
		try
		{
			ArrayList<String> list = new ArrayList<String>();
			while ( reader.hasNext() )
			{
				String arg = reader.next();
				if ( arg.startsWith( "--" ) )
				{
					break;
				}
				list.add( arg );
				reader.mark();
			}
			return list.toArray( new String[ list.size() ] );
		}
		finally
		{
			reader.flip();
		}
	}

	private List<String> getEnvPaths( Session session )
		throws ShellException
	{
		try
		{
			String env = ( String ) session.get( PATH_STRING );
			List<String> list = new ArrayList<String>();
			if ( env != null && env.trim().length() > 0 )
			{
				for ( String path : env.split( ":" ) )
				{
					list.add( path );
				}
			}
			// Some default paths
			list.add( "." );
			list.add( "script" );
			list.add( "src/script" );
			return list;
		}
		catch ( RemoteException e )
		{
			throw new ShellException( e );
		}
	}
	
	private Object newGroovyBinding( Map<String, Object> properties )
		throws ShellException
	{
		try
		{
			Class cls = Class.forName( BINDING_CLASS );
			Object binding = cls.newInstance();
			Method setPropertyMethod =
				cls.getMethod( "setProperty", String.class, Object.class );
			for ( String key : properties.keySet() )
			{
				setPropertyMethod.invoke( binding, key, properties.get( key ) );
			}
			return binding;
		}
		catch ( Exception e )
		{
			throw new ShellException( "Invalid groovy classes", e );
		}
	}

	private Object newGroovyScriptEngine( String[] paths )
		throws ShellException
	{
		try
		{
			Class cls = Class.forName( ENGINE_CLASS );
			return cls.getConstructor( String[].class ).newInstance(
				new Object[] { paths } );
		}
		catch ( Exception e )
		{
			throw new ShellException( "Invalid groovy classes", e );
		}
	}
	
	private void ensureGroovyIsInClasspath() throws ShellException
	{
		try
		{
			Class.forName( BINDING_CLASS );
		}
		catch ( ClassNotFoundException e )
		{
			throw new ShellException( "Groovy couldn't be found", e );
		}
	}

	private static class ArgReader implements Iterator<String>
	{
		private static final int START_INDEX = -1;
		
		private int index = START_INDEX;
		private String[] args;
		private Integer mark;
		
		ArgReader( String[] args )
		{
			this.args = args;
		}
		
		public boolean hasNext()
		{
			return this.index + 1 < this.args.length;
		}
		
		public String next()
		{
			if ( !hasNext() )
			{
				throw new NoSuchElementException();
			}
			this.index++;
			return this.args[ this.index ];
		}
		
		public void previous()
		{
			this.index--;
			if ( this.index < START_INDEX )
			{
				this.index = START_INDEX;
			}
		}
		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		public void mark()
		{
			this.mark = this.index;
		}
		
		public void flip()
		{
			if ( this.mark == null )
			{
				throw new IllegalStateException();
			}
			this.index = this.mark;
			this.mark = null;
		}
	}
	
	@Override
	public String getDescription()
	{
		return "Runs groovy scripts. Usage: gsh <groovy script line>";
	}
}
