package com.studip.api;

public class RouteInactiveException extends Exception
{

    public RouteInactiveException(String responseMessage)
    {
        super(responseMessage);
    }
    public RouteInactiveException() {}
}
