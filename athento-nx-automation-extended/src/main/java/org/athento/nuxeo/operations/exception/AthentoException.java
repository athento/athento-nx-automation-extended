package org.athento.nuxeo.operations.exception;

import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.server.jaxrs.RestOperationException;

public class AthentoException extends RestOperationException {


	public AthentoException(String message, Throwable cause) {
		super(message, cause);
		AthentoException.setEmptyStackTrace(this);
	}

	public AthentoException(String message) {
		super(message, null);
		AthentoException.setEmptyStackTrace(this);
	}

	public AthentoException(Throwable cause) {
		super(cause);
		AthentoException.setEmptyStackTrace(cause);
	}

	public static void setEmptyStackTrace(Throwable e) {
		if (e != null) {
			Throwable cause = AthentoOperationsHelper.getRootCause(e);
			e.setStackTrace(AthentoException.EMPTY_TRACE);
			if (!e.equals(cause)) {
				AthentoException.setEmptyStackTrace(cause);
			}
		}
	}

	private static final StackTraceElement[] EMPTY_TRACE = new StackTraceElement[] {};

	private static final long serialVersionUID = -8048219282223604748L;
}
