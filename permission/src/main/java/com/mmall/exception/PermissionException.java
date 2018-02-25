package com.mmall.exception;

import org.springframework.stereotype.Component;

/**
 * 自定义异常
 * @author Administrator
 *
 */
public class PermissionException extends RuntimeException{

	public PermissionException() {
		super();
	}

	public PermissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermissionException(String message) {
		super(message);
	}

	public PermissionException(Throwable cause) {
		super(cause);
	}

}
