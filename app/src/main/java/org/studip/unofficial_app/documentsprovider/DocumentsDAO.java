package org.studip.unofficial_app.documentsprovider;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import org.studip.unofficial_app.model.room.BasicDao;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface DocumentsDAO extends BasicDao<DocumentRoot>
{
    @Query("SELECT * FROM roots")
    DocumentRoot[] getRoots();
    
    @Query("SELECT * FROM roots")
    LiveData<DocumentRoot[]> observeRoots();
    
    @Query("SELECT * FROM roots WHERE parentID = :parent")
    Single<DocumentRoot[]> isInRoots(String parent);
    
    
}
