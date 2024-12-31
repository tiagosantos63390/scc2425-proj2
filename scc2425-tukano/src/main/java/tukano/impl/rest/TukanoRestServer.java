package tukano.impl.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import tukano.impl.Token;
import utils.Args;
import utils.IP;
import utils.Props;
import java.util.Properties;

@ApplicationPath("/rest")
public class TukanoRestServer extends Application {
	final private static Logger Log = Logger.getLogger(TukanoRestServer.class.getName());

	static final String INETADDR_ANY = "0.0.0.0";
	static String SERVER_BASE_URI = "http://%s:%s/tukano/rest";

	public static final int PORT = 8080;

	public static String serverURI;

	private final Set<Object> singletons = new HashSet<>();
	private final Set<Class<?>> resources = new HashSet<>();
			
	public TukanoRestServer() throws IOException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
		serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);

        // Load props
        /*try {
			var in = Props.class.getClassLoader().getResourceAsStream("keys.props");
			var reader = new InputStreamReader(in);
			var props = new Properties();
            props.load(reader);
			props.forEach( (k,v) -> System.setProperty(k.toString(), v.toString()));
			System.getenv().forEach( System::setProperty );
            
		} catch (IOException e) {
			System.err.println("Error loading props file: " + e.getMessage());
        }*/

        singletons.add(new RestUsersResource());
        singletons.add(new RestShortsResource());

        // Load properties and configurations
        Token.setSecret(Args.valueOf("-secret", ""));
        Log.info("Tukano Application initialized with resources and singletons");
    }


	@Override
    public Set<Class<?>> getClasses() {
        return resources;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
