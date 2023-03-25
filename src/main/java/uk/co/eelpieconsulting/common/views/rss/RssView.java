package uk.co.eelpieconsulting.common.views.rss;

import com.rometools.modules.georss.GeoRSSModule;
import com.rometools.modules.georss.SimpleModuleImpl;
import com.rometools.modules.georss.geometries.Position;
import com.rometools.modules.mediarss.MediaEntryModuleImpl;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.View;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.views.EtagGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    	final String rssContent = renderRss(makeRssEntiresForContent(contentItems));
		response.setHeader("Etag", etagGenerator.makeEtagFor(rssContent));
		
		response.getWriter().print(rssContent);
		response.getWriter().flush();
	}
	
	@Override
	public String getContentType() {
		return "text/xml";
	}

	protected String renderRss(List entries) {
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
		
		if (item.getAuthor() != null) {
			entry.setAuthor(item.getAuthor());
		}
		
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
		
		if (item.getLatLong() != null) {
			populateGeoRSSModule(entry, item.getLatLong(), item.getFeatureName());
		}

		if (item.getCategories() != null) {
			List<SyndCategory> categories = new ArrayList<>();
			for(String category: item.getCategories()) {
				SyndCategory syndCategory = new SyndCategoryImpl();
				syndCategory.setName(category);
				categories.add(syndCategory);
			}
			entry.setCategories(categories);
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
			thumbs[0] = new Thumbnail(new URI(thumbnailUrl));
			md.setThumbnail(thumbs);
			item.setMetadata(md);
			contents[0] = item;
			media.setMediaContents(contents);
			entry.getModules().add(media);

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	 private void populateGeoRSSModule(SyndEntryImpl entry, LatLong latLong, String featureName) {
         final GeoRSSModule geoRSSModule = new SimpleModuleImpl();
		 Position position = new Position(latLong.getLatitude(), latLong.getLongitude());
		 geoRSSModule.setPosition(position);
		 if (featureName != null) {
			 geoRSSModule.setFeatureNameTag(featureName);
		 }
         entry.getModules().add(geoRSSModule);
	 }
	
}