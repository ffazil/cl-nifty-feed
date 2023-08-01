package io.cloudloom.nifty.feed;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Flux;

@Getter
@ToString(exclude = {"data"})
public class Tick {
    private JsonNode data;

    public Tick(JsonNode data) {
        this.data = data;
    }
}
