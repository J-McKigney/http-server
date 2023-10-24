package com.httpserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    private static final int SP = 0x20; //32
    private static final int CR = 0x0D; //13
    private static final int LF = 0x0A; //10
    private static final int COLON = 0x3A; //58

    public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
        LOGGER.info("Parsing HTTP request");

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        HttpRequest request = new HttpRequest();
        HttpHeader contentLength;

        try {
            parseRequestLine(inputStreamReader, request);
            parseHeaders(inputStreamReader, request);
            contentLength = request.getContentLengthHeader();
            if(contentLength != null && !contentLength.getFieldValue().equals("0")) {
                parseBody(inputStreamReader, request, contentLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Request Processed");
        return request;
    }

    private void parseBody(InputStreamReader inputStreamReader, HttpRequest request , HttpHeader contentLength) throws IOException {
        int _byte;
        int length = Integer.parseInt(contentLength.getFieldValue());
        StringBuilder processingDataBuffer = new StringBuilder();
        boolean bodyFound = false;
        while((_byte = inputStreamReader.read()) >= 0){
            if(_byte == CR)
            {
                _byte = inputStreamReader.read();
            }
            if(_byte == LF)
            {
                _byte = inputStreamReader.read();
                bodyFound = true;
            }
            if(bodyFound)
            {
                processingDataBuffer.append((char) _byte);
                break;
            }

        }
        request.setBody(processingDataBuffer.toString());
    }

    private void parseHeaders(InputStreamReader inputStreamReader, HttpRequest request) throws IOException, HttpParsingException {
        int _byte;
        boolean headerFound = false;
        boolean processingComplete = false;
        String headerName = "";

        StringBuilder processingDataBuffer = new StringBuilder();
        while((_byte = inputStreamReader.read()) >= 0){
            if(_byte == CR) {
                _byte = inputStreamReader.read();
                if(_byte == LF) {
                    headerFound = false;
                    if (!processingComplete){
                        processingComplete  = true;
                        request.addHttpHeader(new HttpHeader(headerName, processingDataBuffer.toString()));
                        processingDataBuffer.delete(0, processingDataBuffer.length());
                    }
                    else {
                        return;
                    }
                }
                else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            }
            if(_byte == COLON && !headerFound) {
                processingComplete = false;
                headerFound = true;
                headerName = processingDataBuffer.toString();
                processingDataBuffer.delete(0, processingDataBuffer.length());
                _byte = inputStreamReader.read();
                if(_byte != SP){
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            } else if(_byte != LF){
                processingDataBuffer.append((char) _byte);
            }
        }
    }

    private void parseRequestLine(InputStreamReader inputStreamReader, HttpRequest request) throws IOException, HttpParsingException {
        int _byte;
        StringBuilder processingDataBuffer = new StringBuilder();

        boolean methodParsed = false;
        boolean requestTargetParsed = false;

        while ((_byte = inputStreamReader.read()) >= 0) {
            if(_byte == CR){
                _byte = inputStreamReader.read();
                if (_byte == LF){
                    LOGGER.debug("Request Line VERSION to Process : {}", processingDataBuffer);
                    if(!methodParsed || !requestTargetParsed){
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }
                    try {
                        request.setOriginalHttpVersion(processingDataBuffer.toString());
                    } catch (InvalidHttpVersionException e) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }
                    return;
                } else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            }
            if (_byte == SP){
                if (!methodParsed) {
                    LOGGER.debug("Request Line METHOD to Process : {}", processingDataBuffer);
                    request.setMethod(processingDataBuffer.toString());
                    methodParsed = true;
                } else if (!requestTargetParsed)
                {
                    LOGGER.debug("Request Line REQ TARGET to Process : {}", processingDataBuffer);
                    request.setRequestTarget(processingDataBuffer.toString());
                    requestTargetParsed = true;
                } else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
                processingDataBuffer.delete(0, processingDataBuffer.length());
            } else {
                processingDataBuffer.append((char) _byte);
                if (!methodParsed){
                    if (processingDataBuffer.length() > HttpMethod.MAX_LENGTH){
                        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
                    }
                }
            }
        }
    }
}
