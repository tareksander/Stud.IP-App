package org.studip.unofficial_app.api.rest;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;
public class StudipForumEntryWithChildren implements Serializable
{
    @Embedded
    public StudipForumEntry entry;
    @Relation(
            parentColumn = "topic_id",
            entityColumn = "parent_id"
    )
    public List<StudipForumEntry> children;
}
