package uk.co.eelpieconsulting.common.views.rss;

import java.util.Date;
import java.util.List;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

public interface RssFeedable {

	public String getHeadline();
	public String getWebUrl();
	public Date getDate();
	public String getDescription();
	public String getImageUrl();
	public LatLong getLatLong();
	public String getFeatureName();
	public String getAuthor();
	public List<String> getCategories();

}
