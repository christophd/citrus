/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.actions;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

/**
 * Action purges all messages from a message channel instance. Message channel must be
 * of type {@link QueueChannel}. Action receives a list of channel objects or a list of channel names
 * that are resolved dynamically at runtime.
 * 
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelAction extends AbstractTestAction implements InitializingBean, BeanFactoryAware {
    /** List of channel names to be purged */
    private List<String> channelNames = Collections.emptyList();

    /** List of channels to be purged */
    private List<MessageChannel> channels = Collections.emptyList();
    
    /** The parent bean factory used for channel name resolving */
    private BeanFactory beanFactory;
    
    /** Channel resolver instance */
    private ChannelResolver channelResolver;
    
    /** Selector filter messages to be purged on channels */
    private MessageSelector messageSelector;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(PurgeMessageChannelAction.class);
    
    @Override
    public void doExecute(TestContext context) {
        log.info("Purging message channels ...");
        
        for (MessageChannel channel : channels) {
            purgeChannel(channel);
        }
        
        for (String channelName : channelNames) {
            purgeChannel(resolveChannelName(channelName));
        }

        log.info("Message channel purged successfully");
    }

    /**
     * Purges all messages from a message channel. Prerequisit is that channel is
     * of type {@link QueueChannel}.
     * 
     * @param channel
     */
    private void purgeChannel(MessageChannel channel) {
        if (channel instanceof QueueChannel) {
            if (log.isDebugEnabled()) {
                log.debug("Try to purge message channel " + ((QueueChannel)channel).getComponentName());
            }
            
            List<Message<?>> messages = ((QueueChannel)channel).purge(messageSelector);
            
            if (log.isDebugEnabled()) {
                log.debug("Purged " + messages.size() + " messages from channel");
            }
        }
    }
    
    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName) {
        if (channelResolver == null) {
            channelResolver = new BeanFactoryChannelResolver(beanFactory);
        }
        
        return channelResolver.resolveChannelName(channelName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (messageSelector == null) {
            messageSelector = new MessageSelector() {
                public boolean accept(Message<?> message) {
                    return false; //when purging we have to use "false" in order to include all queues
                }
            };
        }
    }

    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Gets the channelNames.
     * @return the channelNames the channelNames to get.
     */
    public List<String> getChannelNames() {
        return channelNames;
    }

    /**
     * Sets the channelNames.
     * @param channelNames the channelNames to set
     */
    public void setChannelNames(List<String> channelNames) {
        this.channelNames = channelNames;
    }

    /**
     * Gets the channels.
     * @return the channels the channels to get.
     */
    public List<MessageChannel> getChannels() {
        return channels;
    }

    /**
     * Sets the channels.
     * @param channels the channels to set
     */
    public void setChannels(List<MessageChannel> channels) {
        this.channels = channels;
    }

    /**
     * Gets the messageSelector.
     * @return the messageSelector the messageSelector to get.
     */
    public MessageSelector getMessageSelector() {
        return messageSelector;
    }

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    public void setMessageSelector(MessageSelector messageSelector) {
        this.messageSelector = messageSelector;
    }

}
