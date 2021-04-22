package com.studip_old.api;

public class RouteInactiveException extends Exception
{

    public RouteInactiveException(String responseMessage)
    {
        super(responseMessage);
    }
    public RouteInactiveException() {}
}
