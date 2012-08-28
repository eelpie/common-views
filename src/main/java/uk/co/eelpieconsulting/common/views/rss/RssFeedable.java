package uk.co.eelpieconsulting.common.views.rss;

import java.util.Date;

public interface RssFeedable {

	public String getHeadline();
	public String getWebUrl();
	public Date getDate();
	public String getDescription();

}
