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

package org.springframework.session.web.socket.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.web.socket.events.SessionConnectEvent;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketConnectHandlerDecoratorFactoryTests {

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	WebSocketHandler delegate;

	@Mock
	WebSocketSession session;

	@Captor
	ArgumentCaptor<SessionConnectEvent> event;

	private WebSocketConnectHandlerDecoratorFactory factory;

	@BeforeEach
	void setup() {
		this.factory = new WebSocketConnectHandlerDecoratorFactory(this.eventPublisher);
	}

	@Test
	void constructorNullEventPublisher() {
		assertThatIllegalArgumentException().isThrownBy(() -> new WebSocketConnectHandlerDecoratorFactory(null))
			.withMessage("eventPublisher cannot be null");
	}

	@Test
	void decorateAfterConnectionEstablished() throws Exception {
		WebSocketHandler decorated = this.factory.decorate(this.delegate);

		decorated.afterConnectionEstablished(this.session);

		verify(this.eventPublisher).publishEvent(this.event.capture());
		assertThat(this.event.getValue().getWebSocketSession()).isSameAs(this.session);
	}

	@Test
	void decorateAfterConnectionEstablishedEventError() throws Exception {
		WebSocketHandler decorated = this.factory.decorate(this.delegate);
		willThrow(new IllegalStateException("Test throw on publishEvent")).given(this.eventPublisher)
			.publishEvent(any(ApplicationEvent.class));

		decorated.afterConnectionEstablished(this.session);

		verify(this.eventPublisher).publishEvent(any(SessionConnectEvent.class));
	}

}
