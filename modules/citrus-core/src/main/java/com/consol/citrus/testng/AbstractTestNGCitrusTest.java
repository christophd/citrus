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

package com.consol.citrus.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.*;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;

/**
 * Abstract base test implementation for testng test cases. Providing test listener support and
 * loading basic application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(locations = { "classpath:com/consol/citrus/spring/root-application-ctx.xml", 
                                    "classpath:citrus-context.xml", 
                                    "classpath:com/consol/citrus/functions/citrus-function-ctx.xml",
                                    "classpath:com/consol/citrus/validation/citrus-validationmatcher-ctx.xml"})
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {
    /**
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TestSuiteListeners testSuiteListener = new TestSuiteListeners();
    
    @Autowired
    private TestListeners testListener;

    @Autowired
    private TestContextFactoryBean testContextFactory;
    
    @Autowired(required = false)
    private SequenceBeforeSuite beforeSuite;
    
    @Autowired(required = false)
    private SequenceAfterSuite afterSuite;
    
    @Autowired(required = false)
    private SequenceBeforeTest beforeTest;
    
    /** Parameter values provided from external logic */
    private Object[][] allParameters;

    /**
     * Runs tasks before test suite.
     * @param testContext the test context.
     * @throws Exception on error.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        /*
         * Fix for problem with Spring's TestNG support.
         * In order to have access to applicationContext in BeforeSuite annotated methods.
         * Fixed with version 3.1.RC1
         */
        springTestContextPrepareTestInstance();

        Assert.notNull(applicationContext);

        if (beforeSuite != null) {
            try {
                beforeSuite.execute(createTestContext());
            } catch (Exception e) {
                org.testng.Assert.fail("Before suite failed with errors", e);
            }
        } else {
            testSuiteListener.onStart();
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Runs tasks before tests.
     * @param testContext the test context.
     */
    @BeforeClass(alwaysRun=true, dependsOnMethods = "springTestContextPrepareTestInstance")
    public void beforeTest(ITestContext testContext) {
        if (beforeTest != null) {
            try {
                beforeTest.execute(createTestContext());
            } catch (Exception e) {
                org.testng.Assert.fail("Before test failed with errors", e);
            }
        }
    }
    
    /**
     * Executes the test case.
     */
    protected void executeTest() {
        executeTest(null);
    }

    /**
     * Executes the test case.
     * @param testContext the test context.
     */
    protected void executeTest(ITestContext testContext) {
        TestCase testCase = getTestCase();

        if (!testCase.getMetaInfo().getStatus().equals(Status.DISABLED)) {
            testListener.onTestStart(testCase);

            try {
                TestContext ctx = prepareTestContext(createTestContext());
                handleTestParameters(testCase, ctx);
                
                testCase.execute(ctx);
                testListener.onTestSuccess(testCase);
            } catch (Exception e) {
                testListener.onTestFailure(testCase, e);

                throw new TestCaseFailedException(e);
            } finally {
                testListener.onTestFinish(testCase);
                testCase.finish();
            }
        } else {
            testListener.onTestSkipped(testCase);
        }
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     * 
     * @param testCase the constructed Citrus test.
     * @param ctx the Citrus test context.
     */
    private void handleTestParameters(TestCase testCase, TestContext ctx) {
        if (allParameters != null) {
            Parameters parametersAnnotation = Reporter.getCurrentTestResult().getMethod().getMethod().getAnnotation(Parameters.class);
            if (parametersAnnotation == null) {
                throw new CitrusRuntimeException("Missing Parameters annotation, " +
                        "please provide parameter names with this annotation when using Citrus data provider!");
            }
            
            String[] parameterNames = parametersAnnotation.value();
            Object[] parameterValues = allParameters[Reporter.getCurrentTestResult().getMethod().getCurrentInvocationCount()];
            
            if (parameterValues.length != parameterNames.length) {
                throw new CitrusRuntimeException("Parameter mismatch: " + parameterNames.length + 
                        " parameter names defined with " + parameterValues.length + " parameter values available");
            }
            
            String[] parameters = new String[parameterValues.length];
            for (int k = 0; k < parameterValues.length; k++) {
                ctx.setVariable(parameterNames[k], parameterValues[k]);
                parameters[k] = "'" + parameterValues[k].toString() + "'";
            }
            
            testCase.setParameters(parameters);
        }
    }

    /**
     * Prepares the test context.
     *
     * Provides a hook for test context modifications before the test gets executed.
     *
     * @param testContext the test context.
     * @return the (prepared) test context.
     */
    protected TestContext prepareTestContext(final TestContext testContext) {
        return testContext;
    }

    /**
     * Creates a new test context.
     * @return the new citrus test context.
     * @throws Exception on error.
     */
    protected TestContext createTestContext() throws Exception {
        return (TestContext)testContextFactory.getObject();
    }

    /**
     * Gets the test case from application context.
     * @return the new test case.
     */
    protected TestCase getTestCase() {
        ClassPathXmlApplicationContext ctx = createApplicationContext();
        TestCase testCase = null;
        try {
            testCase = (TestCase) ctx.getBean(this.getClass().getSimpleName(), TestCase.class);
            testCase.setPackageName(this.getClass().getPackage().getName());
        } catch (NoSuchBeanDefinitionException e) {
            throw handleError("Could not find test with name '" + this.getClass().getSimpleName() + "'", e);
        }
        return testCase;
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext() {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[] {
                            this.getClass().getPackage().getName().replace('.', '/')
                                    + "/" + getClass().getSimpleName() + ".xml",
                                    "com/consol/citrus/spring/internal-helper-ctx.xml"},
                    true, applicationContext);
        } catch (Exception e) {
            throw handleError("Failed to load test case", e);
        }
    }

    /**
     * Handles error creating a new CitrusRuntimeException and 
     * informs test listeners.
     * 
     * @param message
     * @param cause
     * @return
     */
    private CitrusRuntimeException handleError(String message, Exception cause) {
        // Create empty backup test case for logging
        TestCase backupTest = new TestCase();
        backupTest.setName(getClass().getSimpleName());
        backupTest.setPackageName(getClass().getPackage().getName());
        
        CitrusRuntimeException exception = new CitrusRuntimeException(message, cause);
        
        // inform test listeners with failed test
        testListener.onTestStart(backupTest);
        testListener.onTestFailure(backupTest, exception);
        testListener.onTestFinish(backupTest);
        
        return exception;
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        if (afterSuite != null) {
            try {
                afterSuite.execute(createTestContext());
            } catch (Exception e) {
                org.testng.Assert.fail("After suite failed with errors", e);
            }
        } else {
            testSuiteListener.onFinish();
            testSuiteListener.onFinishSuccess();
        }
    }
    
    /**
     * Default data provider automatically adding parameters 
     * as variables to test case.
     * 
     * @param testContext the current TestNG test context.
     * @return
     */
    @DataProvider(name = "citrusDataProvider")
    protected Object[][] provideTestParameters() {
      allParameters = getParameterValues();
      return allParameters;
    }
    
    /**
     * Hook for subclasses to provide individual test parameters.
     * 
     * @return
     */
    protected Object[][] getParameterValues() {
        return new Object[][] { {} };
    }
}
