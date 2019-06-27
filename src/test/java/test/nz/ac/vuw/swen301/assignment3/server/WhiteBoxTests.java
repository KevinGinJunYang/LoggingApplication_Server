package test.nz.ac.vuw.swen301.assignment3.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import nz.ac.vuw.swen301.assignment3.server.LogEvent;
import nz.ac.vuw.swen301.assignment3.server.LogsServlet;
import nz.ac.vuw.swen301.assignment3.server.LogsServletStats;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WhiteBoxTests {

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static TimeZone tz = TimeZone.getTimeZone("GMT");

    private String randomUUIDString = UUID.randomUUID().toString();
    private String message = "message";
    private String time = df.format(new Date(LogEvent.getCurrentTimeMilli()));
    private String thread = "thread";
    private String logger = "logger";
    private String logLevel = "DEBUG";
    private String errorDetails = "String";

    @Before
    public void clearBefore() {
        LogsServlet.storedLogs.clear();
    }

    @After
    public void clearAfter() {
        LogsServlet.storedLogs.clear();
    }

    @Test
    public void testInvalidRequestResponseCode1() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        // query parameter missing
        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertEquals(400,response.getStatus());
    }

    @Test
    public void testInvalidRequestResponseCode2() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("limit","ss");
        request.setParameter("level","DEBUG");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // wrong query parameter

        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertEquals(400,response.getStatus());
    }

    @Test
    public void testInvalidRequestResponseCode3() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("limits","11");
        request.setParameter("level","11");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // wrong query name

        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertEquals(400,response.getStatus());
    }
    @Test
    public void testInvalidRequestResponseCode4() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("limit","11");
        request.setParameter("level","11");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // wrong query value for level

        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertEquals(400,response.getStatus());
    }

    @Test
    public void testValidRequestResponseCode() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("limit","11");
        request.setParameter("level","DEBUG");
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertEquals(200,response.getStatus());
    }

    @Test
    public void testValidContentType() throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("limit","11");
        request.setParameter("level","DEBUG");
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogsServlet service = new LogsServlet();
        service.doGet(request,response);

        assertTrue(response.getContentType().startsWith("application/json"));
    }

    @Test
    public void testPost() throws ServletException, IOException {

        List<LogEvent> logEvent = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);

        logEvent.add(log);
        String jsonString = new Gson().toJson(logEvent);
        request.setContent(jsonString.getBytes());
        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        String jsonArray = response.getContentAsString();
        System.out.println(jsonArray);

        assertEquals(servlet.getLogs().get(0).toString(), logEvent.get(0).toString());
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testPost2() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,logLevel,errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(servlet.getLogs().size(), events.size());
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testPost3() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"FATAL",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(servlet.getLogs().size(), events.size());
        assertEquals(201, response.getStatus());
    }
    @Test
    public void testPost4() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"ERROR",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(servlet.getLogs().size(), events.size());
        assertEquals(201, response.getStatus());
    }
    @Test
    public void testPost5() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"WARN",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(servlet.getLogs().size(), events.size());
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testDoPostID() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
        for (int i = 0; i < 5; i++) {
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContent(jsonString.getBytes());
        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(1, servlet.getLogs().size());
        assertEquals(409, response.getStatus());
    }

    @Test
    public void testDoPostInvalid() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String jsonString = new Gson().toJson(events);
        request.setContent(jsonString.getBytes());
        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        assertEquals(0, servlet.getLogs().size());
    }


    @Test
    public void testDoGet() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,logLevel,errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "DEBUG");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }



    @Test
    public void testDoGet2() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 11; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,logLevel,errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);


        request.setParameter("level", "DEBUG");
        request.setParameter("limit", "5");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        // covert json String to LogEvent
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
        assertEquals(11, servlet.getLogs().size());
        assertEquals( 201, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
    }

    @Test
    public void testDoGet3() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 11; i++) {
            LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);


        request.setParameter("level", "DEBUG");
        request.setParameter("limit", "500");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(1, logEvents.size());
        assertEquals(1, servlet.getLogs().size());
        assertEquals( 409, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
    }

    @Test
    public void testDoGet4() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogsServlet servlet = new LogsServlet();

        request.setParameter("level", "ss");
        request.setParameter("limit", "5");

        servlet.doGet(request, response);

        assertEquals(0, servlet.getLogs().size());
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testDoGet5() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"FATAL",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "FATAL");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet6() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"ERROR",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "ERROR");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet7() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"WARN",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "WARN");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet8() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"INFO",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "INFO");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet9() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"TRACE",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "TRACE");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet10() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"ALL",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "ALL");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(5, logEvents.size());
    }

    @Test
    public void testDoGet11() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            LogEvent log = new LogEvent(UUID.randomUUID().toString(),message,time,thread,logger,"OFF",errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);
        request.setParameter("level", "OFF");
        request.setParameter("limit", "20");

        servlet.doGet(request, response);

        String responseArray = response.getContentAsString();
        System.out.println(responseArray);
        ObjectMapper objectMapper = new ObjectMapper();
        List<LogEvent> logEvents = objectMapper.readValue(responseArray, new TypeReference<List<LogEvent>>() {});

        assertEquals(0, logEvents.size());
    }

    @Test
    public void testStats() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogsServletStats servlet = new LogsServletStats();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStats2() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        LogsServletStats servlet = new LogsServletStats();

        servlet.doGet(request, response);

        assertEquals("application/vnd.ms-excel", response.getContentType());
    }

    @Test
    public void testStats3() throws ServletException, IOException {
        ArrayList<LogEvent> events = new ArrayList<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 11; i++) {
            LogEvent log = new LogEvent(randomUUIDString,message,time,thread,logger,logLevel,errorDetails);
            events.add(log);
        }
        String jsonString = new Gson().toJson(events);

        request.setContentType("application/json");
        request.setContent(jsonString.getBytes());

        LogsServlet servlet = new LogsServlet();
        servlet.doPost(request, response);

        LogsServletStats stats = new LogsServletStats();
        stats.doGet(request, response);


        String responseArray = response.getContentAsString();

        assertTrue(response.getContentType().startsWith("application/vnd.ms-excel"));
        assertTrue(responseArray.contains("Thread Count"));
    }

}
