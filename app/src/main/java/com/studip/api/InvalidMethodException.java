package com.studip.api;

public class InvalidMethodException extends Exception
{

    public InvalidMethodException(String responseMessage)
    {
        super(responseMessage);
    }
    public InvalidMethodException() {}
}
