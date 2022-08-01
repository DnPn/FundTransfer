package io.dnpn.fundtransfer.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to document that the visibility of a given field is not as strict as it could be so it is
 * accessible to the tests.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface VisibleForTesting {
}
