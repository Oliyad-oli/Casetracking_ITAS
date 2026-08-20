package com.act.casemanagement.domain.exception;

import java.util.UUID;

/**
 * Thrown when an actor attempts a record-level operation they are not
 * authorised to perform (e.g. closing a case they did not assign, editing
 * another officer's note). Maps to HTTP 403.
 *
 * This is a domain-level exception — no Spring Security construct is used.
 * The authorisation check lives in the use case layer using the trusted
 * actor identity from RequestActorContext (Section 5.2).
 */
public class ForbiddenOperationException extends DomainException {

    private final UUID actorId;
    private final String operation;
    private final UUID resourceId;

    public ForbiddenOperationException(UUID actorId, String operation, UUID resourceId) {
        super("Actor " + actorId + " is not authorised to perform [" + operation
              + "] on resource " + resourceId);
        this.actorId   = actorId;
        this.operation = operation;
        this.resourceId = resourceId;
    }

    public UUID getActorId()    { return actorId; }
    public String getOperation(){ return operation; }
    public UUID getResourceId() { return resourceId; }
}
