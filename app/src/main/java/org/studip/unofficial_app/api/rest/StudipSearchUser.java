package org.studip.unofficial_app.api.rest;
import java.io.Serializable;

// from route dispatch.php/multipersonsearch/ajax_search/add_adressees?s=
public class StudipSearchUser implements Serializable
{
    public String user_id;
    public String avatar;
    public String text; // the name, permission and username
    public boolean member;
}
