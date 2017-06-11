package org.structmapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Сигнализирует о том, что данное свойство инициализируется конструктором
 * Т.е. не нужно его инициализировать через конструктор или сеттер 
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
//@Retention(RetentionPolicy.SOURCE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Initialized {
}
