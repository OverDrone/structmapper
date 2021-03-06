package org.structmapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
//@Retention(RetentionPolicy.SOURCE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetRef {
    String value() default "";
}
