package com.act.casemanagement.domain.exception;

import java.util.UUID;

/** Thrown when a requested aggregate does not exist. Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, UUID id) {
        super(resourceType + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceType, String id) {
        super(resourceType + " not found with id: " + id);
    }
}
