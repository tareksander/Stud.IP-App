package com.studip_old.api;

public class InvalidMethodException extends Exception
{

    public InvalidMethodException(String responseMessage)
    {
        super(responseMessage);
    }
    public InvalidMethodException() {}
}
