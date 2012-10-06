package uk.co.eelpieconsulting.common.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.views.json.JsonSerializer;
import uk.co.eelpieconsulting.common.views.json.JsonView;
import uk.co.eelpieconsulting.common.views.rss.RssView;

@Component
public class ViewFactory {

	private final EtagGenerator etagGenerator;
	
	@Autowired
	public ViewFactory(EtagGenerator etagGenerator) {
		this.etagGenerator = etagGenerator;
	}

	public JsonView getJsonView() {
		return new JsonView(new JsonSerializer(), etagGenerator);
	}
	
	public JsonView getJsonView(int maxAge) {
		final JsonView view = new JsonView(new JsonSerializer(), etagGenerator);
		view.setMaxAge(maxAge);
		return view;
	}
	
	public RssView getRssView(String title, String link, String description) {
		final RssView view = new RssView(etagGenerator, title, link, description);
		return view;
	}
	
	public RssView getRssView(int maxAge, String title, String link, String description) {
		final RssView view = new RssView(etagGenerator, title, link, description);
		view.setMaxAge(maxAge);
		return view;
	}

}
