/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.web.context;

import java.util.Arrays;
import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * Registers the {@link DelegatingFilterProxy} to use the springSessionRepositoryFilter
 * before any other registered {@link Filter}. When used with
 * {@link #AbstractHttpSessionApplicationInitializer(Class...)}, it will also register a
 * {@link ContextLoaderListener}. When used with
 * {@link #AbstractHttpSessionApplicationInitializer()}, this class is typically used in
 * addition to a subclass of {@link AbstractContextLoaderInitializer}.
 *
 * <p>
 * By default the {@link DelegatingFilterProxy} is registered with support for
 * asynchronous requests, but can be enabled by overriding
 * {@link #isAsyncSessionSupported()} and {@link #getSessionDispatcherTypes()}.
 * </p>
 *
 * <p>
 * Additional configuration before and after the springSecurityFilterChain can be added by
 * overriding {@link #afterSessionRepositoryFilter(ServletContext)}.
 * </p>
 *
 *
 * <h2>Caveats</h2>
 * <p>
 * Subclasses of {@code AbstractDispatcherServletInitializer} will register their filters
 * before any other {@link Filter}. This means that you will typically want to ensure
 * subclasses of {@code AbstractDispatcherServletInitializer} are invoked first. This can
 * be done by ensuring the {@link Order} or {@link Ordered} of
 * {@code AbstractDispatcherServletInitializer} are sooner than subclasses of
 * {@code AbstractSecurityWebApplicationInitializer}.
 * </p>
 *
 * @author Rob Winch
 *
 */
@Order(100)
public abstract class AbstractHttpSessionApplicationInitializer implements WebApplicationInitializer {

	private static final String SERVLET_CONTEXT_PREFIX = "org.springframework.web.servlet.FrameworkServlet.CONTEXT.";

	/**
	 * The default name for Spring Session's repository filter.
	 */
	public static final String DEFAULT_FILTER_NAME = "springSessionRepositoryFilter";

	private final Class<?>[] configurationClasses;

	/**
	 * Creates a new instance that assumes the Spring Session configuration is loaded by
	 * some other means than this class. For example, a user might create a
	 * {@link ContextLoaderListener} using a subclass of
	 * {@link AbstractContextLoaderInitializer}.
	 *
	 * @see ContextLoaderListener
	 */
	protected AbstractHttpSessionApplicationInitializer() {
		this.configurationClasses = null;
	}

	/**
	 * Creates a new instance that will instantiate the {@link ContextLoaderListener} with
	 * the specified classes.
	 * @param configurationClasses {@code @Configuration} classes that will be used to
	 * configure the context
	 */
	protected AbstractHttpSessionApplicationInitializer(Class<?>... configurationClasses) {
		this.configurationClasses = configurationClasses;
	}

	@Override
	public void onStartup(ServletContext servletContext) {
		beforeSessionRepositoryFilter(servletContext);
		if (this.configurationClasses != null) {
			AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
			rootAppContext.register(this.configurationClasses);
			servletContext.addListener(new ContextLoaderListener(rootAppContext));
		}
		insertSessionRepositoryFilter(servletContext);
		afterSessionRepositoryFilter(servletContext);
	}

	/**
	 * Registers the springSessionRepositoryFilter.
	 * @param servletContext the {@link ServletContext}
	 */
	private void insertSessionRepositoryFilter(ServletContext servletContext) {
		String filterName = DEFAULT_FILTER_NAME;
		DelegatingFilterProxy springSessionRepositoryFilter = new DelegatingFilterProxy(filterName);
		String contextAttribute = getWebApplicationContextAttribute();
		if (contextAttribute != null) {
			springSessionRepositoryFilter.setContextAttribute(contextAttribute);
		}
		registerFilter(servletContext, true, filterName, springSessionRepositoryFilter);
	}

	/**
	 * Inserts the provided {@link Filter}s before existing {@link Filter}s using default
	 * generated names, {@link #getSessionDispatcherTypes()}, and
	 * {@link #isAsyncSessionSupported()}.
	 * @param servletContext the {@link ServletContext} to use
	 * @param filters the {@link Filter}s to register
	 */
	protected final void insertFilters(ServletContext servletContext, Filter... filters) {
		registerFilters(servletContext, true, filters);
	}

	/**
	 * Inserts the provided {@link Filter}s after existing {@link Filter}s using default
	 * generated names, {@link #getSessionDispatcherTypes()}, and
	 * {@link #isAsyncSessionSupported()}.
	 * @param servletContext the {@link ServletContext} to use
	 * @param filters the {@link Filter}s to register
	 */
	protected final void appendFilters(ServletContext servletContext, Filter... filters) {
		registerFilters(servletContext, false, filters);
	}

	/**
	 * Registers the provided {@link Filter}s using default generated names,
	 * {@link #getSessionDispatcherTypes()}, and {@link #isAsyncSessionSupported()}.
	 * @param servletContext the {@link ServletContext} to use
	 * @param insertBeforeOtherFilters if true, will insert the provided {@link Filter}s
	 * before other {@link Filter}s. Otherwise, will insert the {@link Filter}s after
	 * other {@link Filter}s.
	 * @param filters the {@link Filter}s to register
	 */
	private void registerFilters(ServletContext servletContext, boolean insertBeforeOtherFilters, Filter... filters) {
		Assert.notEmpty(filters, "filters cannot be null or empty");

		for (Filter filter : filters) {
			if (filter == null) {
				throw new IllegalArgumentException("filters cannot contain null values. Got " + Arrays.asList(filters));
			}
			String filterName = Conventions.getVariableName(filter);
			registerFilter(servletContext, insertBeforeOtherFilters, filterName, filter);
		}
	}

	/**
	 * Registers the provided filter using the {@link #isAsyncSessionSupported()} and
	 * {@link #getSessionDispatcherTypes()}.
	 * @param servletContext the servlet context
	 * @param insertBeforeOtherFilters should this Filter be inserted before or after
	 * other {@link Filter}
	 * @param filterName the filter name
	 * @param filter the filter
	 */
	private void registerFilter(ServletContext servletContext, boolean insertBeforeOtherFilters, String filterName,
			Filter filter) {
		Dynamic registration = servletContext.addFilter(filterName, filter);
		if (registration == null) {
			throw new IllegalStateException("Duplicate Filter registration for '" + filterName
					+ "'. Check to ensure the Filter is only configured once.");
		}
		registration.setAsyncSupported(isAsyncSessionSupported());
		EnumSet<DispatcherType> dispatcherTypes = getSessionDispatcherTypes();
		registration.addMappingForUrlPatterns(dispatcherTypes, !insertBeforeOtherFilters, "/*");
	}

	/**
	 * Returns the {@link DelegatingFilterProxy#getContextAttribute()} or null if the
	 * parent {@link ApplicationContext} should be used. The default behavior is to use
	 * the parent {@link ApplicationContext}.
	 *
	 * <p>
	 * If {@link #getDispatcherWebApplicationContextSuffix()} is non-null the
	 * {@link WebApplicationContext} for the Dispatcher will be used. This means the child
	 * {@link ApplicationContext} is used to look up the springSessionRepositoryFilter
	 * bean.
	 * </p>
	 * @return the {@link DelegatingFilterProxy#getContextAttribute()} or null if the
	 * parent {@link ApplicationContext} should be used
	 */
	private String getWebApplicationContextAttribute() {
		String dispatcherServletName = getDispatcherWebApplicationContextSuffix();
		if (dispatcherServletName == null) {
			return null;
		}
		return SERVLET_CONTEXT_PREFIX + dispatcherServletName;
	}

	/**
	 * Return the {@code <servlet-name>} to use the DispatcherServlet's
	 * {@link WebApplicationContext} to find the {@link DelegatingFilterProxy} or null to
	 * use the parent {@link ApplicationContext}.
	 *
	 * <p>
	 * For example, if you are using AbstractDispatcherServletInitializer or
	 * AbstractAnnotationConfigDispatcherServletInitializer and using the provided Servlet
	 * name, you can return "dispatcher" from this method to use the DispatcherServlet's
	 * {@link WebApplicationContext}.
	 * </p>
	 * @return the {@code <servlet-name>} of the DispatcherServlet to use its
	 * {@link WebApplicationContext} or null (default) to use the parent
	 * {@link ApplicationContext}.
	 */
	protected String getDispatcherWebApplicationContextSuffix() {
		return null;
	}

	/**
	 * Invoked before the springSessionRepositoryFilter is added.
	 * @param servletContext the {@link ServletContext}
	 */
	protected void beforeSessionRepositoryFilter(ServletContext servletContext) {

	}

	/**
	 * Invoked after the springSessionRepositoryFilter is added.
	 * @param servletContext the {@link ServletContext}
	 */
	protected void afterSessionRepositoryFilter(ServletContext servletContext) {

	}

	/**
	 * Get the {@link DispatcherType} for the springSessionRepositoryFilter.
	 * @return the {@link DispatcherType} for the filter
	 */
	protected EnumSet<DispatcherType> getSessionDispatcherTypes() {
		return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
	}

	/**
	 * Determine if the springSessionRepositoryFilter should be marked as supporting
	 * asynch. Default is true.
	 * @return true if springSessionRepositoryFilter should be marked as supporting asynch
	 */
	protected boolean isAsyncSessionSupported() {
		return true;
	}

}
