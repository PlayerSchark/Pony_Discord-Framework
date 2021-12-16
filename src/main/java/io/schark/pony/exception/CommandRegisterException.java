package io.schark.pony.exception;

/**
 * @author Player_Schark
 */
public class CommandRegisterException extends RuntimeException {

	public CommandRegisterException(Exception exception) {
		super();
		exception.printStackTrace();
	}

}
