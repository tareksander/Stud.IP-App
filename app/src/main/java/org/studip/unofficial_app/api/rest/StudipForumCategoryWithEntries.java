package org.studip.unofficial_app.api.rest;
import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;
public class StudipForumCategoryWithEntries
{
    @Embedded
    public StudipForumCategory category;
    @Relation(
        parentColumn = "category_id",
        entityColumn = "parent_id"
    )
    public List<StudipForumEntry> children;
}
