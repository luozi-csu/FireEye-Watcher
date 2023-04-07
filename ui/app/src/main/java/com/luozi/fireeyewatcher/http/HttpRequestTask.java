package com.luozi.fireeyewatcher.http;

public class HttpRequestTask {
    private static HttpClient client;
    private HttpRequest request;
    private HttpResponseBuffer responseBuffer;

    private static class HttpResponseBuffer {
        private HttpResponse response;
        private boolean valueSet = false;

        public synchronized void put(HttpResponse response) {
            if (valueSet) {
                try {
                    wait(client.readTimeout + client.connectTimeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.response = response;
            valueSet = true;
            notify();
        }

        public synchronized HttpResponse get() {
            if (!valueSet) {
                try {
                    wait(client.readTimeout + client.connectTimeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            valueSet = false;
            notify();
            return this.response;
        }
    }

    public HttpRequestTask(HttpClient client, HttpRequest request) {
        this.client = client;
        this.request = request;
        this.responseBuffer = new HttpResponseBuffer();
    }

    public HttpResponse execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = client.doRequest(request);
                responseBuffer.put(response);
            }
        }).start();

        HttpResponse response = responseBuffer.get();
        return response;
    }
}
