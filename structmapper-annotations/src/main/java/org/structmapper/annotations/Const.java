package org.structmapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
//@Retention(RetentionPolicy.SOURCE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Const {
    String name();
    boolean[] bool() default {};
    byte[] b() default {};
    char[] c() default {};
    short[] sh() default {};
    int[] i() default {};
    long[] l() default {};
    float[] f() default {};
    double[] d() default {};
    String[] s() default {};
    Class<?>[] cl() default {};
    String[] member() default {};
}
