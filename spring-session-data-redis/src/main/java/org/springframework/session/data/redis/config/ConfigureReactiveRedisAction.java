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

package org.springframework.session.data.redis.config;

import reactor.core.publisher.Mono;

import org.springframework.data.redis.connection.ReactiveRedisConnection;

/**
 * Allows specifying a strategy for configuring and validating Redis using a Reactive
 * connection.
 *
 * @author Marcus da Coregio
 * @since 3.3
 */
public interface ConfigureReactiveRedisAction {

	Mono<Void> configure(ReactiveRedisConnection connection);

	/**
	 * An implementation of {@link ConfigureReactiveRedisAction} that does nothing.
	 */
	ConfigureReactiveRedisAction NO_OP = (connection) -> Mono.empty();

}
