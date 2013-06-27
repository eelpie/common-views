package uk.co.eelpieconsulting.common.views.rss;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import uk.co.eelpieconsulting.common.views.EtagGenerator;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;


public class RssView implements View {

	private final EtagGenerator etagGenerator;
	private final String link;
	private final String title;
	private final String description;
	
	private Integer maxAge;
	
	public RssView(EtagGenerator etagGenerator, String title, String link, String description) {
		this.etagGenerator = etagGenerator;
		this.title = title;
		this.link = link;
		this.description = description;
	}
	
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
	
	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {        
    	response.setContentType("text/xml;charset=UTF-8");
    	response.setCharacterEncoding("UTF-8");
    	response.setHeader("Cache-Control", "max-age=" + (maxAge != null ? maxAge : 0));		
    	
    	final List<RssFeedable> contentItems = (List<RssFeedable>) model.get("data");
    	final String rssContent = renderRss(title, link, description, makeRssEntiresForContent(contentItems));
		response.setHeader("Etag", etagGenerator.makeEtagFor(rssContent));
		
		response.getWriter().print(rssContent);
		response.getWriter().flush();
	}
	
	@Override
	public String getContentType() {
		return "text/xml";
	}
	
	protected String renderRss(String title, String link, String description, List entries) {
		SyndFeed feed = new SyndFeedImpl();

		feed.setTitle(title);
		feed.setFeedType("rss_2.0");
		feed.setLink(link);
		feed.setDescription(description);
		feed.setEncoding("UTF-8");
		feed.setEntries(entries);

		StringWriter writer = new StringWriter();
		SyndFeedOutput output = new SyndFeedOutput();

		try {
			output.output(feed, writer);
			return writer.toString();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (FeedException e) {
			throw new RuntimeException(e);
		}
	}
    
	private List<SyndEntry> makeRssEntiresForContent(List<RssFeedable> contentItems) {
		List<SyndEntry> entires = new ArrayList<SyndEntry>();
    	for (RssFeedable item : contentItems) {
			SyndEntry rssItem = makeRssItem(item);
			if (rssItem != null) {
				entires.add(rssItem);
			}
		}
		return entires;
	}
	
	private SyndEntry makeRssItem(RssFeedable item) {
		SyndEntryImpl entry = new SyndEntryImpl();
		entry.setTitle(item.getHeadline());
		entry.setLink(item.getWebUrl());
		entry.setPublishedDate(item.getDate());
		
		final String bodyText = item.getDescription();
		if (bodyText != null) {
			SyndContent description = new SyndContentImpl();
	        description.setType("text/plain");
	        description.setValue(bodyText);
	        entry.setDescription(description);
		}
		
		final String imageUrl = item.getImageUrl();
		if (imageUrl != null) {
			populateMediaModule(entry, imageUrl);                       
		}          
		return entry;
	}
	
	private void populateMediaModule(SyndEntryImpl entry, String thumbnailUrl) {
		try {
			MediaEntryModuleImpl media = new MediaEntryModuleImpl();
			MediaContent[] contents = new MediaContent[1];
			MediaContent item = new MediaContent(new UrlReference(thumbnailUrl));
			item.setType("image/jpeg");
			Metadata md = new Metadata();
			Thumbnail[] thumbs = new Thumbnail[1];
			thumbs[0] = new Thumbnail(new URL(thumbnailUrl));
			md.setThumbnail(thumbs);
			item.setMetadata(md);
			contents[0] = item;
			media.setMediaContents(contents);
			entry.getModules().add(media);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
}