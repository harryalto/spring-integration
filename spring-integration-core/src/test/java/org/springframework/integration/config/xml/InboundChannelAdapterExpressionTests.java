/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mark Fisher
 * @author Artem Bilan
 *
 * @since 2.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class InboundChannelAdapterExpressionTests {

	@Autowired
	private ApplicationContext context;


	@Test
	public void fixedDelay() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("fixedDelayProducer", SourcePollingChannelAdapter.class);
		assertFalse(adapter.isAutoStartup());
		DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapter);
		Trigger trigger = TestUtils.getPropertyValue(adapter, "trigger", Trigger.class);
		assertEquals(PeriodicTrigger.class, trigger.getClass());
		DirectFieldAccessor triggerAccessor = new DirectFieldAccessor(trigger);
		assertEquals(1234L, triggerAccessor.getPropertyValue("period"));
		assertEquals(Boolean.FALSE, triggerAccessor.getPropertyValue("fixedRate"));
		assertEquals(this.context.getBean("fixedDelayChannel"), adapterAccessor.getPropertyValue("outputChannel"));
		Expression expression = TestUtils.getPropertyValue(adapter, "source.expression", Expression.class);
		assertEquals("'fixedDelayTest'", expression.getExpressionString());
	}

	@Test
	public void fixedRate() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("fixedRateProducer", SourcePollingChannelAdapter.class);
		assertFalse(adapter.isAutoStartup());
		DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapter);
		Trigger trigger = TestUtils.getPropertyValue(adapter, "trigger", Trigger.class);
		assertEquals(PeriodicTrigger.class, trigger.getClass());
		DirectFieldAccessor triggerAccessor = new DirectFieldAccessor(trigger);
		assertEquals(5678L, triggerAccessor.getPropertyValue("period"));
		assertEquals(Boolean.TRUE, triggerAccessor.getPropertyValue("fixedRate"));
		assertEquals(this.context.getBean("fixedRateChannel"), adapterAccessor.getPropertyValue("outputChannel"));
		Expression expression = TestUtils.getPropertyValue(adapter, "source.expression", Expression.class);
		assertEquals("'fixedRateTest'", expression.getExpressionString());
	}

	@Test
	public void cron() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("cronProducer", SourcePollingChannelAdapter.class);
		assertFalse(adapter.isAutoStartup());
		DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapter);
		Trigger trigger = TestUtils.getPropertyValue(adapter, "trigger", Trigger.class);
		assertEquals(CronTrigger.class, trigger.getClass());
		assertEquals("7 6 5 4 3 ?", new DirectFieldAccessor(new DirectFieldAccessor(
				trigger).getPropertyValue("sequenceGenerator")).getPropertyValue("expression"));
		assertEquals(this.context.getBean("cronChannel"), adapterAccessor.getPropertyValue("outputChannel"));
		Expression expression = TestUtils.getPropertyValue(adapter, "source.expression", Expression.class);
		assertEquals("'cronTest'", expression.getExpressionString());
	}

	@Test
	public void triggerRef() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("triggerRefProducer", SourcePollingChannelAdapter.class);
		assertTrue(adapter.isAutoStartup());
		DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapter);
		Trigger trigger = TestUtils.getPropertyValue(adapter, "trigger", Trigger.class);
		assertEquals(this.context.getBean("customTrigger"), trigger);
		assertEquals(this.context.getBean("triggerRefChannel"), adapterAccessor.getPropertyValue("outputChannel"));
		Expression expression = TestUtils.getPropertyValue(adapter, "source.expression", Expression.class);
		assertEquals("'triggerRefTest'", expression.getExpressionString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void headerExpressions() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("headerExpressionsProducer", SourcePollingChannelAdapter.class);
		assertFalse(adapter.isAutoStartup());
		Map<String, Expression> headerExpressions =
				TestUtils.getPropertyValue(adapter, "source.headerExpressions", Map.class);
		assertEquals(2, headerExpressions.size());
		assertEquals("6 * 7", headerExpressions.get("foo").getExpressionString());
		assertEquals("x", headerExpressions.get("bar").getExpressionString());
		assertEquals(42, headerExpressions.get("foo").getValue());
		assertEquals("x", headerExpressions.get("bar").getValue());
	}

	@Test
	public void testInt2867InnerExpression() {
		SourcePollingChannelAdapter adapter =
				this.context.getBean("expressionElement", SourcePollingChannelAdapter.class);
		Expression expression = TestUtils.getPropertyValue(adapter, "source.expression", Expression.class);
		assertEquals("'Hello World!'", expression.getExpressionString());
	}

}
