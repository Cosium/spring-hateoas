/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.util.Assert;

/**
 *
 * Factory implementation to register creator functions to eventually create value from
 * {@link AffordanceModel.PropertyMetadata} to decouple the registration (via {@link HalFormsConfiguration}) from the consumption during
 * rendering.
 *
 * @author Réda Housni Alaoui
 */
class HalFormsValueFactory {

	private final Map<Class<?>, BiFunction<AffordanceModel.InputPayloadMetadata, AffordanceModel.PropertyCreationContext, Map<String, ? extends @Nullable Object>>> values;

	/**
	 * Creates a new, empty {@link HalFormsValueFactory}.
	 */
	public HalFormsValueFactory() {
		this.values = new HashMap<>();
	}

	/**
	 * Copy-constructor to keep {@link HalFormsValueFactory} immutable during registrations.
	 *
	 * @param values must not be {@literal null}.
	 */
	private HalFormsValueFactory(
			Map<Class<?>, BiFunction<AffordanceModel.InputPayloadMetadata, AffordanceModel.PropertyCreationContext, Map<String, ? extends @Nullable Object>>> values) {
		this.values = values;
	}

	/**
	 * Registers a {@link BiFunction} to create the values of the properties of the given type, keyed by property name,
	 * from its {@link AffordanceModel.InputPayloadMetadata} and {@link AffordanceModel.PropertyCreationContext}. It
	 * replaces any creator previously registered for the type.
	 *
	 * @param type must not be {@literal null}.
	 * @param creator must not be {@literal null}.
	 */
	HalFormsValueFactory withValues(Class<?> type,
			BiFunction<AffordanceModel.InputPayloadMetadata, AffordanceModel.PropertyCreationContext, Map<String, ? extends @Nullable Object>> creator) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(creator, "Creator function must not be null!");

		Map<Class<?>, BiFunction<AffordanceModel.InputPayloadMetadata, AffordanceModel.PropertyCreationContext, Map<String, ? extends @Nullable Object>>> values = new HashMap<>(
				this.values);
		values.put(type, creator);

		return new HalFormsValueFactory(values);
	}

	/**
	 * Returns the values, keyed by property name, of the properties of the given
	 * {@link AffordanceModel.InputPayloadMetadata}.
	 *
	 * @param payload must not be {@literal null}.
	 * @param context must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Map<String, ? extends @Nullable Object> getValues(AffordanceModel.InputPayloadMetadata payload,
			AffordanceModel.PropertyCreationContext context) {

		Assert.notNull(payload, "Payload metadata must not be null!");
		Assert.notNull(context, "Property creation context must not be null!");

		BiFunction<AffordanceModel.InputPayloadMetadata, AffordanceModel.PropertyCreationContext, Map<String, ? extends @Nullable Object>> function = values
				.get(payload.getType());

		return function == null ? Collections.emptyMap() : function.apply(payload, context);
	}
}
