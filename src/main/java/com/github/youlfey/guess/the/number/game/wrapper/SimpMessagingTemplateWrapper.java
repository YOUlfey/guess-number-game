package com.github.youlfey.guess.the.number.game.wrapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.AbstractMessageBrokerConfiguration;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.github.youlfey.guess.the.number.game.wrapper.SimpMessagingTemplateWrapper.TEMPLATE_WRAPPER;

@Component(value = TEMPLATE_WRAPPER)
@Primary
public class SimpMessagingTemplateWrapper extends SimpMessagingTemplate {
    public static final String TEMPLATE_WRAPPER = "simpMessagingTemplateWrapper";
    private static final String userDestination = "user";

    /**
     * @see AbstractMessageBrokerConfiguration#brokerChannel() - overide this bean
     * @see AbstractMessageBrokerConfiguration#brokerMessageConverter() - need for overriden bean
     * @see AbstractMessageBrokerConfiguration#brokerMessagingTemplate() - need for overriden bean
     */
    public SimpMessagingTemplateWrapper(@Qualifier("brokerChannel") AbstractSubscribableChannel messageChannel,
                                        @Qualifier("brokerMessageConverter") CompositeMessageConverter brokerMessageConverter) {
        super(messageChannel);
        super.setUserDestinationPrefix(userDestination);
        super.setMessageConverter(brokerMessageConverter);
    }

    @Override
    public void convertAndSendToUser(String user, String destination,
                                     Object payload, Map<String, Object> headers,
                                     MessagePostProcessor postProcessor) throws MessagingException {
        Assert.notNull(user, "User must not be null");
        Assert.isTrue(!user.contains("%2F"), "Invalid sequence \"%2F\" in user name: " + user);
        user = StringUtils.replace(user, "/", "%2F");
        destination = destination.startsWith("/") ? destination : "/" + destination;
        super.convertAndSend(String.format("%s-%s%s", destination,
                userDestination, user), payload, headers, postProcessor);
    }
}
