package io.schark.pony.core.feat.commands.registry.wrapper;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Player_Schark
 */
public record PonyAccessWrapper<A extends Annotation>(Class<A> annoClass, Set<Long> accessIds, PonyRunnable noAccess, String noAccessMessage) {

}
