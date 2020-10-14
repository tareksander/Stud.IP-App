package com.studip;

import com.google.gson.Gson;
import com.studip.api.API;
import com.studip.api.User;

import org.jsoup.Jsoup;

public class Data
{
    public static API api;
    public static User user;
    public static Settings settings; // TODO initialize before API in every onCreate
    public static Gson gson;
    public static Jsoup jsoup; // TODO initialize in onCreate of HomeActivity
    
}
