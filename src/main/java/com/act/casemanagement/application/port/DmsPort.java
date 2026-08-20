package com.act.casemanagement.application.port;

import java.util.UUID;

/**
 * Document Management System port.
 * If a platform-wide DMS service is confirmed, the adapter calls it the same
 * way bs-filing-core-server uses its DmsPort for filing certificates.
 * Until confirmed, the adapter is a clearly-marked stub.
 */
public interface DmsPort {

    /**
     * Stores a document and returns a DMS reference ID.
     *
     * @param caseId       owning case
     * @param fileName     original file name
     * @param contentType  MIME type
     * @param content      raw bytes
     * @param correlationId trace id
     * @return DMS reference id
     */
    String store(UUID caseId, String fileName, String contentType,
                 byte[] content, UUID correlationId);

    /**
     * Retrieves document bytes by DMS reference.
     */
    byte[] retrieve(String dmsReference, UUID correlationId);
}
