package org.studip.unofficial_app.api.rest;

import androidx.room.Embedded;
import androidx.room.Relation;

public class StudipCourseMemberWithUser
{
    @Embedded
    public StudipCourseMember member;
    @Relation(
            parentColumn = "id",
            entityColumn = "user_id"
    )
    public StudipUser user;
}
