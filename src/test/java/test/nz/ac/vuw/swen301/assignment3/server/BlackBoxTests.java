package test.nz.ac.vuw.swen301.assignment3.server;

import com.google.gson.Gson;
import nz.ac.vuw.swen301.assignment3.server.LogEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BlackBoxTests {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 8080;
    private static final String TEST_PATH = "/resthome4logs"; // as defined in pom.xml
    private static final String SERVICE_PATH = TEST_PATH + "/logs"; // as defined in pom.xml and web.xml
    private static final String STATS_PATH = TEST_PATH + "/stats"; // as defined in pom.xml and web.xml
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    public static TimeZone tz = TimeZone.getTimeZone("GMT");

    String randomUUIDString = UUID.randomUUID().toString();
    String message = "message";
    String time = df.format(new Date(LogEvent.getCurrentTimeMilli()));
    String thread = "thread";
    String logger = "logger";
    String logLevel = "FATAL";
    String errorDetails = "String";

    //netstat -nlp|grep 8080

    static Process process;
    @BeforeClass
    public static void startServer() throws Exception {
        process = Runtime.getRuntime().exec("mvn jetty:run");
        Thread.sleep(5000);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        process.destroy();
    }

    private HttpResponse get(URI uri) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);
        return httpClient.execute(request);
    }

    private boolean isServerReady() throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(TEST_PATH);
        URI uri = builder.build();
        try {
            HttpResponse response = get(uri);
            boolean success = response.getStatusLine().getStatusCode() == 200;

            if (!success) {
                System.err.println("Check whether server is up and running, request to " + uri + " returns " + response.getStatusLine());
            }

            return success;
        } catch (Exception x) {
            System.err.println("Encountered error connecting to " + uri + " -- check whether server is running and application has been deployed");
            return false;
        }
    }

    @Test
    public void testValidRequestResponseCode() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(TEST_PATH)
               .setParameter("limit", "11").setParameter("level", "DEBUG");
        URI uri = builder.build();
        HttpResponse response = get(uri);

        assertEquals(200, response.getStatusLine().getStatusCode());
    }

   @Test
    public void testInvalidRequestResponseCode1() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        // query parameter missing
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH);
        URI uri = builder.build();
        HttpResponse response = get(uri);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRequestResponseCode2() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        // wrong query parameter name
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH)
                .setParameter("limits", "ss").setParameter("level", "DEBUG");
        URI uri = builder.build();
        HttpResponse response = get(uri);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRequestResponseCode3() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        // wrong query parameter name
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH)
                .setParameter("limit", "ss").setParameter("level", "tt");
        URI uri = builder.build();
        HttpResponse response = get(uri);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testValidContentType() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH)
                .setParameter("limit", "11").setParameter("level", "DEBUG");
        URI uri = builder.build();
        HttpResponse response = get(uri);

        assertNotNull(response.getFirstHeader("Content-Type"));

        // use startsWith instead of assertEquals since server may append char encoding to header value
        assertTrue(response.getFirstHeader("Content-Type").getValue().startsWith("application/json"));
    }



    @Test
    public void testPostRequest() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "11").setParameter("level","WARN");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,"yes",logLevel,errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        String responseData2 = response.getEntity().getContentType().toString();
        System.out.println("server response: " + responseData);

        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseData2.contains("application/json"));
    }

    @Test
    public void testPostRequest2() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "11").setParameter("level","WARN");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,"yes","WARN",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

   @Test
    public void testGetRequest() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","DEBUG");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest2() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "13").setParameter("level","FATAL");
        URI uri = builder.build();

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpGet request = new HttpGet(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);
        HttpResponse response = client.execute(request);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertTrue(responseData.contains("message"));
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest3() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","FATAL");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"FATAL",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest4() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","ERROR");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"ERROR",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest5() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","WARN");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"WARN",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest6() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","INFO");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"INFO",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest7() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","TRACE");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"TRACE",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest8() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","ALL");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"ALL",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetRequest9() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "12").setParameter("level","OFF");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,"OFF",errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response = get(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);


        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertNotNull(response.getEntity());
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRequest() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "14").setParameter("level","ss");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpGet request = new HttpGet(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);
        HttpResponse response = client.execute(request);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRequest2() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "ss").setParameter("level","DEBUG");
        URI uri = builder.build();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
        events.add(log);

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpGet request = new HttpGet(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);
        HttpResponse response = client.execute(request);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testMultipleLogsToServer() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "15").setParameter("level","FATAL");
        URI uri = builder.build();

        for(int i = 0; i < 10; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(), message, time, thread, logger, logLevel, errorDetails);
            events.add(log);
        }

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpGet request = new HttpGet(uri);


        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);
        HttpResponse response = client.execute(request);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertTrue(responseData.contains("message"));
        assertEquals(201, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testSameIDCode() throws Exception {
        Assume.assumeTrue(isServerReady());
        ArrayList<LogEvent> events = new ArrayList<>();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(SERVICE_PATH).setParameter("limit", "15").setParameter("level","FATAL");
        URI uri = builder.build();

        for(int i = 0; i < 10; i++) {
            LogEvent log = new LogEvent("yes", message, time, thread, logger, logLevel, errorDetails);
            events.add(log);
        }

        String jsonString = new Gson().toJson(events);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(uri);
        HttpGet request = new HttpGet(uri);

        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse response2 = client.execute(httpPost);
        HttpResponse response = client.execute(request);

        System.out.println("Entity: " + response2);
        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

        assertEquals(409, response2.getStatusLine().getStatusCode());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }


   @Test
    public void testStatsGenerator() throws Exception {
        Assume.assumeTrue(isServerReady());
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(TEST_HOST).setPort(TEST_PORT).setPath(STATS_PATH);
        URI uri = builder.build();

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);

        HttpResponse response = client.execute(request);

        String responseData = EntityUtils.toString(response.getEntity());
        System.out.println("server response: " + responseData);

       assertEquals(200, response.getStatusLine().getStatusCode());
       assertTrue(response.getFirstHeader("Content-Type").getValue().startsWith("application/vnd.ms-excel"));

    }

    //WORKS WITH DATA MADE FROM PREVIOUS TESTS
    @Test
    public void testReadStats() throws Exception {
        Assume.assumeTrue(isServerReady());
        URLConnection connection = new URL("http://localhost:8080/resthome4logs/stats").openConnection();

        InputStream response23 = connection.getInputStream();
        Workbook workbook2 = new HSSFWorkbook(response23);
        workbook2.write(new FileOutputStream("LogStats.xls"));
        workbook2.close();

        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream("LogStats.xls"));
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFRow date = sheet.getRow(0);
        HSSFRow name = sheet.getRow(1);
        HSSFRow level = sheet.getRow(2);
        HSSFRow thread = sheet.getRow(3);
        HSSFRow logs1 = sheet.getRow(4);
        HSSFRow logs2 = sheet.getRow(5);
        HSSFRow logs3 = sheet.getRow(6);

        assertEquals("Date:", date.getCell(0).getStringCellValue());
        assertEquals("Logger Name:", name.getCell(0).getStringCellValue());
        assertEquals("Logger Level:", level.getCell(0).getStringCellValue());
        assertEquals("Log threads:", thread.getCell(0).getStringCellValue());
        assertEquals("Logger Count:", logs1.getCell(0).getStringCellValue());
        assertEquals("Level Count:", logs2.getCell(0).getStringCellValue());
        assertEquals("Thread Count:", logs3.getCell(0).getStringCellValue());

    }

}
