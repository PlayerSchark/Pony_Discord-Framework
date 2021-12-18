package io.schark.pony.core.feat.commands.annotation;

import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Player_Schark
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuildCommand {

	long[] guildIds();
	Class<? extends PonyAccessor> accessor() default PonyAccessor.class;
}
