/*
 * Copyright (C) 2013 Maino
 * 
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
 * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
 * California, 94105, USA.
 * 
 */

package groovesquid;

import groovesquid.model.Clients;
import com.google.gson.Gson;
import groovesquid.util.Utils;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * @author Maino
 */
public class UpdateCheckThread extends Thread {
    private final static Logger log = Logger.getLogger(Main.class.getName());
    private static Gson gson = new Gson();
    private static String updateFile = "http://groovesquid.com/updatecheck.php";
    
    public UpdateCheckThread() {
        
    }
    
    @Override
    public void run() {
        UpdateCheck updateCheck = gson.fromJson(getFile(updateFile), UpdateCheck.class);
        if(updateCheck.getClients() != null)
            Grooveshark.setClients(updateCheck.getClients());
        
        if(Utils.compareVersions(updateCheck.getVersion(), Main.getVersion()) > 0) {
            if(JOptionPane.showConfirmDialog(null, "New version (v" + updateCheck.getVersion() + ") is available! Do you want to download the new version (recommended)?", "New version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                try {
                    Desktop.getDesktop().browse(java.net.URI.create("http://groovesquid.com/#download"));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static String getFile(String url) {
        String responseContent = null;
        HttpEntity httpEntity = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            httpGet.setHeader(HTTP.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
            
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpEntity = httpResponse.getEntity();
            
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                httpEntity.writeTo(baos);
            } else {
                throw new RuntimeException(url);
            }

            responseContent = baos.toString("UTF-8");
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        return responseContent;
    }
    
    public class UpdateCheck {
        private String version;
        private Clients clients;

        public String getVersion() {
            return version;
        }
        
        public Clients getClients() {
            return clients;
        }
    }
}
