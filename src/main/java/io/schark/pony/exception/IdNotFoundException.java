package io.schark.pony.exception;

/**
 * @author Player_Schark
 */
public class IdNotFoundException extends RuntimeException{

	public IdNotFoundException(long id, String type) {
		super("Id '" + id + "' of type '" + type + "' not found.");
	}
}
