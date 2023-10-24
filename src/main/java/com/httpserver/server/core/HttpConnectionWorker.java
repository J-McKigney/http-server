package com.httpserver.server.core;

import com.httpserver.http.HttpParser;
import com.httpserver.http.HttpParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorker extends Thread{

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorker.class);
    private Socket socket;

    HttpConnectionWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpParser parser = new HttpParser();
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            String html = "<html><head><title>Simple Java Server</title></head><body><h1>Simple Java HTTP server</h1>Test document</body></html>";

            final String crlf = "\n\r";
            String response = "HTTP/1.1 200 OK" + crlf +
                    "Content-Length: " + html.getBytes().length + crlf + crlf +
                    html + crlf + crlf;

            try {
                parser.parseHttpRequest(inputStream);
            } catch (HttpParsingException e) {
                e.printStackTrace();
            }

            outputStream.write(response.getBytes());
            LOGGER.info("Connection processing finished");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
