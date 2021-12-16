package io.schark.pony.core.feat.commands.annotation.access;

import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.annotation.impl.PonyFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Player_Schark
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedRoles {

	long[] ids() default {};
	Class<? extends PonyAccessor> accessor() default PonyAccessor.class;
	String noAccessMessage() default "";
	Class<? extends PonyFunction> noAccessFunction() default PonyFunction.class;
}
