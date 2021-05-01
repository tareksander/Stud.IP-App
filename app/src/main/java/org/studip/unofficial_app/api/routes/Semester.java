package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipSemester;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Semester
{
    @GET("api.php/semesters")
    Call<StudipCollection<StudipSemester>> semesters(@Query("offset") int offset, @Query("limit") int limit);

    @GET("api.php/semester/{sid}")
    Call<StudipSemester> semester(@Path("sid") String id);
}
