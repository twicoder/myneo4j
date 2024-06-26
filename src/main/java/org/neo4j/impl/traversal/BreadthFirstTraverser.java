package org.neo4j.impl.traversal;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;

/**
 * A traverser that traverses the node space breadth-first. Breadth-first
 * means that it visits all the end-nodes of the current node's relationships
 * before it visits the end-nodes of those nodes' relationships. This class
 * is package private: any documentation interesting to a client programmer
 * should reside in {@link Traverser}. For some implementation documentation,
 * see {@link AbstractTraverser}.
 *
 * @see Traverser
 * @see AbstractTraverser
 * @see DepthFirstTraverser
 */
// TODO: document reason for initializeList() and the declaration of 'nodeQueue'
class BreadthFirstTraverser extends AbstractTraverser
{
	private java.util.LinkedList<TraversalPositionImpl> nodeQueue;
	
	/**
	 * Creates a BreadthFirstTraverser according the contract of
	 * {@link AbstractTraverser#AbstractTraverser AbstractTraverser}.
	 */
	BreadthFirstTraverser
						(
							Node startNode,
							RelationshipType[] traversableRels,
							Direction[] traversableDirections,
							RelationshipType[] preservingRels,
							Direction[] preservingDirections,
							StopEvaluator stopEvaluator,
							ReturnableEvaluator returnableEvaluator, 
							RandomEvaluator randomEvaluator
						)
	{
		super( startNode, traversableRels, traversableDirections,
			   preservingRels, preservingDirections, stopEvaluator,
			   returnableEvaluator, randomEvaluator );
	}
	
	void addPositionToList( TraversalPositionImpl position )
	{
		this.nodeQueue.addLast( position );
	}
	
	TraversalPositionImpl getNextPositionFromList()
	{
		return this.nodeQueue.removeFirst();
	}
	
	boolean listIsEmpty()
	{
		return this.nodeQueue.isEmpty();
	}
	
	final boolean traverseChildrenInNaturalOrder()
	{
		return true;
	}
	
	void initializeList()
	{
		this.nodeQueue = new java.util.LinkedList<TraversalPositionImpl>();
	}
}
