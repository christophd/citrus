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

package com.consol.citrus.context;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.VariableNullValueException;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;

/**
 * Class holding and managing test variables. The test context also provides utility methods
 * for replacing dynamic content(variables and functions) in message payloads and headers.
 * 
 * @author Christoph Deppisch
 */
public class TestContext {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TestContext.class);
    
    /** Local variables */
    protected Map<String, Object> variables;
    
    /** Global variables */
    private GlobalVariables globalVariables;
    
    /** Function registry holding all available functions */
    private FunctionRegistry functionRegistry = new FunctionRegistry();
    
    /** Registered message validators */
    private MessageValidatorRegistry messageValidatorRegistry;
    
    /** Registered validation matchers */
    private ValidationMatcherRegistry validationMatcherRegistry = new ValidationMatcherRegistry();
    
    /**
     * Default constructor
     */
    public TestContext() {
        variables = new LinkedHashMap<String, Object>();
    }
    
    /**
     * Gets the value for the given variable expression. Expression usually is the 
     * simple variable name, with optional expression prefix/suffix.
     * 
     * In case variable is not known to the context throw runtime exception.
     * 
     * @param variableExpression expression to search for.
     * @throws CitrusRuntimeException
     * @return value of the variable
     */
    public String getVariable(final String variableExpression) {
        return getVariableObject(variableExpression).toString();
    }
    
    /**
     * Gets the value for the given variable as object representation.
     * Use this method if you seek for test objects stored in the context.
     * 
     * @param variableExpression expression to search for.
     * @throws CitrusRuntimeException
     * @return value of the variable as object
     */
    public Object getVariableObject(final String variableExpression) {
        String variableName = VariableUtils.cutOffVariablesPrefix(variableExpression);
        
        if (!variables.containsKey(variableName)) {
            throw new CitrusRuntimeException("Unknown variable '" + variableName + "'");
        }

        return variables.get(variableName);
    }
    
    /**
     * Creates a new variable in this test context with the respective value. In case variable already exists 
     * variable is overwritten.
     * 
     * @param variableName the name of the new variable
     * @param value the new variable value
     * @throws CitrusRuntimeException
     * @return
     */
    public void setVariable(final String variableName, Object value) {
        if (!StringUtils.hasText(variableName) || VariableUtils.cutOffVariablesPrefix(variableName).length() == 0) {
            throw new CitrusRuntimeException("Can not create variable '"+ variableName + "', please define proper variable name");
        }

        if (value == null) {
            throw new VariableNullValueException("Trying to set variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + ", but variable value is null");
        }

        if (log.isDebugEnabled()) {
            log.debug("Setting variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + " with value: '" + value + "'");
        }

        variables.put(VariableUtils.cutOffVariablesPrefix(variableName), value);
    }
    
    /**
     * Add several new variables to test context. Existing variables will be 
     * overwritten.
     * 
     * @param variablesToSet the list of variables to set.
     */
    public void addVariables(Map<String, Object> variablesToSet) {
        for (Entry<String, Object> entry : variablesToSet.entrySet()) {
            if (entry.getValue() != null) {
                setVariable(entry.getKey(), entry.getValue());
            } else {
                setVariable(entry.getKey(), "");
            }
        }
    }

    /**
     * Replaces variables and functions inside a map with respective values and returns a new
     * map representation.
     *
     * @param map optionally having variable entries.
     * @return the constructed map without variable entries.
     */
    public Map<String, Object> resolveDynamicValuesInMap(final Map<String, ?> map) {
        Map<String, Object> target = new HashMap<String, Object>(map.size());

        for (Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            //put value into target map, but check if value is variable or function first
            target.put(key, resolveDynamicValue(value));
        }
        return target;
    }

    /**
     * Replaces variables and functions in a list with respective values and
     * returns the new list representation.
     *
     * @param list having optional variable entries.
     * @return the constructed list without variable entries.
     */
    public List<String> resolveDynamicValuesInList(final List<String> list) {
        List<String> variableFreeList = new ArrayList<String>(list.size());

        for (String entry : list) {
            //add new value after check if it is variable or function
            variableFreeList.add(resolveDynamicValue(entry));
        }
        return variableFreeList;
    }
    
    /**
     * Clears variables in this test context. Initially adds all global variables.
     */
    public void clear() {
        variables.clear();
        variables.putAll(globalVariables.getVariables());
    }
    
    /**
     * Checks if variables are present right now.
     * @return boolean flag to mark existence
     */
    public boolean hasVariables() {
        return !CollectionUtils.isEmpty(variables);
    }
    
    /**
     * Method replacing variable declarations and place holders as well as 
     * function expressions in a string
     * 
     * @param str the string to parse.
     * @return resulting string without any variable place holders.
     */
    public String replaceDynamicContentInString(String str) {
        return replaceDynamicContentInString(str, false);
    }

    /**
     * Method replacing variable declarations and functions in a string, optionally 
     * the variable values get surrounded with single quotes.
     * 
     * @param str the string to parse for variable place holders.
     * @param enableQuoting flag marking surrounding quotes should be added or not.
     * @return resulting string without any variable place holders.
     */
    public String replaceDynamicContentInString(final String str, boolean enableQuoting) {
        String result;
        result = VariableUtils.replaceVariablesInString(str, this, enableQuoting);
        result = FunctionUtils.replaceFunctionsInString(result, this, enableQuoting);
        
        return result;
    }
    
    /**
     * Checks wether the given expression is a variable or function and resolves the value
     * accordingly
     * @param expression the expression to resolve
     * @return the resolved expression value
     */
    public String resolveDynamicValue(String expression) {
        if (VariableUtils.isVariableName(expression)) {
            return getVariable(expression);
        } else if (functionRegistry.isFunction(expression)) {
            return FunctionUtils.resolveFunction(expression, this);
        }
        return expression;
    }
    
    /**
     * Setter for test variables in this context.
     * @param variables
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * Getter for test variables in this context.
     * @return test variables for this test context.
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Get global variables.
     * @param globalVariables
     */
	public void setGlobalVariables(GlobalVariables globalVariables) {
		this.globalVariables = globalVariables;
		
		variables.putAll(globalVariables.getVariables());
	}

    /**
     * Set global variables.
     * @return the globalVariables
     */
    public Map<String, Object> getGlobalVariables() {
        return globalVariables.getVariables();
    }
    
    /**
     * Get the current function registry.
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Set the function registry.
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * Set the message validator registry.
     * @param messageValidatorRegistry the messageValidatorRegistry to set
     */
    public void setMessageValidatorRegistry(MessageValidatorRegistry messageValidatorRegistry) {
        this.messageValidatorRegistry = messageValidatorRegistry;
    }

    /**
     * Get the message validator registry.
     * @return the messageValidatorRegistry
     */
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        return messageValidatorRegistry;
    }

    /**
     * Get the current validation matcher registry
     * @return
     */
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return validationMatcherRegistry;
    }

    /**
     * Set the validation matcher registry
     * @param validationMatcherRegistry
     */
    public void setValidationMatcherRegistry(ValidationMatcherRegistry validationMatcherRegistry) {
        this.validationMatcherRegistry = validationMatcherRegistry;
    }
    
}
