package com.studip.api;

public class AuthorisationException extends Exception
{

    public AuthorisationException(String responseMessage)
    {
        super(responseMessage);
    }
    public AuthorisationException() {}
}
