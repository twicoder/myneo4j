package org.neo4j.impl.nioneo.store;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Wrapps a <CODE>ByteBuffer</CODE> and is tied to a {@link PersistenceWindow}. 
 * Using the {@link #setOffset(int)} method one can offset the buffer (within 
 * the limits of the persistence window.
 * <p>
 * All the <CODE>put</CODE> and <CODE>get</CODE> methos of this class works
 * the same way as in <CODE>ByteBuffer</CODE>.
 *  
 *  @see ByteBuffer, PersistenceWindow
 */
public class Buffer
{
	private static Logger logger = Logger.getLogger( Buffer.class.getName() );
	
	private ByteBuffer buf = null;
	private PersistenceWindow persistenceWindow = null;
	
	Buffer( PersistenceWindow persistenceWindow )
	{
		this.persistenceWindow = persistenceWindow;
	}
	
	void setByteBuffer( ByteBuffer byteBuffer )
	{
		this.buf = byteBuffer;
	}
	
	/**
	 * Returns the position of the persistence window tied to this buffer.
	 *  
	 * @return The persistence window's position
	 */
	public int position()
	{
		return persistenceWindow.position();
	}
	
	/**
	 * Returnes the underlying byte buffer.
	 * 
	 * @return The byte buffer wrapped by this buffer
	 */
	public ByteBuffer getBuffer()
	{
		return buf;
	}
	
	/**
	 * Sets the offset from persistence window position in the underlying
	 * byte buffer.
	 * 
	 * @param offset The new offset to set 
	 * @return This buffer
	 */
	public Buffer setOffset( int offset )
	{
		try
		{
			buf.position( offset );
		}
		catch ( java.lang.IllegalArgumentException e )
		{
			logger.severe( "Illegal buffer position: Pos=" + position() + 
				" off=" + offset + " capacity=" + buf.capacity() );
			throw e;
		}
		return this;
	}

	/**
	 * Returns the offset of this buffer.
	 * 
	 * @return The offset
	 */
	public int getOffset()
	{
		return buf.position();
	}
	
	/**
	 * Puts a <CODE>byte</CODE> into the underlying buffer.
	 * 
	 * @param b The <CODE>byte</CODE> that will be written
	 * @return This buffer
	 */
	public Buffer put( byte b )
	{
		buf.put( b );
		return this;
	}

	/**
	 * Puts a <CODE>int</CODE> into the underlying buffer.
	 * 
	 * @param i The <CODE>int</CODE> that will be written
	 * @return This buffer
	 */
	public Buffer putInt( int i )
	{
		buf.putInt( i );
		return this;
	}
	
	/**
	 * Puts a <CODE>long</CODE> into the underlying buffer.
	 * 
	 * @param l The <CODE>long</CODE> that will be written
	 * @return This buffer
	 */
	public Buffer putLong( long l )
	{
		buf.putLong( l );
		return this;
	}
	
	/**
	 * Reads and returns a <CODE>byte</CODE> from the underlying buffer.
	 * 
	 * @return The <CODE>byte</CODE> value at the current position/offset
	 */
	public byte get()
	{
		return buf.get();
	}
	
	/**
	 * Reads and returns a <CODE>int</CODE> from the underlying buffer.
	 * 
	 * @return The <CODE>int</CODE> value at the current position/offset
	 */
	public int getInt()
	{
		return buf.getInt();
	}
	
	/**
	 * Reads and returns a <CODE>long</CODE> from the underlying buffer.
	 * 
	 * @return The <CODE>long</CODE> value at the current position/offset
	 */
	public long getLong()
	{
		return buf.getLong();
	}
	
	/**
	 * Puts a <CODE>byte array</CODE> into the underlying buffer.
	 *  
	 * @param src The <CODE>byte array</CODE> that will be written
	 * @return This buffer
	 */
	public Buffer put( byte src[] )
	{
		buf.put( src );
		return this;
	}

	/**
	 * Puts a <CODE>byte array</CODE> into the underlying buffer starting
	 * from <CODE>offset</CODE> in the array and writing <CODE>length</CODE>
	 * values.
	 * 
	 * @param src The <CODE>byte array</CODE> to write values from
	 * @param offset The offset in the <CODE>byte array</CODE>
	 * @param length The number of bytes to write
	 * @return This buffer
	 */
	public Buffer put( byte src[], int offset, int length )
	{
		buf.put( src, offset, length );
		return this;
	}
	
	/**
	 * Reads <CODE>byte array length</CODE> bytes into the 
	 * <CODE>byte array</CODE> from the underlying buffer.
	 *  
	 * @param dst The byte array to read values into
	 * @return This buffer
	 */
	public Buffer get( byte dst[] )
	{
		buf.get( dst );
		return this;
	}
}
