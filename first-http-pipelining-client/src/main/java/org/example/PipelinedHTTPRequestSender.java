package org.example;


import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class PipelinedHTTPRequestSender {

    private static final String GATEWAY_URI = "http://localhost:8080";

    private static final String GATEWAY_PATH = "/test-api";


    public static void main(final String[] args) throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(60, TimeUnit.SECONDS)
                .build();

        final HttpAsyncRequester requester = AsyncRequesterBootstrap.bootstrap()
                .setIOReactorConfig(ioReactorConfig)
                .create();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> requester.close(CloseMode.GRACEFUL)));
        requester.start();

        final HttpHost target = HttpHost.create(GATEWAY_URI);
        final String[] requestUris = new String[]{GATEWAY_PATH, GATEWAY_PATH};

        final Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(60));
        final AsyncClientEndpoint clientEndpoint = future.get();

        final CountDownLatch latch = new CountDownLatch(requestUris.length);
        for (final String requestUri : requestUris) {
            clientEndpoint.execute(
                    new BasicRequestProducer(Method.GET, target, requestUri),
                    new BasicResponseConsumer<>(new StringAsyncEntityConsumer()),
                    new FutureCallback<Message<HttpResponse, String>>() {

                        @Override
                        public void completed(final Message<HttpResponse, String> message) {
                            latch.countDown();
                            final HttpResponse response = message.getHead();
                            final String body = message.getBody();
                            System.out.println(
                                    requestUri + ". Response_status " + response.getCode() + ". Response: " + body);
                        }


                        @Override
                        public void failed(final Exception ex) {
                            latch.countDown();
                            System.out.println(requestUri + " -> " + ex);
                        }


                        @Override
                        public void cancelled() {
                            latch.countDown();
                            System.out.println(requestUri + " cancelled");
                        }

                    });
        }

        latch.await();

        clientEndpoint.releaseAndDiscard();
        requester.initiateShutdown();
    }

}
