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

package org.springframework.session;

import java.util.UUID;

import org.springframework.lang.NonNull;

/**
 * A {@link SessionIdGenerator} that generates a random UUID to be used as the session id.
 *
 * @author Marcus da Coregio
 * @since 3.2
 */
public final class UuidSessionIdGenerator implements SessionIdGenerator {

	private static final UuidSessionIdGenerator INSTANCE = new UuidSessionIdGenerator();

	private UuidSessionIdGenerator() {
	}

	@Override
	@NonNull
	public String generate() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Returns the singleton instance of {@link UuidSessionIdGenerator}.
	 * @return the singleton instance of {@link UuidSessionIdGenerator}
	 */
	public static UuidSessionIdGenerator getInstance() {
		return INSTANCE;
	}

}
