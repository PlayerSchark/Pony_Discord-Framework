package io.schark.pony.feat.commands.annotation;

import java.lang.annotation.*;

/**
 * @author Player_Schark
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {

	String[] aliases();
}
