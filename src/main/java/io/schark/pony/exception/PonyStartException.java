package io.schark.pony.exception;

/**
 * @author Player_Schark
 */
public class PonyStartException extends Exception {


	public PonyStartException(Exception e, String message) {
		super(message);
		e.printStackTrace();
	}

	public PonyStartException(String message) {
		super(message);
	}
}
