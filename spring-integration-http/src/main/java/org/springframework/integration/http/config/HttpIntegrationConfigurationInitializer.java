/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.integration.http.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.integration.config.IntegrationConfigurationInitializer;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.http.inbound.IntegrationHandlerResultHandler;
import org.springframework.integration.http.inbound.IntegrationRequestMappingHandlerMapping;
import org.springframework.integration.http.inbound.ReactiveIntegrationRequestMappingHandlerMapping;
import org.springframework.integration.http.support.HttpContextUtils;

/**
 * The HTTP Integration infrastructure {@code beanFactory} initializer.
 *
 * @author Artem Bilan
 *
 * @since 4.0
 */
public class HttpIntegrationConfigurationInitializer implements IntegrationConfigurationInitializer {

	private static final Log logger = LogFactory.getLog(HttpIntegrationConfigurationInitializer.class);

	@Override
	public void initialize(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			registerRequestMappingHandlerMappingIfNecessary((BeanDefinitionRegistry) beanFactory);
			registerReactiveRequestMappingHandlerMappingIfNecessary((BeanDefinitionRegistry) beanFactory);
		}
		else {
			logger.warn("'IntegrationRequestMappingHandlerMapping' isn't registered because 'beanFactory'" +
					" isn't an instance of `BeanDefinitionRegistry`.");
		}
	}

	/**
	 * Registers an {@link IntegrationRequestMappingHandlerMapping}
	 * which could also be overridden by the user by simply registering
	 * a {@link IntegrationRequestMappingHandlerMapping} {@code <bean>} with 'id'
	 * {@link HttpContextUtils#HANDLER_MAPPING_BEAN_NAME}.
	 * <p>
	 * In addition, checks if the {@code javax.servlet.Servlet} class is present on the classpath.
	 * When Spring Integration HTTP is used only as an HTTP client, there is no reason to use and register
	 * the HTTP server components.
	 */
	private void registerRequestMappingHandlerMappingIfNecessary(BeanDefinitionRegistry registry) {
		if (HttpContextUtils.WEB_MVC_PRESENT &&
				!registry.containsBeanDefinition(HttpContextUtils.HANDLER_MAPPING_BEAN_NAME)) {
			BeanDefinitionBuilder requestMappingBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(IntegrationRequestMappingHandlerMapping.class);
			requestMappingBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			requestMappingBuilder.addPropertyValue(IntegrationNamespaceUtils.ORDER, 0);
			registry.registerBeanDefinition(HttpContextUtils.HANDLER_MAPPING_BEAN_NAME,
					requestMappingBuilder.getBeanDefinition());
		}
	}

	/**
	 * Registers a {@link ReactiveIntegrationRequestMappingHandlerMapping}
	 * which could also be overridden by the user by simply registering
	 * a {@link ReactiveIntegrationRequestMappingHandlerMapping} {@code <bean>} with 'id'
	 * {@link HttpContextUtils#REACTIVE_HANDLER_MAPPING_BEAN_NAME}.
	 * <p>
	 * In addition, checks if the {@code org.springframework.web.reactive.result.method.RequestMappingInfo}
	 * class is present on the classpath.
	 * When Spring Integration HTTP is used only as an HTTP client, there is no reason to use and register
	 * the HTTP server components.
	 */
	private void registerReactiveRequestMappingHandlerMappingIfNecessary(BeanDefinitionRegistry registry) {
		if (HttpContextUtils.WEB_FLUX_PRESENT &&
				!registry.containsBeanDefinition(HttpContextUtils.REACTIVE_HANDLER_MAPPING_BEAN_NAME)) {
			BeanDefinitionBuilder requestMappingBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ReactiveIntegrationRequestMappingHandlerMapping.class);
			requestMappingBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			requestMappingBuilder.addPropertyValue(IntegrationNamespaceUtils.ORDER, 0);
			registry.registerBeanDefinition(HttpContextUtils.REACTIVE_HANDLER_MAPPING_BEAN_NAME,
					requestMappingBuilder.getBeanDefinition());

			BeanDefinitionReaderUtils.registerWithGeneratedName(
					new RootBeanDefinition(IntegrationHandlerResultHandler.class), registry);
		}
	}

}
