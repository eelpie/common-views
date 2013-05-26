package uk.co.eelpieconsulting.common.views.json;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import uk.co.eelpieconsulting.common.views.EtagGenerator;

public class JsonView implements View {
	
	private final JsonSerializer jsonSerializer;
	private final EtagGenerator etagGenerator;
	private Integer maxAge;
	private String dataField;

	public JsonView(JsonSerializer jsonSerializer, EtagGenerator etagGenerator) {
		this.jsonSerializer = jsonSerializer;
		this.etagGenerator = etagGenerator;
		this.dataField = "data";
	}
	
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
	
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType());
    	response.setHeader("Cache-Control", "max-age=" + (maxAge != null ? maxAge : 0));		
		
		final String json = jsonSerializer.serialize(model.get(dataField));
		response.setHeader("Etag", etagGenerator.makeEtagFor(json));
		
		String callbackFunction = null;
		if (model.containsKey("callback")) {			
			callbackFunction = (String) model.get("callback");
			response.getWriter().write(callbackFunction + "(");			
		}
		
		response.getWriter().write(json);
		
		if (callbackFunction != null) {
			response.getWriter().write(");");			
		}
		
		response.getWriter().flush();
	}

}
