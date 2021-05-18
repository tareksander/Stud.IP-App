package org.studip.unofficial_app.api.plugins.meetings;

import retrofit2.Retrofit;

public class Meetings
{
    public final MeetingsRoutes routes;
    public Meetings(Retrofit r) {
        routes = r.create(MeetingsRoutes.class);
    }
}
