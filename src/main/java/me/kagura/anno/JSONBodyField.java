package me.kagura.anno;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JSONBodyField {

    String value() default "";

    boolean required() default true;

}
