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

package sample.web;

import java.security.Principal;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * {@link ControllerAdvice} to expose user related attributes.
 *
 * @author Rob Winch
 */
@ControllerAdvice
public class SecurityControllerAdvise {

	@ModelAttribute("currentUserName")
	String currentUser(Principal principal) {
		return (principal != null) ? principal.getName() : null;
	}

	@ModelAttribute("httpSession")
	HttpSession httpSession(HttpSession httpSession) {
		return httpSession;
	}

}
