package org.neo4j.impl.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.neo4j.impl.event.Event;
import org.neo4j.impl.event.EventData;
import org.neo4j.impl.event.EventManager;


public class UserTransactionImpl implements UserTransaction
{
	private static UserTransactionImpl instance = new UserTransactionImpl();

	private final TransactionManager tm;
	
	public static UserTransactionImpl getInstance()
	{
		return instance;
	}

	private UserTransactionImpl()
	{
		tm = TransactionFactory.getTransactionManager();
	}
	
	public void begin() throws NotSupportedException, SystemException
	{
		tm.begin();
		// Neo generates TX_BEGIN, TX_ROLLBACK and TX_COMMIT events on
		// transaction begin, rollback and commit, respectively. TX_BEGIN is
		// generated here in UserTransaction.begin(), while ROLLBACK and COMMIT
		// are generated by a synchronization hook (TxEventGenerator) registered
		// below.
		try
		{
			TransactionImpl tx = ( TransactionImpl ) tm.getTransaction();
			EventManager.getManager().generateReActiveEvent(
				Event.TX_BEGIN, new EventData( tx.getEventIdentifier() ) );
			tx.registerSynchronization( TxEventGenerator.getInstance() );
		}
		catch ( Exception e )
		{
			throw new SystemException( "Unable to generate tx events:" + e );
		}
	}

	public void commit() throws RollbackException, HeuristicMixedException,
				 HeuristicRollbackException, SecurityException,
				 IllegalStateException, SystemException
	{
		tm.commit();
	}

	public void rollback() throws SecurityException, IllegalStateException,
				 SystemException
	{
		tm.rollback();
	}

	public void setRollbackOnly() throws IllegalStateException, SystemException
	{
		tm.setRollbackOnly();
	}

	public int getStatus() throws SystemException
	{
		return tm.getStatus();
	}

	public void setTransactionTimeout(int seconds) throws SystemException
	{
		tm.setTransactionTimeout(seconds);
	}
	
	/**
	 * Returns the event identifier for the current transaction. If no 
	 * transaction is active <CODE>null</CODE> is returned.
	 */
	public Integer getEventIdentifier()
	{
		try
		{
			TransactionImpl tx = ( TransactionImpl ) tm.getTransaction();
			if ( tx != null )
			{
				return tx.getEventIdentifier();
			}
		}
		catch ( SystemException e )
		{
		}
		return null;
	}
}
