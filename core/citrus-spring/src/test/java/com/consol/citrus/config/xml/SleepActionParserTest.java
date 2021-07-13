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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class SleepActionParserTest extends AbstractActionParserTest<SleepAction> {

    @Test
    public void testSleepActionParser() {
        assertActionCount(4);
        assertActionClassAndName(SleepAction.class, "sleep");

        SleepAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTime(), "5000");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTime(), "1.5");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.SECONDS);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTime(), "1.5");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.SECONDS);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTime(), "1500");
        Assert.assertEquals(action.getTimeUnit(), TimeUnit.MILLISECONDS);
    }
}
