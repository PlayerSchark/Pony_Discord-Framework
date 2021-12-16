package io.schark.pny.example;
import lombok.Getter;

/**
 * @author Player_Schark
 */
public enum MyRoleEnum {

	ADMIN(125),
	MOD(124);

	@Getter private final long id;

	MyRoleEnum(int id) {
		this.id = id;
	}
}
