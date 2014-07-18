package lab.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.stereotype.Component;

@Component
public class JacksonObjectMapper extends ObjectMapper {
    public JacksonObjectMapper() {
        super();
        registerModule(new JodaModule());
        registerModule(new GuavaModule());
        disable(MapperFeature.AUTO_DETECT_CREATORS);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
