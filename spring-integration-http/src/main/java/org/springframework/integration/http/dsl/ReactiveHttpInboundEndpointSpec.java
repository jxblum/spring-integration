/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.http.dsl;

import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.integration.http.inbound.ReactiveHttpInboundEndpoint;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

/**
 * The {@link HttpInboundEndpointSupportSpec} implementation for the {@link ReactiveHttpInboundEndpoint}.
 *
 * @author Artem Bilan
 *
 * @since 5.0
 */
public class ReactiveHttpInboundEndpointSpec
		extends HttpInboundEndpointSupportSpec<ReactiveHttpInboundEndpointSpec, ReactiveHttpInboundEndpoint> {

	ReactiveHttpInboundEndpointSpec(ReactiveHttpInboundEndpoint gateway, String... path) {
		super(gateway, path);
	}

	public ReactiveHttpInboundEndpointSpec codecConfigurer(ServerCodecConfigurer codecConfigurer) {
		this.target.setCodecConfigurer(codecConfigurer);
		return this;
	}

	public ReactiveHttpInboundEndpointSpec requestedContentTypeResolver(
			RequestedContentTypeResolver requestedContentTypeResolver) {

		this.target.setRequestedContentTypeResolver(requestedContentTypeResolver);
		return this;
	}

	public ReactiveHttpInboundEndpointSpec reactiveAdapterRegistry(ReactiveAdapterRegistry adapterRegistry) {
		this.target.setReactiveAdapterRegistry(adapterRegistry);
		return this;
	}

}
