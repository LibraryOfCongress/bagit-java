/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bagit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation conveys following information:
 * <ul>
 * <li>The API is fairly new and we would appreciate your feedback. For example, what are you missing from the API
 * to solve your use case.</li>
 * <li>The API might change.
 * The chance for that is small because we care great deal for the initial design.
 * The incubating API might change based on the feedback from the community in order to make the API most useful for the users.
 * </li>
 * <li>
 * For types or methods that are not yet released it means the API is <strong>work in progress</strong>
 * and can change before release.
 * </li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Incubating {
}