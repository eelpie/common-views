package uk.co.eelpieconsulting.common.views.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.SimpleModuleImpl;
import com.rometools.modules.georss.geometries.Position;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.utils.Lists;
import org.junit.Test;
import uk.co.eelpieconsulting.common.views.EtagGenerator;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class RssViewTest {

    @Test
    public void canRenderRssView() {
        RssView rssView = new RssView(new EtagGenerator(), "A feed", "http://localhost/feed", "Just another feed");

        SyndEntryImpl entry = new SyndEntryImpl();
        entry.setTitle("An item");
        entry.setLink("http://localhost/an-item");
        entry.setPublishedDate(new Date());

        String rendered = rssView.renderRss(Arrays.asList(entry));

        assertTrue(rendered.contains("<title>A feed</title>"));
        assertTrue(rendered.contains("<title>An item</title>"));
    }

    @Test
    public void canRenderRssItemCategories() {
        RssView rssView = new RssView(new EtagGenerator(), "A feed", "http://localhost/feed", "Just another feed");

        SyndEntryImpl entry = new SyndEntryImpl();
        entry.setTitle("An item");
        entry.setLink("http://localhost/an-item");
        entry.setPublishedDate(new Date());

        SyndCategory news = new SyndCategoryImpl();
        news.setName("News");
        List categories = Lists.create(news);
        entry.setCategories(categories);

        String rendered = rssView.renderRss(Arrays.asList(entry));

        assertTrue(rendered.contains("<category>News</category>"));
    }

    @Test
    public void canRenderGeoRSSItems() {
        RssView rssView = new RssView(new EtagGenerator(), "A feed", "http://localhost/feed", "Just another feed");

        SyndEntryImpl entry = new SyndEntryImpl();
        entry.setTitle("A geo item");
        entry.setLink("http://localhost/a-geo-item");
        entry.setPublishedDate(new Date());

        final GeoRSSModule geoRSSModule = new SimpleModuleImpl();
        Position position = new Position(51.1, -0.3);
        geoRSSModule.setPosition(position);
        geoRSSModule.setFeatureNameTag("Somewhere");
        entry.getModules().add(geoRSSModule);

        String rendered = rssView.renderRss(Arrays.asList(entry));

        assertTrue(rendered.contains("<title>A feed</title>"));
        assertTrue(rendered.contains("<georss:point>51.1 -0.3</georss:point>"));
        assertTrue(rendered.contains("<georss:featurename>Somewhere</georss:featurename>"));
    }

}
