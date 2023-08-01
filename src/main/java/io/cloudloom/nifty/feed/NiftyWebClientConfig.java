package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.logging.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NiftyWebClientConfig {

    private final ObjectMapper objectMapper;

    /**
     *
     * NIFTY Feed API {@link WebClient}.
     *
     * @param cookieWebClient
     * @return
     */
    @Bean
    public WebClient niftyClient(@Qualifier("cookieWebClient") WebClient cookieWebClient){

        HttpClient httpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL); // Log request / response
        ClientHttpConnector conn = new ReactorClientHttpConnector(httpClient);


        WebClient client = WebClient.builder()
                .clientConnector(conn)
                .filter(cookieFilter(cookieWebClient))
                .defaultHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                .baseUrl("https://www.nseindia.com/api/")
                .exchangeStrategies(exchangeStrategies())
                .build();
        return client;
    }

    /**
     * {@link WebClient} to fetch cookies.
     *
     * @param resourceFactory
     * @return
     */
    @Bean("cookieWebClient")
    public WebClient cookieWebClient(ReactorResourceFactory resourceFactory) {
        var httpClient = HttpClient.create(resourceFactory.getConnectionProvider());
        var clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    /**
     * {@link ExchangeFilterFunction} that adds cookies to request.
     */
    @Bean
    public ExchangeFilterFunction cookieFilter(WebClient cookieWebClient) {
        return (request, next) -> cookieWebClient.get()
                .uri("https://www.nseindia.com/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                .exchangeToMono(response -> next.exchange(ClientRequest.from(request)
                        .cookies(readCookies(response))
                        .build()));
    }

    /**
     *
     * Read cookies from response.
     *
     * @param response
     * @return
     */
    private Consumer<MultiValueMap<String, String>> readCookies(ClientResponse response) {
        return cookies -> response.cookies().forEach((responseCookieName, responseCookies) ->
                cookies.addAll(responseCookieName,
                        responseCookies.stream().map(responseCookie -> responseCookie.getValue())
                                .collect(Collectors.toList())));
    }


    private ExchangeStrategies exchangeStrategies() {
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(objectMapper);
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(objectMapper);
        return ExchangeStrategies
                .builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(encoder);
                    configurer.defaultCodecs().jackson2JsonDecoder(decoder);
                }).build();
    }
}
