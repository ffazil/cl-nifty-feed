package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.context.event.EventListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.integration.dsl.MessageChannels;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final NiftyProxy niftyProxy;
    private final SubscribableChannel subscribableChannel = MessageChannels.publishSubscribe().getObject();

    @GetMapping(path = "/feed")
    public Mono<JsonNode> feed(@RequestParam("symbol") String symbol, @RequestParam("option") String option,
                               @RequestParam("expiry") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate expiry,
                               @RequestParam("type") Type type, @RequestParam("strikePrice") String strikePrice){
        Mono<JsonNode> feed = niftyProxy.feed(symbol, option, expiry, type, Money.of(new BigDecimal(strikePrice), "INR"));
        return feed;
    }

    @GetMapping(path = "/ticker", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<JsonNode> ticker(){
        Flux<JsonNode> response = Flux.create(sink -> {
            MessageHandler handler = message -> sink.next(Tick.class.cast(message.getPayload()).getData());
            sink.onCancel(() -> subscribableChannel.unsubscribe(handler));
            subscribableChannel.subscribe(handler);
        }, FluxSink.OverflowStrategy.LATEST);

        return response;
    }

    @EventListener
    public void on(Tick tick){
        subscribableChannel.send(new GenericMessage<>(tick));
    }
}
