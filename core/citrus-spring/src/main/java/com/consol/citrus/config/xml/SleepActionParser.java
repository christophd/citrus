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

import java.util.concurrent.TimeUnit;

import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for sleep action in test case.
 *
 * @author Christoph Deppisch
 */
public class SleepActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SleepActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("time"), "seconds");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("seconds"), "seconds");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("milliseconds"), "milliseconds");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class SleepActionFactoryBean extends AbstractTestActionFactoryBean<SleepAction, SleepAction.Builder> {

        private final SleepAction.Builder builder = new SleepAction.Builder();

        public void setMilliseconds(String milliseconds) {
            builder.time(milliseconds);
        }

        public void setSeconds(String seconds) {
            builder.time(seconds, TimeUnit.SECONDS);
        }

        public void setTime(String time) {
            builder.time(time, TimeUnit.SECONDS);
        }

        @Override
        public SleepAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SleepAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public SleepAction.Builder getBuilder() {
            return builder;
        }
    }
}
