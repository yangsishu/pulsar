/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.runtime.worker.request;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.util.FutureUtil;
import org.apache.pulsar.functions.runtime.worker.Utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ServiceRequestManager implements AutoCloseable {

    private final Producer producer;

    public ServiceRequestManager(Producer producer) throws PulsarClientException {
        this.producer = producer;
    }

    public CompletableFuture<MessageId> submitRequest(ServiceRequest serviceRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Submitting Service Request: {}", serviceRequest);
        }
        byte[] bytes;
        try {
            bytes = Utils.toByteArray(serviceRequest);
        } catch (IOException e) {
            log.error("error serializing request {}", serviceRequest, e);
            return FutureUtil.failedFuture(e);
        }
        return producer.sendAsync(bytes);
    }

    @Override
    public void close() {
        try {
            this.producer.close();
        } catch (PulsarClientException e) {
            log.warn("Failed to close producer for service request manager", e);
        }
    }
}