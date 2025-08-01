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

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author jitendra on 5/3/16.
 */
@Controller
public class HomeController {

	@RequestMapping("/setValue")
	public String setValue(@RequestParam(name = "key", required = false) String key,
			@RequestParam(name = "value", required = false) String value, HttpServletRequest request, Model model) {
		if (!ObjectUtils.isEmpty(key) && !ObjectUtils.isEmpty(value)) {
			request.getSession().setAttribute(key, value);
		}
		model.addAttribute("sessionAttributeNames", Collections.list(request.getSession().getAttributeNames()));
		return "home";
	}

}
