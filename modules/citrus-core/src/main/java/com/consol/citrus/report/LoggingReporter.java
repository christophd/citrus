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

package com.consol.citrus.report;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.report.TestResult.RESULT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simple logging reporter printing test start and ending to the console/logger.
 * 
 * @author Christoph Deppisch
 */
public class LoggingReporter implements MessageListener, TestSuiteListener, TestListener, TestActionListener, TestReporter {
    
    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();

    /** Inbound message logger */
    private static Logger inboundMsgLogger = LoggerFactory.getLogger("Logger.Message_IN");

    /** Outbound message logger */
    private static Logger outboundMsgLogger = LoggerFactory.getLogger("Logger.Message_OUT");
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(LoggingReporter.class);

    /**
     * @see com.consol.citrus.report.TestReporter#clearTestResults()
     */
    public void clearTestResults() {
        testResults = new TestResults();
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults()
     */
    public void generateTestResults() {
        separator();
        newLine();
        log.info("CITRUS TEST RESULTS");
        newLine();

        for (TestResult testResult : testResults) {
            log.info(testResult.toString());

            if (testResult.getResult().equals(RESULT.FAILURE)) {
                log.info(testResult.getFailureCause());
            }
        }

        newLine();
        log.info("Number of skipped tests: " + testResults.getSkipped() + " (" + testResults.getSkippedPercentage() + "%)");
        newLine();

        log.info("TOTAL:\t" + (testResults.getFailed() + testResults.getSuccess()));
        log.info("FAILED:\t" + testResults.getFailed() + " (" + testResults.getFailedPercentage() + "%)");
        log.info("SUCCESS:\t" + testResults.getSuccess() + " (" + testResults.getSuccessPercentage() + "%)");
        newLine();

        separator();
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause, test.getParameters()));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, test.getParameters()));
        }

        newLine();
        log.error("TEST FAILED " + test.getName() + " <" + test.getPackageName() + "> Nested exception is: ", cause);
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        newLine();
        separator();
        log.info("SKIPPING TEST: " + test.getName());
        separator();
        newLine();

        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP, test.getParameters()));
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {
        newLine();
        separator();
        log.info("STARTING TEST " + test.getName() + " <" + test.getPackageName() + ">");
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS, test.getParameters()));

        newLine();
        log.info("TEST SUCCESS " + test.getName() + " (" + test.getPackageName() + ")");
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish()
     */
    public void onFinish() {
        newLine();
        separator();
        log.info("AFTER TEST SUITE");
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart()
     */
    public void onStart() {
        newLine();
        separator();
        log.info("C I T R U S   T E S T S");
        newLine();

        separator();
        log.info("BEFORE TEST SUITE");
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(java.lang.Throwable)
     */
    public void onFinishFailure(Throwable cause) {
        newLine();
        log.info("AFTER TEST SUITE: FAILED");
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess()
     */
    public void onFinishSuccess() {
        newLine();
        log.info("AFTER TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(java.lang.Throwable)
     */
    public void onStartFailure(Throwable cause) {
        newLine();
        log.info("BEFORE TEST SUITE: FAILED");
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess()
     */
    public void onStartSuccess() {
        newLine();
        log.info("BEFORE TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    /**
     * @see com.consol.citrus.report.TestActionListener#onTestActionStart(com.consol.citrus.TestCase, com.consol.citrus.TestAction)
     */
    public void onTestActionStart(TestCase testCase, TestAction testAction, TestContext context) {
        newLine();
        log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount());
        log.info("Test action <" + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + ">");

        if (testAction instanceof TestActionContainer) {
            log.info("Action container with " + ((TestActionContainer)testAction).getActionCount() + " embedded actions");
        }

        if (log.isDebugEnabled() && StringUtils.hasText(testAction.getDescription())) {
            log.debug("");
            log.debug(testAction.getDescription());
            log.debug("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestActionListener#onTestActionFinish(com.consol.citrus.TestCase, com.consol.citrus.TestAction)
     */
    public void onTestActionFinish(TestCase testCase, TestAction testAction, TestContext context) {
        log.info("Test action <" + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + "> done");
        log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + " done");
    }

    /**
     * @see com.consol.citrus.report.TestActionListener#onTestActionSkipped(com.consol.citrus.TestCase, com.consol.citrus.TestAction)
     */
    public void onTestActionSkipped(TestCase testCase, TestAction testAction, TestContext context) {
        newLine();
        log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount());
        log.info("Skipping test action <" + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + ">");
    }

    /**
     * @see MessageListener#onInboundMessage(String)
     */
    public void onInboundMessage(String message) {
        inboundMsgLogger.info(message);
    }

    /**
     * @see MessageListener#onOutboundMessage(String)
     */
    public void onOutboundMessage(String message) {
        outboundMsgLogger.info(message);
    }

    /**
     * Helper method to build consistent separators
     */
    private void separator() {
        log.info("------------------------------------------------------------------------");
    }

    /**
     * Adds new line to console logging output.
     */
    private void newLine() {
        log.info("");
    }
}
