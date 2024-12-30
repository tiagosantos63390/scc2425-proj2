package srv;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;

/**
 * Class with control endpoints.
 */
@Path("/ctrl")
public class ControlResource
{

	private static final String ADMIN = "admin";

	/**
	 * This methods just prints a string. It may be useful to check if the current 
	 * version is running on Azure.
	 */
	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String version() {
		var session = Authentication.validateSession(ADMIN);
		
		var sb = new StringBuilder("<html>");

		sb.append("<p>version: 0001</p>");
		sb.append(String.format("<p>session:%s, user:%s</p>", session.uid(), session.uid()));
		
		System.getProperties().forEach( (k,v) -> {
			sb.append("<p><pre>").append(k).append("  =  ").append( v ).append("</pre></p>");
		});
		sb.append("</hmtl>");
		return sb.toString();
	}

	@Path("/version2")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String version2( @CookieParam(Authentication.COOKIE_KEY) Cookie cookie) {

		var session = Authentication.validateSession(cookie, ADMIN);
		
		var sb = new StringBuilder("<html>");

		sb.append("<p>version: 0002</p>");
		sb.append(String.format("<p>session:%s, user:%s</p>", session.uid(), session.uid()));
		
		System.getProperties().forEach( (k,v) -> {
			sb.append("<p><pre>").append(k).append("  =  ").append( v ).append("</pre></p>");
		});
		sb.append("</hmtl>");
		return sb.toString();
	}

}
