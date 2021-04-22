package com.studip_old;import org.studip.unofficial_app.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.studip_old.api.rest.StudipNews;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
public class NewsAdapter extends ArrayAdapter
{
    StudipNews[] news;
    public NewsAdapter(@NonNull Context context, int resource)
    {
        super(context, resource);
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View v;
        if (convertView != null)
        {
            v = convertView;
        }
        else
        {
            v = LayoutInflater.from(getContext()).inflate(R.layout.course_news_entry,parent,false);
        }
        TextView title = v.findViewById(R.id.news_title);
        TextView content = v.findViewById(R.id.news_content);
        title.setText(news[position].topic);
        Document d = Jsoup.parse(news[position].body_html);
        content.setText(d.wholeText());
        return v;
    }
    @Override
    public int getCount()
    {
        if (news == null)
        {
            return 0;
        }
        return news.length;
    }
}
