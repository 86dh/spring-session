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

package org.springframework.session.data.redis.config.annotation.web.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.RedisIndexedHttpSessionConfiguration.EnableRedisKeyspaceNotificationsInitializer;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RedisIndexedHttpSessionConfigurationMockTests {

	@Mock(strictness = Mock.Strictness.LENIENT)
	RedisConnectionFactory factory;

	@Mock
	RedisConnection connection;

	@BeforeEach
	void setup() {
		given(this.factory.getConnection()).willReturn(this.connection);
	}

	@Test
	void enableRedisKeyspaceNotificationsInitializerAfterPropertiesSetWhenNoOpThenNoInteractionWithConnectionFactory() {
		EnableRedisKeyspaceNotificationsInitializer init = new EnableRedisKeyspaceNotificationsInitializer(this.factory,
				ConfigureRedisAction.NO_OP);

		init.afterPropertiesSet();

		verifyNoMoreInteractions(this.factory);
	}

	@Test
	void enableRedisKeyspaceNotificationsInitializerAfterPropertiesSetWhenExceptionThenCloseConnection() {
		ConfigureRedisAction action = mock(ConfigureRedisAction.class);
		willThrow(new RuntimeException()).given(action).configure(this.connection);

		EnableRedisKeyspaceNotificationsInitializer init = new EnableRedisKeyspaceNotificationsInitializer(this.factory,
				action);

		try {
			init.afterPropertiesSet();
			failBecauseExceptionWasNotThrown(Throwable.class);
		}
		catch (Throwable success) {
		}

		verify(this.connection).close();
	}

	@Test
	void enableRedisKeyspaceNotificationsInitializerAfterPropertiesSetWhenNoExceptionThenCloseConnection() {
		ConfigureRedisAction action = mock(ConfigureRedisAction.class);

		EnableRedisKeyspaceNotificationsInitializer init = new EnableRedisKeyspaceNotificationsInitializer(this.factory,
				action);

		init.afterPropertiesSet();

		verify(this.connection).close();
	}

}
