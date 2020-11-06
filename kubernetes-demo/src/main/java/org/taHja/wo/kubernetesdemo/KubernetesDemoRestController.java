package org.taHja.wo.kubernetesdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Random;

@RestController
public class KubernetesDemoRestController {
    @Autowired
    private BuildProperties buildProperties;

    private Random random = new SecureRandom();

    @GetMapping("/check")
    public String check() throws InterruptedException {

        final long millis = Integer.toUnsignedLong(random.nextInt(1000));

        System.out.println("Sleeping " + millis + " milliseconds");
        Thread.sleep(millis);

        return buildProperties.getGroup() + ":"
                + buildProperties.getArtifact() + ":"
                + buildProperties.getVersion();
    }
}
