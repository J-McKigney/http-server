package com.httpserver.http;

import java.util.ArrayList;

public class HttpRequest extends HttpMessage {

    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion;
    private HttpVersion bestCompatibleVersion;
    private ArrayList<HttpHeader> httpHeaders = new ArrayList<>();
    private String body;

    HttpRequest(){

    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(String methodName) throws HttpParsingException {
        for(HttpMethod method: HttpMethod.values()) {
            if(methodName.equals(method.name())){
                this.method = method;
                return;
            }
        }
        throw new HttpParsingException(
                HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED
        );

    }

    public String getRequestTarget() {
        return requestTarget;
    }

    public void setRequestTarget(String requestTarget) throws HttpParsingException {
        if(requestTarget == null || requestTarget.length() == 0){
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);
        }
        this.requestTarget = requestTarget;
    }


    public void setOriginalHttpVersion(String originalHttpVersion) throws InvalidHttpVersionException, HttpParsingException {
        this.originalHttpVersion = originalHttpVersion;
        this.bestCompatibleVersion = HttpVersion.getBestCompatibleVersion(originalHttpVersion);
        if(this.bestCompatibleVersion == null){
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_505_HTTP_VERSION_NOT_SUPPORTED);
        }
    }


    public String getOriginalHttpVersion() {
        return originalHttpVersion;
    }

    public HttpVersion getBestCompatibleVersion() {
        return bestCompatibleVersion;
    }

    public void addHttpHeader(HttpHeader httpHeader) {
        if(httpHeader != null) {
            this.httpHeaders.add(httpHeader);
        }
    }


    public void setBody(String body) {
        if(body != null) {
            this.body = body;
        }
    }

    public HttpHeader getContentLengthHeader(){
        for (HttpHeader header : httpHeaders) {
            if (header.getFieldName().equals("Content-Length")) {
                return header;
            }
        }
        return null;
    }
}
