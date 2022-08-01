package io.dnpn.fundtransfer.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to exclude a method/class from the Jacoco report.
 * <p>
 * Use this method with parsimony, only when excluding a method makes sense to reduce noise in the report and use the
 * field `reason` to justify why a given class/method should be excluded from the coverage report.
 * <p>
 * It relies on the fact that Jacoco excludes methods/classes annotated with an annotation containing the word
 * `Generated`. Use this method with parsimony, only when excluding a method makes
 * sense to reduce noise in the report.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExcludeFromJacocoGeneratedReport {
    String reason();
}
