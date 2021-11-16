package i5.las2peer.services.gamificationStreakService;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;


import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationStreakService.database.StreakModel;
import i5.las2peer.testing.MockAgentFactory;

import org.json.JSONObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationStreakServiceTest {

private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3;

	private static UserAgentImpl user1, user2, user3;

	private static String gameId = "test";
	private static String streakId = "streakTest";

	private static final String mainPath = "gamification/streaks/";
	
	private StreakModel streak;
	
	private Map<String, String> headers;
	
	// to fetch data per batch
	int currentPage = 1;
	int windowSize = 10;
	String searchParam = "";
	
	String unitName = "dollar";
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {

		// start node
		//node = LocalNode.newNode();
		
		node = new LocalNodeManager().newNode();
		node.launch();
		
		user1 = MockAgentFactory.getAdam();
		user2 = MockAgentFactory.getAbel();
		user3 = MockAgentFactory.getEve();

		// agent must be unlocked in order to be stored 
		user1.unlock("adamspass");
		user2.unlock("abelspass");
		user3.unlock("evespass");

		node.storeAgent(user1);
		node.storeAgent(user2);
		node.storeAgent(user3);

		node.startService(new ServiceNameVersion(GamificationStreakService.class.getName(), "0.1"), "a pass");

		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		// wait a second for the connector to become ready
		Thread.sleep(1000);
		
		c1 = new MiniClient();
		c1.setConnectorEndpoint(connector.getHttpEndpoint());
		c1.setLogin(user1.getIdentifier(), "adamspass");
		
		c2 = new MiniClient();
		c2.setConnectorEndpoint(connector.getHttpEndpoint());
		c2.setLogin(user2.getIdentifier(), "abelspass");

		c3 = new MiniClient();
		c3.setConnectorEndpoint(connector.getHttpEndpoint());
		c3.setLogin(user3.getIdentifier(), "evespass");
		
		//TODO create streak with infos
		streak = new StreakModel();
		
		headers = new HashMap<>();
		
		headers.put("Accept-Encoding","gzip, deflate");
		headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
	}

	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

	

	@Test
	public void testH1_createNewStreak(){
		System.out.println("Test --- Create New Streak");
		try
		{
			
			JSONObject body = new JSONObject(streak);
			
			
			ClientResponse result = c1.sendRequest("POST", mainPath + "" + gameId, body.toString(), "application/json", "*/*", headers);

			System.out.println(result.getResponse());
			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
			}
			else{

				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testH2_getStreakWithId(){
		System.out.println("Test --- Get Streak With Id");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "" + gameId + "/" + streakId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}

	}
	
	@Test
	public void testH3_updateStreak(){
		System.out.println("Test --- Update Streak");
		try
		{
			JSONObject body = new JSONObject(streak);
			
			ClientResponse result = c1.sendRequest("PUT", mainPath + "" + gameId +"/"+ streakId, body.toString(), "application/json", "*/*", headers);
			
			System.out.println(result.getResponse());
			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testH4_getStreakList()
	{
		System.out.println("Test --- Get Streak List");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "" + gameId + "?current=1&rowCount=10&searchPhrase=", "");
			assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	
	}

	@Test
	public void testZ9_deleteStreak(){
		try
		{
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "" + gameId + "/" + streakId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
}
