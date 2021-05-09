package org.studip.unofficial_app.api.plugins.opencast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.reactivex.Single;
import retrofit2.Retrofit;

public class Opencast
{
    
    
    
    private final OpencastRoutes routes;
    public Opencast(Retrofit retrofit) {
        routes = retrofit.create(OpencastRoutes.class);
        
        
        
        
        
    }
    
    
    public Single<OpencastVideo[]> getOpencast(String course) {
        return routes.getOpencastPage(course).map(s -> {
            //System.out.println("mapping");
            Document d = Jsoup.parse(s);
            if (d == null) {
                throw new Exception("could not parse OpenCast website");
            }
            Elements videos = d.getElementsByAttributeValue("class","oce_item");
            Elements downloads = d.getElementsByAttributeValueContaining("id","download_dialog");
            if (videos.size() == 0 || videos.size() != downloads.size()) {
                throw new Exception("no matching elements found");
            }
            OpencastVideo[] result = new OpencastVideo[videos.size()];
            for (Element v : videos) {
                int pos = Integer.parseInt(v.attr("data-pos"));
                result[pos] = new OpencastVideo();
                Elements prev = v.getElementsByAttributeValue("class","previewimage");
                if (prev.size() == 2) {
                    result[pos].preview_url = prev.get(1).attr("data-src");
                } else {
                    throw new Exception("could not find preview url");
                }
                Elements prevcontainer = v.getElementsByAttributeValue("class","oce_playercontainer");
                if (prevcontainer.size() != 0) {
                    Elements children = prevcontainer.get(0).children();
                    if (children.size() != 0) {
                        result[pos].watch_opencast = children.get(0).attr("href");
                    } else {
                        throw new Exception("could not find preview container");
                    }
                } else {
                    throw new Exception("could not find preview container");
                }
                Elements title = v.getElementsByAttributeValue("class","oce_metadata oce_list_title");
                if (title.size() != 0) {
                    result[pos].title = title.get(0).text();
                } else {
                    throw new Exception("could not find title");
                }
                Elements metadata = v.getElementsByAttributeValue("class","oce_contentlist");
                if (metadata.size() != 0) {
                    Element mt = metadata.get(0);
                    Elements children = mt.children();
                    if (children.size() == 3) {
                        result[pos].date = children.get(0).text();
                        result[pos].author = children.get(1).text();
                        result[pos].description = children.get(2).text();
                    } else {
                        throw new Exception("could not find metadata");
                    }
                } else {
                    throw new Exception("could not find metadata");
                }
                Elements download_qualities = v.getElementsByAttributeValue("class","download presentation button");
                result[pos].versions = new OpencastVideo.VideoVersion[download_qualities.size()];
                for (int i = 0;i<result[pos].versions.length;i++) {
                    result[pos].versions[i] = new OpencastVideo.VideoVersion();
                    Element q = download_qualities.get(i);
                    result[pos].versions[i].resolution = q.wholeText();
                    result[pos].versions[i].download = q.attr("href");
                }
            }
            return result;
        });
    }
    
    
    
}
