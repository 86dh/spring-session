/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.spring.gradle.convention;

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.sonarqube.gradle.SonarQubePlugin;

/**
 * @author Rob Winch
 */
public class SpringSamplePlugin extends AbstractSpringJavaPlugin {

    @Override
    public void additionalPlugins(Project project) {
        project.plugins.withType(SonarQubePlugin) {
            project.sonarqube.skipProject = true
        }
    }

	@Override
	protected void initialPlugins(Project project) {
		def versionCatalog = project.rootProject.extensions.getByType(VersionCatalogsExtension.class)
				.named("libs")
		def version = versionCatalog.findVersion("org-springframework-boot")
		version.ifPresent {
			def springBootVersion = it.displayName
			if (Utils.isSnapshot(springBootVersion)) {
				project.ext.forceMavenRepositories = 'snapshot'
			} else if (Utils.isMilestone(springBootVersion)) {
				project.ext.forceMavenRepositories = 'milestone'
			}
		}
	}
}
