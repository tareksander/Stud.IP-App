package org.studip.unofficial_app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipNews;

public class NewsAdapter extends ArrayAdapter
{
    private StudipNews[] news;
    public void setNews(StudipNews[] news) {
        this.news = news;
        notifyDataSetChanged();
    }
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
        content.setText(HelpActivity.fromHTML(news[position].body_html, false, null));
        title.setTextIsSelectable(true);
        content.setTextIsSelectable(true);
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
