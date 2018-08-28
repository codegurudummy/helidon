/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.webserver.spi;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import io.helidon.common.reactive.Flow;
import io.helidon.webserver.Http;
import io.helidon.webserver.ResponseChunk;
import io.helidon.webserver.Routing;
import io.helidon.webserver.SocketClosedException;
import io.helidon.webserver.WebServer;


/**
 * Bare (minimal) representation of HTTP Response. Used by {@link WebServer WebServer} implementations to invoke
 * a {@link Routing Routing}.
 */
public interface BareResponse extends Flow.Subscriber<ResponseChunk> {

    /**
     * Send response line and headers to the client.
     *
     * @param status  an HTTP status
     * @param headers a Map of header names and values
     * @throws SocketClosedException if headers were already send or response is closed
     * @throws NullPointerException if {@code status} is {@code null}
     */
    void writeStatusAndHeaders(Http.ResponseStatus status, Map<String, List<String>> headers)
            throws SocketClosedException, NullPointerException;

    /**
     * Returns a {@link CompletionStage} of headers part of this response. The stage is completed when all headers are sent to
     * the client.
     *
     * @return a completion stage of the response.
     */
    CompletionStage<BareResponse> whenHeadersCompleted();

    /**
     * Returns a {@link CompletionStage} of this response. The stage is completed when all response data are sent or response
     * channel is finished.
     *
     * @return a completion stage of the response.
     */
    CompletionStage<BareResponse> whenCompleted();

    /**
     * Each response is subscribed up to a single publisher and AFTER {@link #writeStatusAndHeaders(Http.ResponseStatus, Map)}
     * method is called and returned.
     *
     * @param subscription a subscription.
     */
    @Override
    void onSubscribe(Flow.Subscription subscription);

    /**
     * Provided {@link ByteBuffer} MUST be fully read during the method call.
     *
     * @param data chunk of the response payload
     * @throws SocketClosedException if response is already closed
     */
    @Override
    void onNext(ResponseChunk data) throws SocketClosedException;

    /**
     * Response should be flushed and closed.
     * <p>
     * <b>This method can be called without a subscription or demand. HTTP IO must be able to handle it.</b>
     *
     * @param thr an error.
     */
    @Override
    void onError(Throwable thr);

    /**
     * Response should be flushed and closed.
     * <p>
     * <b>This method can be called without a subscription or demand. HTTP IO must be able to handle it.</b>
     */
    @Override
    void onComplete();

    /**
     * A unique correlation ID that is associated with this response and its associated request.
     *
     * @return a unique correlation ID associated with this response and its request
     */
    long requestId();
}