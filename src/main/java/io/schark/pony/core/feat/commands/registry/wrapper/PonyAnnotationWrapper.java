package io.schark.pony.core.feat.commands.registry.wrapper;

import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Player_Schark
 */
public class PonyAnnotationWrapper {

	private Map<WrapperType<?>, PonyAccessWrapper<?>> wrappers = new HashMap<>();

	public PonyAnnotationWrapper(PonyAccessWrapper<AllowedRoles> role, PonyAccessWrapper<AllowedUsers> user, PonyAccessWrapper<AllowedGuilds> guild, PonyAccessWrapper<AllowedChannels> channel) {
		this.wrappers.put(WrapperType.ROLE, role);
		this.wrappers.put(WrapperType.USER, user);
		this.wrappers.put(WrapperType.GUILD, guild);
		this.wrappers.put(WrapperType.CHANNEL, channel);
	}

	public <A extends Annotation> PonyAccessWrapper<A> getAccessWrapper(WrapperType<A> type) {
		//noinspection unchecked
		return (PonyAccessWrapper<A>) this.wrappers.get(type);
	}

	public <A extends Annotation> void setAccessWrapper(WrapperType<A> type, PonyAccessWrapper<A> wrapper) {
		this.wrappers.put(type, wrapper);
	}

}
