package org.athento.nuxeo.operations.exception;

public class AthentoException extends Exception {
	private String message;
	private String code;
	
	
	
	public AthentoException(String message, String code) {
		super();
		this.message = message;
		this.code = code;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
