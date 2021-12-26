package io.schark.pony.core.feat.commands.registry.wrapper;

import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;

import java.lang.annotation.Annotation;

/**
 * @author Player_Schark
 */
public record WrapperType<A extends Annotation>(Class<? extends Annotation> annoClass) {
	public static final WrapperType<AllowedRoles> ROLE = new WrapperType<>(AllowedRoles.class);
	public static final WrapperType<AllowedUsers> USER = new WrapperType<>(AllowedUsers.class);
	public static final WrapperType<AllowedGuilds> GUILD = new WrapperType<>(AllowedGuilds.class);
	public static final WrapperType<AllowedChannels> CHANNEL = new WrapperType<>(AllowedChannels.class);
}
