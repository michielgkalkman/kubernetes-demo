package org.taHjaj.wo.demo.kubernetes.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.fusesource.jansi.Ansi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WebClientDemoCommandLineRunner implements CommandLineRunner {

    private final AtomicLong nrOfCallsMade = new AtomicLong();
    private final AtomicLong nrOfSessions = new AtomicLong();
    private final Ansi ansi = Ansi.ansi();

    private long nrOfSessionsLastTime = 0;
    private String lastError = "None";
    private String lastBody = "None";

    @Override
    public void run(String... args) throws Exception {
//        AnsiConsole.systemInstall(); // CraftBukkit - install Windows JNI library

        prepScreen();

        runWebClient();
    }

    private void prepScreen() {
        System.out.print( ansi.eraseScreen().fg(Ansi.Color.GREEN).bg(Ansi.Color.WHITE));
        System.out.print( ansi.cursor( 1, 1)
                .format("#Sessions :").toString());
        System.out.print( ansi.cursorDown(1).cursorToColumn(0)
                .format("#Calls    :").toString());
        System.out.print( ansi.cursorDown(1).cursorToColumn(0)
                .format("HttpStatus:").toString());
        System.out.print( ansi.cursorDown(1).cursorToColumn(0)
                .format("Last Error:").toString());
        System.out.print( ansi.cursorDown(1).cursorToColumn(0)
                .format("Last Body:").toString());
    }

    synchronized
    private void printNrOfSessions() {
        final long l = nrOfSessions.get();
        if( l != nrOfSessionsLastTime) {
            System.out.print(ansi.cursor(1, 12).format("%-5d", l).toString());
            nrOfSessionsLastTime = l;
        }
    }

    synchronized
    private void printNrOfCallsMade() {
        System.out.print( ansi.cursor( 2, 12).format( "%-5d", nrOfCallsMade.get()).toString());
    }

    synchronized
    private void printHttpStatus( HttpStatus httpStatus) {
        System.out.print( ansi.cursor( 3, 12).format( "%s", httpStatus.toString()).toString());
    }

    synchronized
    private void printLastError( Throwable throwable) {
        System.out.print( ansi.cursor( 4, 12).format( "%s:%s", throwable.getClass().getCanonicalName(), throwable.getLocalizedMessage()).toString());
    }

    synchronized
    private void printBody(String body) {
        if( !lastBody.equals(body)) {
            System.out.print(ansi.cursor(5, 12).format("%s", body).toString());
            lastBody = body;
        }
    }

    private void runWebClient() throws InterruptedException {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(5))
                                        .addHandlerLast(new WriteTimeoutHandler(5))));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient.wiretap(true));


        while( true) {
            final long nrOfSessionsCount = nrOfSessions.incrementAndGet();
//            System.out.println( "#sessions=" + nrOfSessionsCount);
            printNrOfSessions();
            if( nrOfSessionsCount < 10l) {
//                System.out.println( "Create WebClient - #sessions=" + nrOfSessionsCount);
                WebClient.builder().baseUrl("http://localhost:8080/check")
                        .clientConnector(connector)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build()
                        .get()
                        .retrieve()
                        .toEntity(String.class)
                        .doOnError(c -> {printLastError( c);})
                        .subscribe( r -> {
                            nrOfSessions.decrementAndGet();
                            nrOfCallsMade.incrementAndGet();
                            printNrOfSessions();
                            printNrOfCallsMade();
                            HttpStatus httpStatus = r.getStatusCode();
                            printHttpStatus(httpStatus);
                            printBody(r.getBody());
//                            System.out.println("statusCode=" + statusCode + ", #nrOfCallsMade=" + nrOfCallsMade.incrementAndGet());
                        });
            } else {
                nrOfSessions.decrementAndGet();
            }

            Thread.sleep(100);
        }
    }
}
