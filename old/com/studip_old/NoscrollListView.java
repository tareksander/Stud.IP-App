package com.studip_old;import org.studip.unofficial_app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
public class NoscrollListView extends ListView
{
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
    public NoscrollListView(Context context)
    {
        super(context);
    }

    public NoscrollListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public NoscrollListView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public NoscrollListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
