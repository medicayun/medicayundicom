package org.dcm4chex.arr.ejb.session;

import java.lang.Exception;

final class ArrInputException extends Exception
{
	public ArrInputException() { super(); }
	public ArrInputException(String desc) { super(desc); }
	public ArrInputException(String desc, Throwable cause) { super(desc, cause); }
}
