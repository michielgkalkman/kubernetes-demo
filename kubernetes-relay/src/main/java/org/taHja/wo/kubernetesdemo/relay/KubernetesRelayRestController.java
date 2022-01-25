package org.taHja.wo.kubernetesdemo.relay;

import feign.Feign;
import feign.Logger;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Random;

@RestController
public class KubernetesRelayRestController {
    @Autowired
    private BuildProperties buildProperties;

    private Random random = new SecureRandom();

    private KubernetesDemoClient kubernetesDemoClient = Feign.builder()
            .client(new OkHttpClient())
//            .encoder(new GsonEncoder())
//            .decoder(new GsonDecoder())
            .logger(new Slf4jLogger(KubernetesDemoClient.class))
            .logLevel(Logger.Level.FULL)
//            .target(KubernetesDemoClient.class, "https://localhost:8999/check");
            .target(KubernetesDemoClient.class, "https://192.168.68.109:8999/check");

    @GetMapping("/gocheck")
    public String gocheck() throws InterruptedException {

        return kubernetesDemoClient.check();
    }
}
