package org.taHja.wo.kubernetesdemo.relay;

import feign.RequestLine;

public interface KubernetesDemoClient {
    @RequestLine("GET")
    String check();
}
