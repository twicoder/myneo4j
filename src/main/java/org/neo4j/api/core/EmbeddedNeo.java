package org.neo4j.api.core;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;
import org.neo4j.impl.core.NodeManager;
import org.neo4j.impl.shell.NeoShellServer;

/**
 * The main Neo factory, with functionality to start and shutdown Neo, create
 * and get nodes and define valid relationship types. This class is typically
 * used in the outer loop in a Neo-enabled application, for example as follows:
 * <pre><code>
 * EmbeddedNeo neo = new EmbeddedNeo( MyRelationshipTypes.class, "var/neo", true );
 * // ... use neo
 * neo.shutdown();
 * </code></pre>
 * Neo is started when this class is instantiated. It provides operations to
 * {@link #createNode() create notes}, {@link #getNodeById(long) get nodes
 * given an id}, get the {@link #getReferenceNode() reference node} and
 * ultimately {@link #shutdown() shutdown Neo}. Typically, once instantiated
 * the reference to EmbeddedNeo is stored away in a service registry or in
 * a singleton instance.
 * <p>
 * Please note that after startup (i.e. constructor invocation), all operations
 * that read or write to the node space must be invoked in a {@link Transaction
 * transactional context}.
 */
public class EmbeddedNeo
{
	private static Logger log = Logger.getLogger( EmbeddedNeo.class.getName() );
    //private Class<? extends RelationshipType> validRelationshipTypes;
	private NeoShellServer shellServer;
	
	/**
	 * Creates an embedded neo instance with a given set of relationship types
	 * and that reads data from a given store.
	 * @param validRelationshipTypes an enum class containing your relationship types
	 * @param storeDir the store directory for the neo db files
	 * @param create whether a new store directory will be created if it doesn't
	 * already exist
 	 * @throws NullPointerException if clazz is <code>null</code>
 	 * @throws IllegalArgumentException if clazz is not an enum
	 */
	public EmbeddedNeo( Class<? extends RelationshipType> validRelationshipTypes,
		String storeDir, boolean create )
	{	    
		//this.validRelationshipTypes = validRelationshipTypes;
		this.shellServer = null;
		NeoJvmInstance.start( validRelationshipTypes, storeDir, create );
	}
	
	/**
	 * Creates an embedded neo instance with a given set of relationship types,
	 * that reads data from a given store which will be created if it doesn't
	 * already exist. Invoking this constructor is equivalent to invoke
	 * <code>new EmbeddedNeo( clazz, storeDir, true )</code>.
	 * @param validRelationshipTypes an enum class containing your relationship types
	 * @param storeDir the store directory for the neo db files
 	 * @throws NullPointerException if clazz is <code>null</code>
 	 * @throws IllegalArgumentException if clazz not an enum
	 */
	public EmbeddedNeo( Class<? extends RelationshipType> validRelationshipTypes,
		String storeDir )
	{
		this( validRelationshipTypes, storeDir, true );
	}
	
	public EmbeddedNeo( String dir, RelationshipType[] relationshipTypes )
	{
		this( null, dir, true );
		// TODO
	}

	public EmbeddedNeo( String dir, Iterable<RelationshipType> relationshipTypes )
	{
		this( null, dir, true );
	}
	    
	 	// Private accessor for the remote shell (started with enableRemoteShell())
	private NeoShellServer getShellServer()
	{
		return this.shellServer;
	}
	
	/**
	 * Creates a {@link Node}.
	 * @return the created node.
	 */
	public Node createNode()
	{
		return NodeManager.getManager().createNode();
	}
	
	/**
	 * Looks up a node by id.
	 * @param id the id of the node 
	 * @return the node with id <code>id</code> if found
	 * @throws RuntimeException if not found
	 */
	public Node getNodeById( long id )
	{
		return NodeManager.getManager().getNodeById( (int) id );
	}
	
	/**
	 * Returns the reference node.
	 * @return the reference node
	 * @throws RuntimeException if unable to get the reference node
	 */
	// TODO: Explain this concept
	public Node getReferenceNode()
	{
		return NodeManager.getManager().getReferenceNode();
	}
	
	/**
	 * Shuts down Neo. After this method has been invoked, it's invalid to
	 * invoke any methods in the Neo API.
	 */
	public void shutdown()
	{
		if ( getShellServer() != null )
		{
			try
			{
				getShellServer().shutdown();
			}
			catch ( Throwable t )
			{
				log.warning( "Error shutting down shell server: " + t );
			}
		}
		NeoJvmInstance.shutdown();
	}
	
