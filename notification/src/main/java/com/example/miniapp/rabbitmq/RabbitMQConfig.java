package com.example.miniapp.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;                 // same import
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String MULTI_QUEUE = "multi_event_queue";

    @Bean
    public Queue queue() {
        return new Queue(MULTI_QUEUE, true);
    }

    @Bean
    public TopicExchange communityExchange() {
        return new TopicExchange("community_exchange", true, false);
    }

    // add more topic exchanges for different things needed

    @Bean
    public Binding communityBinding(Queue queue, TopicExchange communityExchange) {
        return BindingBuilder.bind(queue).to(communityExchange).with("community.memberAdded");
    }

    /** Same JSON converter so listener can auto-deserialize */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // 1. Create a type-mapper and tell it which simple IDs map to which classes:
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idToClass = new HashMap<>();
        idToClass.put("MemberDTO",    com.example.miniapp.models.MemberDTO.class);

        // …add any other event/DTO types you need
        typeMapper.setIdClassMapping(idToClass);

        // 2. Tell it to use those IDs instead of the FQN
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        converter.setJavaTypeMapper(typeMapper);
        return converter;

    }

    /**
     * This factory is used by @RabbitListener to create the
     * listener container and apply your JSON converter.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf,
            Jackson2JsonMessageConverter converter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        return factory;
    }
}

