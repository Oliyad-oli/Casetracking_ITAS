package com.act.casemanagement.observability.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a use-case method for START / SUCCESS / FAILURE audit wrapping
 * by AuditInterceptor. Place on the public execute() method of each use case.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /** Human-readable action label — recorded in the audit log. */
    String action();
}
