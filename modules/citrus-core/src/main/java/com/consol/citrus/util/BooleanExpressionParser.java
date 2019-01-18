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

package com.consol.citrus.util;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Parses boolean expression strings and evaluates to boolean result.
 * 
 * @author Christoph Deppisch
 */
@SuppressWarnings("unchecked")
public final class BooleanExpressionParser {
    
    /** List of known operators */
    private static final List<String> OPERATORS = new ArrayList<String>(
            CollectionUtils.arrayToList(new String[]{"(", "=", "and", "or", "lt", "lt=", "gt", "gt=", ")"}));

    /** List of known boolean values */
    private static final List<String> BOOLEAN_VALUES = new ArrayList<String>(
            CollectionUtils.arrayToList(new String[]{"true", "false"}));

    /**
     * SeparatorToken is an explicit type to identify different kinds of separators.
     */
    private enum SeparatorToken {
        SPACE(' '),
        OPEN_PARENTHESIS('('),
        CLOSE_PARENTHESIS(')');

        private final Character value;

        SeparatorToken(final Character value) {
            this.value = value;
        }
    }

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(BooleanExpressionParser.class);

    /**
     * Prevent instantiation.
     */
    private BooleanExpressionParser() {
    }
    
    /**
     * Perform evaluation of boolean expression string.
     * @param expression
     * @throws CitrusRuntimeException
     * @return
     */
    public static boolean evaluate(String expression) {
        Stack<String> operators = new Stack<String>();
        Stack<String> values = new Stack<String>();
        boolean result = true;

        char actChar;

        try {
            for (int i = 0; i < expression.length(); i++) {
                actChar = expression.charAt(i);
    
                if (SeparatorToken.OPEN_PARENTHESIS.value == actChar) {
                    operators.push(SeparatorToken.OPEN_PARENTHESIS.value.toString());
                } else if (SeparatorToken.SPACE.value == actChar) {
                    continue; //ignore
                } else if (SeparatorToken.CLOSE_PARENTHESIS.value == actChar) {
                    String operator = operators.pop();
                    while (!(operator).equals(SeparatorToken.OPEN_PARENTHESIS.value.toString())) {
                        values.push(getBooleanResultAsString(operator, values.pop(), values.pop()));
                        operator = operators.pop();
                    }
                } else if (!Character.isDigit(actChar)) {
                    StringBuffer operatorBuffer = new StringBuffer();
    
                    int m = i;
                    do {
                        operatorBuffer.append(actChar);
                        m++;
                        
                        if (m < expression.length()) {
                            actChar = expression.charAt(m);
                        }
                    } while (m < expression.length() && !Character.isDigit(actChar) && !isSeparatorToken(actChar));
    
                    i = m - 1;

                    if (BOOLEAN_VALUES.contains(operatorBuffer.toString())) {
                        values.push(Boolean.valueOf(operatorBuffer.toString()) ? "1" : "0");
                    } else {
                        operators.push(validateOperator(operatorBuffer.toString()));
                    }
                } else if (Character.isDigit(actChar)) {
                    StringBuffer digitBuffer = new StringBuffer();
    
                    int m = i;
                    do {
                        digitBuffer.append(actChar);
                        m++;
                        
                        if (m < expression.length()) {
                            actChar = expression.charAt(m);
                        }
                    } while (m < expression.length() && Character.isDigit(actChar));
    
                    i = m - 1;
    
                    values.push(digitBuffer.toString());
                }
            }
    
            while (!operators.isEmpty()) {
                values.push(getBooleanResultAsString(operators.pop(), values.pop(), values.pop()));
            }
    
            String value = values.pop();

            if (value.equals("0")) {
                value = "false";
            } else if (value.equals("1")) {
                value = "true";
            }

            result = Boolean.valueOf(value).booleanValue();
    
            if (log.isDebugEnabled()) {
                log.debug("Boolean expression " + expression + " evaluates to " + result);
            }
        } catch(EmptyStackException e) {
            throw new CitrusRuntimeException("Unable to parse boolean expression '" + expression + "'. Maybe expression is incomplete!", e);
        }

        return result;
    }

    /**
     * Checks whether a given character is a known separator token or no
     * @param possibleSeparatorChar The character to check
     * @return True in case its a separator, false otherwise
     */
    private static boolean isSeparatorToken(final char possibleSeparatorChar) {
        for (final SeparatorToken token : SeparatorToken.values()) {
            if (token.value == possibleSeparatorChar) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if operator is known to this class.
     * @param operator to validate
     * @return the operator itself.
     * @throws CitrusRuntimeException
     */
    private static String validateOperator(String operator) {
        if (!OPERATORS.contains(operator)) {
            throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
        return operator;
    }

    /**
     * Evaluates a boolean expression to a String representation (true/false).
     * @param operator
     * @param value1
     * @param value2
     * @return true/false as String
     */
    private static String getBooleanResultAsString(String operator, String value1, String value2) {
        if (operator.equals("lt")) {
            return Boolean.valueOf(Integer.valueOf(value2).intValue() < Integer.valueOf(value1).intValue()).toString();
        } else if (operator.equals("lt=")) {
            return Boolean.valueOf(Integer.valueOf(value2).intValue() <= Integer.valueOf(value1).intValue()).toString();
        } else if (operator.equals("gt")) {
            return Boolean.valueOf(Integer.valueOf(value2).intValue() > Integer.valueOf(value1).intValue()).toString();
        } else if (operator.equals("gt=")) {
            return Boolean.valueOf(Integer.valueOf(value2).intValue() >= Integer.valueOf(value1).intValue()).toString();
        } else if (operator.equals("=")) {
            return Boolean.valueOf(Integer.valueOf(value2).intValue() == Integer.valueOf(value1).intValue()).toString();
        } else if (operator.equals("and")) {
            return Boolean.valueOf(Boolean.valueOf(value2).booleanValue() && Boolean.valueOf(value1).booleanValue()).toString();
        } else if (operator.equals("or")) {
            return Boolean.valueOf(Boolean.valueOf(value2).booleanValue() || Boolean.valueOf(value1).booleanValue()).toString();
        } else {
            throw new CitrusRuntimeException("Unknown operator '" + operator + "'");
        }
    }
}
