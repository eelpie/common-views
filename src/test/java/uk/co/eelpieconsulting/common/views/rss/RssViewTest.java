package uk.co.eelpieconsulting.common.views.rss;

import org.junit.Test;
import uk.co.eelpieconsulting.common.views.EtagGenerator;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class RssViewTest {

    @Test
    public void canRenderRssView() {
        RssView rssView = new RssView(new EtagGenerator(), "A feed", "http://localhost/feed", "Just another feed");

        String rendered = rssView.renderRss(new ArrayList());

        assertTrue(rendered.contains("<title>A feed</title>"));
    }

}
