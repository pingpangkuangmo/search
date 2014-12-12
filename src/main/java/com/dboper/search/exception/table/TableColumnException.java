package com.dboper.search.exception.table;

public class TableColumnException extends RuntimeException{

	private static final long serialVersionUID = -4543289306025169931L;

	public TableColumnException(String paramString) {
		super(paramString);
	} 
	
	public TableColumnException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}
}
