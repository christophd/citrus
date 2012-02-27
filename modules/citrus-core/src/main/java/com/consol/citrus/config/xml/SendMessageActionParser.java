/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.variable.VariableExtractor;

/**
 * Bean definition parser for send action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SendMessageActionParser extends AbstractMessageActionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageSenderReference = element.getAttribute("with");
        
        BeanDefinitionBuilder builder;

        if (StringUtils.hasText(messageSenderReference)) {
            builder = parseComponent(element, parserContext);
            builder.addPropertyValue("name", element.getLocalName());

            builder.addPropertyReference("messageSender", messageSenderReference);
        } else {
            throw new BeanCreationException("Missing message sender attrbiute 'with'");
        }
        
        DescriptionElementParser.doParse(element, builder);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        
        AbstractMessageContentBuilder<?> messageBuilder = constructMessageBuilder(messageElement);
        parseHeaderElements(element, messageBuilder);
        
        if (messageBuilder != null) {
            builder.addPropertyValue("messageBuilder", messageBuilder);
        }

        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        parseExtractHeaderElements(element, variableExtractors);
        
        if (!variableExtractors.isEmpty()) {
            builder.addPropertyValue("variableExtractors", variableExtractors);
        }

        return builder.getBeanDefinition();
    }

    @Override
    protected void parseHeaderElements(Element actionElement, AbstractMessageContentBuilder<?> messageBuilder) {
        super.parseHeaderElements(actionElement, messageBuilder);
    }

    /**
     * Parse component returning generic bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.actions.SendMessageAction");
    }
}
