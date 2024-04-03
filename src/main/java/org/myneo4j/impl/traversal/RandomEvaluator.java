package org.myneo4j.impl.traversal;

import org.myneo4j.api.core.RelationshipType;

public interface RandomEvaluator
{
    public boolean shouldRandomize( TraversalPositionImpl position,
                                    RelationshipType type );
}
