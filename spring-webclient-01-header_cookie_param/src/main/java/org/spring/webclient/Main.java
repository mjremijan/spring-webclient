package org.spring.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Value("${my.site.rest.url}")
    protected String url;
    
    @Value("${my.site.rest.cookie.name}")
    protected String cookieName;
    
    @Value("${my.site.rest.cookie.value}")
    protected String cookieValue;
    
    @Value("${my.site.rest.param.name}")
    protected String paramName;
    
    @Value("${my.site.rest.param.valueFormat}")
    protected String paramValueFormat;
    
    @Value("${my.proxy.host}")
    protected String host;
    
    @Value("${my.proxy.port}")
    protected Integer port;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.printf("URL: %s%n", url);
        System.out.printf("cookieName: %s%n", cookieName);
        System.out.printf("cookieValue: %s%n", cookieValue);
        System.out.printf("paramName: %s%n", paramName);
        System.out.printf("paramValueFormat: %s%n", paramValueFormat);
        System.out.printf("host: %s%n", host);
        System.out.printf("port: %s%n", port);
        
//        SslProvider sslProvider 
//            = SslProvider.builder().sslContext(
//                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
//            ).defaultConfiguration(SslProvider.DefaultConfigurationType.NONE).build()
//        ;

        TcpClient tcpClient = TcpClient.create()
            //.secure(sslProvider)
            .proxy(p -> p.type(ProxyProvider.Proxy.HTTP).host(host).port(port))
        ;

        HttpClient httpClient 
            = HttpClient.from(tcpClient);

        ClientHttpConnector httpConnector 
            = new ReactorClientHttpConnector(httpClient);
        
        System.out.printf("New webclient%n");
        WebClient webClient = WebClient.builder()
            .baseUrl(url)
            .clientConnector(httpConnector)
            .defaultHeader(HttpHeaders.USER_AGENT, String.format("Java/%s", System.getProperty("java.version")))
            .defaultHeader("Cookie", String.format("%s=%s",cookieName, cookieValue))
            .build();

        System.out.printf("Calling ...\"%s\"%n", url);
        int book = 1;
        int chapter = 1;
        String body
            = webClient
                .get()          
                .uri(uriBuilder -> uriBuilder                    
                    .path("/scripture_text.php")
                    .queryParam(paramName,String.format(paramValueFormat, book, chapter, 0, 999))
                    .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.printf("Body:%n%s%n", body);
    }
    
    
    
}