	/**
	 * Returns the valid relationship types for this Neo instance invocation.
	 * This is the exact same class instance that was passed to the constructor
	 * of this EmbeddedNeo.
	 * @return the valid relationship types for this Neo instance invocation
	 */
//	public Class<? extends RelationshipType> getRelationshipTypes()
//	{
//	    return this.validRelationshipTypes;
//	}

	/**
	 * Enables remote shell access to this Neo instance, if the Neo4j
	 * <code>shell</code> project is available on the classpath. This will
	 * publish a shell access interface on an RMI registry on localhost (with
	 * configurable port and RMI binding name). It can be accessed by a
	 * client that implements <code>org.neo4j.util.shell.ShellClient</code>
	 * from the Neo4J <code>shell</code> project. Typically, the
	 * <code>neoshell</code> binary package is used (see
	 * <a href="http://neo4j.org/download">neo4j.org/download</a>).
	 * <p>
	 * The shell is parameterized by a map of properties passed in to this
	 * method. Currently, two properties are used:
	 * <ul>
	 *	<li><code>port</code>, an {@link Integer} describing the port of the RMI
	 * registry where the Neo shell will be bound, defaults to <code>1337</code>
	 *	<li><code>name</code>, the {@link String} under which the Neo shell will
	 * be bound in the RMI registry, defaults to <code>neoshell</code>
	 * </ul>
	 * @param initialProperties a set of properties that will be used to
	 * configure the remote shell
	 * @return <code>true</code> if the shell has been enabled,
	 * <code>false</code> otherwise (<code>false</code> usually indicates that
	 * the <code>shell</code> jar dependency is not on the classpath)
	 * @throws ClassCastException if the shell library is available, but one
	 * (or more) of the configuration properties have an unexpected type
	 * @throws IllegalStateException if the shell library is available, but
	 * the remote shell can't be enabled anyway
	 */
	public boolean enableRemoteShell( Map<String, Serializable>
		initialProperties )
	{
		try
		{
			if ( shellDependencyAvailable() )
			{
				this.shellServer = new NeoShellServer( this );
				Object port = initialProperties.get( "port" );
				Object name = initialProperties.get( "name" );
				this.shellServer.makeRemotelyAvailable( 
					port != null ? ( Integer ) port : 1337,
					name != null ? ( String ) name : "shell" );
				return true;
			}
			else
			{
				log.info( "Shell library not available. Neo shell not " +
					"started. Please add the Neo4j shell jar to the " +
					"classpath." );
				return false;
			}
		}
		catch ( RemoteException e )
		{
			throw new IllegalStateException( "Can't start remote neo shell: " +
				e );
		}
	}
	
	private boolean shellDependencyAvailable()
	{
		try
		{
			Class.forName( "org.neo4j.util.shell.ShellServer" );
			return true;
		}
		catch ( Throwable t )
		{
			return false;
		}
	}

	public void registerEnumRelationshipTypes( 
		Class<? extends RelationshipType> relationshipTypes )
	{
		NeoJvmInstance.addEnumRelationshipTypes( relationshipTypes );
	}

    public Iterable<RelationshipType> getRelationshipTypes()
    {
    	return NeoJvmInstance.getRelationshipTypes();
    }
    
    public RelationshipType getRelationshipType( String name )
    {
		return NeoJvmInstance.getRelationshipTypeByName( name );
    }
    
    public boolean hasRelationshipType( String name )
    {
    	return NeoJvmInstance.hasRelationshipType( name );
    	
    }
     
    public RelationshipType createAndRegisterRelationshipType( String name )
    {
    	return NeoJvmInstance.registerRelationshipType( name, true );
    }
    
    // must exist in store
    public RelationshipType registerRelationshipType( String name )
    {
    	return NeoJvmInstance.registerRelationshipType( name, false );
    }
    
    public void registerRelationshipTypes( Iterable<RelationshipType> types )
    {
    	for ( RelationshipType type : types )
    	{
    		NeoJvmInstance.registerRelationshipType( type.name(), true );
    	}
    }
    
    public void registerRelationshipTypes( RelationshipType[] types )
    {
       	for ( RelationshipType type : types )
    	{
    		NeoJvmInstance.registerRelationshipType( type.name(), true );
    	}
    }
}
