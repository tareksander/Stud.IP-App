package org.studip.unofficial_app.api.plugins.courseware;

import android.net.Uri;

import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewareHTMLBlock;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewareOpencastBlock;
import org.studip.unofficial_app.api.plugins.courseware.blocks.CoursewarePDFBlock;
import org.studip.unofficial_app.model.GsonProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import retrofit2.Retrofit;

public class Courseware
{
    // use Html.fromHtml() for html blocks
    
    private final CoursewareRoutes routes;
    public Courseware(Retrofit r) {
        this.routes = r.create(CoursewareRoutes.class);
    }
    
    
    public Single<CoursewareChapter[]> getChapters(String cid) {
        return routes.getCourseware(cid, null).map(s -> {
            Document d = Jsoup.parse(s);
            Elements chapters = d.getElementsByAttributeValue("data-type", "Chapter");
            CoursewareChapter[] c = new CoursewareChapter[chapters.size()];
            for (int i = 0;i<c.length;i++) {
                Element chapter = chapters.get(i);
                c[i] = new CoursewareChapter();
                c[i].name = chapter.attr("data-title");
                c[i].id = chapter.attr("data-blockid");
            }
            return c;
        });
    }
    
    public Single<CoursewareSubchapter[]> getSubchapters(String cid, String chapter) {
        return routes.getCourseware(cid, chapter).map(s -> {
            Document d = Jsoup.parse(s);
            Elements subchapters = d.getElementsByAttributeValue("data-type", "Subchapter");
            CoursewareSubchapter[] c = new CoursewareSubchapter[subchapters.size()];
            for (int i = 0;i<c.length;i++) {
                Element subchapter = subchapters.get(i);
                c[i] = new CoursewareSubchapter();
                c[i].name = subchapter.attr("data-title");
                c[i].id = subchapter.attr("data-blockid");
            }
            return c;
        });
    }
    
    
    public Single<CoursewareSection[]> getSections(String cid, String subchapter) {
        return routes.getCourseware(cid, subchapter).map(s -> {
            Document d = Jsoup.parse(s);
            Elements subchapters = d.getElementsByAttributeValue("class","active-subchapter \n    \n    ");
            if (subchapters.size() == 0) {
                throw new RuntimeException("Could not find active subchapter");
            }
            //System.out.println(subchapters.get(0).toString());
            Elements sections = subchapters.get(0).getElementsByAttributeValue("data-type", "Section");
            CoursewareSection[] c = new CoursewareSection[sections.size()];
            for (int i = 0;i<c.length;i++) {
                Element section = sections.get(i);
                //System.out.println(section.toString());
                c[i] = new CoursewareSection();
                Elements title = section.getElementsByAttributeValue("class", "navigate");
                if (title.size() == 0) {
                    throw new RuntimeException("Could not find courseware section title");
                }
                c[i].name = title.get(0).attr("data-title");
                c[i].id = section.attr("data-blockid");
            }
            return c;
        });
    }
    
    public Single<CoursewareBlock[]> getBlocks(String cid, String section) {
        return routes.getCourseware(cid, section).map(s -> {
            Document d = Jsoup.parse(s);
            Elements blocks = d.getElementsByAttributeValueContaining("data-blocktype", "Block");
            CoursewareBlock[] c = new CoursewareBlock[blocks.size()];
            for (int i = 0;i<c.length;i++) {
                Element block = blocks.get(i);
                String type = block.attr("data-blocktype");
                if ("HtmlBlock".equals(type)) {
                    c[i] = new CoursewareHTMLBlock(block.html());
                } else if ("OpenCastBlock".equals(type)) {
                    Elements opencast = block.getElementsByAttributeValue("class", "courseware-oc-video");
                    if (opencast.size() == 0) {
                        throw new RuntimeException("Could not load Courseware Opencast block");
                    }
                    Element o = opencast.get(0);
                    String url = o.attr("data-src");
                    //System.out.println(url);
                    Uri uri = Uri.parse(url);
                    JsonObject ltidata;
                    Elements script = block.getElementsByTag("script");
                    if (script.size() == 0) {
                        throw new RuntimeException("Could not load ltidata for Courseware Opencast block");
                    }
                    Pattern p = Pattern.compile(".*OC_LTI_DATA {3}= {2}(\\{.*\\});.*", Pattern.DOTALL);
                    Matcher m = p.matcher(script.html());
                    if (! m.matches()) {
                        throw new RuntimeException("Could not find ltidata for Courseware Opencast block");
                    }
                    //System.out.println(m.group(1));
                    ltidata = GsonProvider.getGson().fromJson(m.group(1), JsonObject.class);
                    c[i] = new CoursewareOpencastBlock(uri.getHost(), uri.getQueryParameter("id"), ltidata);
                } else if ("PdfBlock".equals(type)) {
                    Elements file = block.getElementsByAttributeValueContaining("class","cw-pdf-file-url");
                    if (file.size() == 0) {
                        throw new RuntimeException("Could not load Courseware Opencast block");
                    }
                    Element f = file.get(0);
                    Uri uri = Uri.parse(f.attr("value"));
                    c[i] = new CoursewarePDFBlock(f.attr("value"),uri.getQueryParameter("file_name"));
                } else {
                    System.out.println("Unknown Courseware block type: "+type);
                    c[i] = null;
                }
            }
            return c;
        });
    }
    
    
    
}
