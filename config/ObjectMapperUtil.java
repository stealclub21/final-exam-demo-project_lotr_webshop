package hu.progmasters.webshop.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;

public class ObjectMapperUtil {

    private static final ModelMapper modelMapper;

    private ObjectMapperUtil(){}

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public static <D, T> List<D> mapAll(List<T> entities, Class<D> clazz) {
        return entities.stream()
                       .map(e -> modelMapper.map(e, clazz))
                       .toList();
    }
}
