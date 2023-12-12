package cmd.builtin;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {TYPE})
public @interface Option {

    String shortName() default "";

    String name() default "";

    String description() default "";

    boolean required() default false;

    boolean unique() default false;

}
