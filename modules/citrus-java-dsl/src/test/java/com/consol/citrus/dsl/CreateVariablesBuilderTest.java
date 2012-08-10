package com.consol.citrus.dsl;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.actions.SleepAction;

public class CreateVariablesBuilderTest 
{
	@Test
	public void testCreateVariablesBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
		@Override
		protected void configure(){
			createVariables()
				.add("TestVariable", "TestValue");
		}
	};
	
	builder.configure();
	
	
	Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), CreateVariablesAction.class);
	
	Assert.assertEquals(((CreateVariablesAction)builder.getTestCase().getActions().get(0)).getName(), CreateVariablesAction.class.getSimpleName());
	System.out.println(((CreateVariablesAction)builder.getTestCase().getActions().get(0)).getVariables());
	Assert.assertEquals(((CreateVariablesAction)builder.getTestCase().getActions().get(0)).getVariables(), "{TestVariable=TestValue}");
	}
	
}
