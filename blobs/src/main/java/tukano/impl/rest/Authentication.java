package main.java.tukano.impl.rest;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import main.java.tukano.impl.rest.auth.RequestCookies;
import main.java.utils.JSON;
import main.java.utils.RedisCache;
import redis.clients.jedis.Jedis;

@Path(Authentication.PATH)
public class Authentication {
    final private static Logger Log = Logger.getLogger(Authentication.class.getName());
	static final String PATH = "/login";
	static final String USER = "username";
	static final String PWD = "password";
	static final String COOKIE_KEY = "scc:session";
	static final String LOGIN_PAGE = "login.html";
	private static final int MAX_COOKIE_AGE = 3600;
	static final String REDIRECT_TO_AFTER_LOGIN = "/blobservice/";
	static final String ADMIN = "admin";

	@POST
	public Response login( @FormParam(USER) String user, @FormParam(PWD) String password ) {
		System.out.println("user: " + user + " pwd:" + password );
		boolean pwdOk = true; // replace with code to check user password
		if (pwdOk) {
			String uid = UUID.randomUUID().toString();
			var cookie = new NewCookie.Builder(COOKIE_KEY)
					.value(uid).path("/")
					.comment("sessionid")
					.maxAge(MAX_COOKIE_AGE)
					.secure(false) //ideally it should be true to only work for https requests
					.httpOnly(true)
					.build();

			try (Jedis jedis = RedisCache.getCachePool().getResource()) {
				var s = new Session( uid, user);
				jedis.setex(s.uid(), 3600, JSON.encode(s));
			}

			
            return Response.ok()
                    .cookie(cookie) 
                    .build();
		} else {
				 throw new NotAuthorizedException("Incorrect login");
		}        	
	}
	
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String login() {
		try {
			var in = getClass().getClassLoader().getResourceAsStream(LOGIN_PAGE);
			return new String( in.readAllBytes() );			
		} catch( Exception x ) {
			throw new WebApplicationException( Status.INTERNAL_SERVER_ERROR );
		}
	}

	// checks if there is a valid session and if userId equals admin, it also checks if it has permission
	static public void validateSession(String userId) throws NotAuthorizedException {
		var cookies = RequestCookies.get();
		Cookie cookie = cookies.get(COOKIE_KEY);

		if (cookie == null)
			throw new NotAuthorizedException("No session initialized");

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			var value = jedis.get(cookie.getValue());
			Session session = JSON.decode(value, Session.class);

			if (session == null)
				throw new NotAuthorizedException("No valid session initialized");

			if (session.user() == null || session.user().length() == 0)
				throw new NotAuthorizedException("No valid session initialized");

			if(userId.equals(ADMIN) && !session.user().equals(userId)) {
				throw new NotAuthorizedException("user : " + session.user() + "not authorized");
			}
		}
	}

}
