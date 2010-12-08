/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.http.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default {@link HeaderMapper} implementation for HTTP.
 * 
 * @author Mark Fisher
 * @author Jeremy Grelle
 * @since 2.0
 */
public class DefaultHttpHeaderMapper implements HeaderMapper<HttpHeaders> {

	public static final String USER_DEFINED_HEADER_PREFIX = "X-";

	private static final String ACCEPT = "Accept";

	private static final String ACCEPT_CHARSET = "Accept-Charset";

	private static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static final String ACCEPT_LANGUAGE = "Accept-Language";

	private static final String ACCEPT_RANGES = "Accept-Ranges";

	private static final String AGE = "Age";

	private static final String ALLOW = "Allow";

	private static final String AUTHORIZATION = "Authorization";

	private static final String CACHE_CONTROL = "Cache-Control";

	private static final String CONNECTION = "Connection";

	private static final String CONTENT_ENCODING = "Content-Encoding";

	private static final String CONTENT_LANGUAGE = "Content-Language";

	private static final String CONTENT_LENGTH = "Content-Length";

	private static final String CONTENT_LOCATION = "Content-Location";

	private static final String CONTENT_MD5 = "Content-MD5";

	private static final String CONTENT_RANGE = "Content-Range";

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String COOKIE = "Cookie";

	private static final String DATE = "Date";

	private static final String ETAG = "ETag";

	private static final String EXPECT = "Expect";

	private static final String EXPIRES = "Expires";

	private static final String FROM = "From";

	private static final String HOST = "Host";

	private static final String IF_MATCH = "If-Match";

	private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	private static final String IF_NONE_MATCH = "If-None-Match";

	private static final String IF_RANGE = "If-Range";

	private static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

	private static final String LAST_MODIFIED = "Last-Modified";

	private static final String LOCATION = "Location";

	private static final String MAX_FORWARDS = "Max-Forwards";

	private static final String PRAGMA = "Pragma";

	private static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

	private static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

	private static final String RANGE = "Range";

	private static final String REFERER = "Referer";

	private static final String REFRESH = "Refresh";

	private static final String RETRY_AFTER = "Retry-After";

	private static final String SERVER = "Server";

	private static final String SET_COOKIE = "Set-Cookie";

	private static final String TE = "TE";

	private static final String TRAILER = "Trailer";

	private static final String TRANSFER_ENCODING = "Transfer-Encoding";

	private static final String UPGRADE = "Upgrade";

	private static final String USER_AGENT = "User-Agent";

	private static final String VARY = "Vary";

	private static final String VIA = "Via";

	private static final String WARNING = "Warning";

	private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	private static final String[] HTTP_REQUEST_HEADER_NAMES = new String[] {
			ACCEPT,
			ACCEPT_CHARSET,
			ACCEPT_ENCODING,
			ACCEPT_LANGUAGE,
			ACCEPT_RANGES,
			AUTHORIZATION,
			CACHE_CONTROL,
			CONNECTION,
			CONTENT_LENGTH,
			CONTENT_TYPE,
			COOKIE,
			DATE,
			EXPECT,
			FROM,
			HOST,
			IF_MATCH,
			IF_MODIFIED_SINCE,
			IF_NONE_MATCH,
			IF_RANGE,
			IF_UNMODIFIED_SINCE,
			MAX_FORWARDS,
			PRAGMA,
			PROXY_AUTHORIZATION,
			RANGE,
			REFERER,
			TE,
			UPGRADE,
			USER_AGENT,
			VIA,
			WARNING
	};

	private static String[] HTTP_RESPONSE_HEADER_NAMES = new String[] {
			ACCEPT_RANGES,
			AGE,
			ALLOW,
			CACHE_CONTROL,
			CONTENT_ENCODING,
			CONTENT_LANGUAGE,
			CONTENT_LENGTH,
			CONTENT_LOCATION,
			CONTENT_MD5,
			CONTENT_RANGE,
			CONTENT_TYPE,
			DATE,
			ETAG,
			EXPIRES,
			LAST_MODIFIED,
			LOCATION,
			PRAGMA,
			PROXY_AUTHENTICATE,
			REFRESH,
			RETRY_AFTER,
			SERVER,
			SET_COOKIE,
			TRAILER,
			TRANSFER_ENCODING,
			VARY,
			VIA,
			WARNING,
			WWW_AUTHENTICATE
	};


	private volatile String[] outboundHeaderNames = new String[0];

	private volatile String[] inboundHeaderNames = new String[0];


	/**
	 * Provide the header names that should be mapped to an HTTP request (for outbound adapters)
	 * or HTTP response (for inbound adapters) from a Spring Integration Message's headers.
	 * <p>
	 * Any non-standard headers will be prefixed by {@value #USER_DEFINED_HEADER_PREFIX} if not already.
	 */
	public void setOutboundHeaderNames(String[] outboundHeaderNames) {
		this.outboundHeaderNames = (outboundHeaderNames != null) ? outboundHeaderNames : new String[0];
	}

	/**
	 * Provide the header names that should be mapped from an HTTP request (for inbound adapters)
	 * or HTTP response (for outbound adapters) to a Spring Integration Message's headers.
	 * <p>
	 * This will match the header name directly or, for non-standard HTTP headers, it will match
	 * the header name prefixed by {@value #USER_DEFINED_HEADER_PREFIX}. 
	 */
	public void setInboundHeaderNames(String[] inboundHeaderNames) {
		this.inboundHeaderNames = (inboundHeaderNames != null) ? inboundHeaderNames : new String[0];
	}

	/**
	 * Map from the integration MessageHeaders to an HttpHeaders instance.
	 * Depending on which type of adapter is using this mapper, the HttpHeaders might be
	 * for an HTTP request (outbound adapter) or for an HTTP response (inbound adapter). 
	 */
	public void fromHeaders(MessageHeaders headers, HttpHeaders target) {
		for (String name : this.outboundHeaderNames) {
			Object value = headers.get(name);
			if (value != null) {
				if (!ObjectUtils.containsElement(HTTP_REQUEST_HEADER_NAMES, name) && !ObjectUtils.containsElement(HTTP_RESPONSE_HEADER_NAMES, name)) {
					// prefix the user-defined header names if not already prefixed
					name = name.startsWith(USER_DEFINED_HEADER_PREFIX) ? name : USER_DEFINED_HEADER_PREFIX + name;
				}
				this.setHttpHeader(target, name, value);
			}
		}
	}

	/**
	 * Map from an HttpHeaders instance to integration MessageHeaders.
	 * Depending on which type of adapter is using this mapper, the HttpHeaders might be
	 * from an HTTP request (inbound adapter) or from an HTTP response (outbound adapter). 
	 */
	public Map<String, ?> toHeaders(HttpHeaders source) {
		Map<String, Object> target = new HashMap<String, Object>();
		for (String name : this.inboundHeaderNames) {
			if (!ObjectUtils.containsElement(HTTP_REQUEST_HEADER_NAMES, name) && !ObjectUtils.containsElement(HTTP_RESPONSE_HEADER_NAMES, name)) {
				String prefixedName = name.startsWith(USER_DEFINED_HEADER_PREFIX) ? name
						: USER_DEFINED_HEADER_PREFIX + name;
				Object value = source.containsKey(prefixedName) ? this.getHttpHeader(source, prefixedName) : this.getHttpHeader(source, name);
				if (value != null) {
					this.setMessageHeader(target, name, value);
				}
			}
			else {
				Object value = this.getHttpHeader(source, name);
				if (value != null) {
					this.setMessageHeader(target, name, value);
				}
			}
		}
		return target;
	}

	private void setHttpHeader(HttpHeaders target, String name, Object value) {
		if (ACCEPT.equals(name)) {
			if (value instanceof Collection<?>) {
				Collection<?> values = (Collection<?>) value;
				if (!CollectionUtils.isEmpty(values)) {
					List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
					for (Object type : values) {
						if (type instanceof MediaType) {
							acceptableMediaTypes.add((MediaType) type);
						}
						else if (type instanceof String) {
							acceptableMediaTypes.addAll(MediaType.parseMediaTypes((String) type));
						}
						else {
							Class<?> clazz = (type != null) ? type.getClass() : null;
							throw new IllegalArgumentException(
									"Expected MediaType or String value for 'Accept' header value, but received: " + clazz);
						}
					}
					target.setAccept(acceptableMediaTypes);
				}
			}
			else if (value instanceof MediaType) {
				target.setAccept(Collections.singletonList((MediaType) value));
			}
			else if (value instanceof String[]) {
				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
				for (String next : (String[]) value) {
					acceptableMediaTypes.add(MediaType.parseMediaType(next));
				}
				target.setAccept(acceptableMediaTypes);
			}
			else if (value instanceof String) {
				target.setAccept(MediaType.parseMediaTypes((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected MediaType or String value for 'Accept' header value, but received: " + clazz);
			}
		}
		else if (ACCEPT_CHARSET.equals(name)) {
			if (value instanceof Collection<?>) {
				Collection<?> values = (Collection<?>) value;
				if (!CollectionUtils.isEmpty(values)) {
					List<Charset> acceptableCharsets = new ArrayList<Charset>();
					for (Object charset : values) {
						if (charset instanceof Charset) {
							acceptableCharsets.add((Charset) charset);
						}
						else if (charset instanceof String) {
							acceptableCharsets.add(Charset.forName((String) charset));
						}
						else {
							Class<?> clazz = (charset != null) ? charset.getClass() : null;
							throw new IllegalArgumentException(
									"Expected Charset or String value for 'Accept-Charset' header value, but received: " + clazz);
						}
					}
					target.setAcceptCharset(acceptableCharsets);
				}
			}
			else if (value instanceof Charset) {
				target.setAcceptCharset(Collections.singletonList((Charset) value));
			}
			else if (value instanceof String) {
				target.setAcceptCharset(Collections.singletonList(Charset.forName((String) value)));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Charset or String value for 'Accept-Charset' header value, but received: " + clazz);
			}
		}
		else if (ALLOW.equals(name)) {
			if (value instanceof Collection<?>) {
				Collection<?> values = (Collection<?>) value;
				if (!CollectionUtils.isEmpty(values)) {
					Set<HttpMethod> allowedMethods = new HashSet<HttpMethod>();
					for (Object method : values) {
						if (method instanceof HttpMethod) {
							allowedMethods.add((HttpMethod) method);
						}
						else if (method instanceof String) {
							allowedMethods.add(HttpMethod.valueOf((String) method));
						}
						else {
							Class<?> clazz = (method != null) ? method.getClass() : null;
							throw new IllegalArgumentException(
									"Expected HttpMethod or String value for 'Allow' header value, but received: " + clazz);
						}
					}
					target.setAllow(allowedMethods);
				}
			}
			else {
				if (value instanceof HttpMethod) {
					target.setAllow(Collections.singleton((HttpMethod) value));
				}
				else if (value instanceof String || value instanceof String[]) {
					String[] values = (value instanceof String[]) ? (String[]) value
							: StringUtils.commaDelimitedListToStringArray((String) value);
					Set<HttpMethod> allowedMethods = new HashSet<HttpMethod>();
					for (String next : values) {
						allowedMethods.add(HttpMethod.valueOf(next));
					}
					target.setAllow(allowedMethods);
				}
				else {
					Class<?> clazz = (value != null) ? value.getClass() : null;
					throw new IllegalArgumentException(
							"Expected HttpMethod or String value for 'Allow' header value, but received: " + clazz);	
				}
			}
		}
		else if (CACHE_CONTROL.equals(name)) {
			if (value instanceof String) {
				target.setCacheControl((String) value);
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected String value for 'Cache-Control' header value, but received: " + clazz);				
			}
		}
		else if (CONTENT_LENGTH.equals(name)) {
			if (value instanceof Number) {
				target.setContentLength(((Number) value).longValue());
			}
			else if (value instanceof String) {
				target.setContentLength(Long.parseLong((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Number or String value for 'Content-Length' header value, but received: " + clazz);
			}
		}
		else if (CONTENT_TYPE.equals(name)) {
			if (value instanceof MediaType) {
				target.setContentType((MediaType) value);
			}
			else if (value instanceof String) {
				target.setContentType(MediaType.parseMediaType((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected MediaType or String value for 'Content-Type' header value, but received: " + clazz);
			}
		}
		else if (DATE.equals(name)) {
			if (value instanceof Date) {
				target.setDate(((Date) value).getTime());
			}
			else if (value instanceof Number) {
				target.setDate(((Number) value).longValue());
			}
			else if (value instanceof String) {
				target.setDate(Long.parseLong((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Date, Number, or String value for 'Date' header value, but received: " + clazz);
			}
		}
		else if (ETAG.equals(name)) {
			if (value instanceof String) {
				target.setETag((String) value);
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected String value for 'ETag' header value, but received: " + clazz);
			}
		}
		else if (EXPIRES.equals(name)) {
			if (value instanceof Date) {
				target.setExpires(((Date) value).getTime());
			}
			else if (value instanceof Number) {
				target.setExpires(((Number) value).longValue());
			}
			else if (value instanceof String) {
				target.setExpires(Long.parseLong((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Date, Number, or String value for 'Expires' header value, but received: " + clazz);
			}
		}
		else if (IF_MODIFIED_SINCE.equals(name)) {
			if (value instanceof Date) {
				target.setIfModifiedSince(((Date) value).getTime());
			}
			else if (value instanceof Number) {
				target.setIfModifiedSince(((Number) value).longValue());
			}
			else if (value instanceof String) {
				target.setIfModifiedSince(Long.parseLong((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Date, Number, or String value for 'If-Modified-Since' header value, but received: " + clazz);
			}
		}
		else if (IF_NONE_MATCH.equals(name)) {
			if (value instanceof String) {
				target.setIfNoneMatch((String) value);
			}
			else if (value instanceof Collection) {
				Collection<?> values = (Collection<?>) value;
				if (!CollectionUtils.isEmpty(values)) {
					List<String> ifNoneMatchList = new ArrayList<String>();
					for (Object next : values) {
						if (next instanceof String) {
							ifNoneMatchList.add((String) next);
						}
						else {
							Class<?> clazz = (next != null) ? next.getClass() : null;
							throw new IllegalArgumentException(
									"Expected String value for 'If-None-Match' header value, but received: " + clazz);
						}
					}
					target.setIfNoneMatch(ifNoneMatchList);
				}
			}
		}
		else if (LAST_MODIFIED.equals(name)) {
			if (value instanceof Date) {
				target.setLastModified(((Date) value).getTime());
			}
			else if (value instanceof Number) {
				target.setLastModified(((Number) value).longValue());
			}
			else if (value instanceof String) {
				target.setLastModified(Long.parseLong((String) value));
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected Date, Number, or String value for 'Last-Modified' header value, but received: " + clazz);
			}
		}
		else if (LOCATION.equals(name)) {
			if (value instanceof URI) {
				target.setLocation((URI) value);
			}
			else if (value instanceof String) {
				try {
					target.setLocation(new URI((String) value));
				}
				catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected URI or String value for 'Location' header value, but received: " + clazz);
			}
		}
		else if (PRAGMA.equals(name)) {
			if (value instanceof String) {
				target.setPragma((String) value);
			}
			else {
				Class<?> clazz = (value != null) ? value.getClass() : null;
				throw new IllegalArgumentException(
						"Expected String value for 'Pragma' header value, but received: " + clazz);
			}
		}
		else if (value instanceof String) {
			target.set(name, (String) value);
		}
		else if (value instanceof String[]) {
			for (String next : (String[]) value) {
				target.add(name, next);
			}
		}
		else if (value instanceof Iterable<?>) {
			for (Object next : (Iterable<?>) value) {
				if (next instanceof String) {
					target.add(name, (String) next);
				}
			}
		}
	}

	private Object getHttpHeader(HttpHeaders source, String name) {
		if (ACCEPT.equals(name)) {
			return source.getAccept();
		}
		else if (ACCEPT_CHARSET.equals(name)) {
			return source.getAcceptCharset();
		}
		else if (ALLOW.equals(name)) {
			return source.getAllow();
		}
		else if (CACHE_CONTROL.equals(name)) {
			String cacheControl = source.getCacheControl();
			return (StringUtils.hasText(cacheControl)) ? cacheControl : null;
		}
		else if (CONTENT_LENGTH.equals(name)) {
			long contentLength = source.getContentLength();
			return (contentLength > -1) ? contentLength : null;
		}
		else if (CONTENT_TYPE.equals(name)) {
			return source.getContentType();
		}
		else if (DATE.equals(name)) {
			long date = source.getDate();
			return (date > -1) ? date : null;
		}
		else if (ETAG.equals(name)) {
			String eTag = source.getETag();
			return (StringUtils.hasText(eTag)) ? eTag : null;
		}
		else if (EXPIRES.equals(name)) {
			long expires = source.getExpires();
			return (expires > -1) ? expires : null;
		}
		else if (IF_NONE_MATCH.equals(name)) {
			return source.getIfNoneMatch();
		}
		else if (IF_UNMODIFIED_SINCE.equals(name)) {
			long unmodifiedSince = source.getIfNotModifiedSince();
			return (unmodifiedSince > -1) ? unmodifiedSince : null;
		}
		else if (LAST_MODIFIED.equals(name)) {
			long lastModified = source.getLastModified();
			return (lastModified > -1) ? lastModified : null;
		}
		else if (LOCATION.equals(name)) {
			return source.getLocation();
		}
		else if (PRAGMA.equals(name)) {
			String pragma = source.getPragma();
			return (StringUtils.hasText(pragma)) ? pragma : null;
		}
		return source.get(name);
	}

	private void setMessageHeader(Map<String, Object> target, String name, Object value) {
		if (ObjectUtils.isArray(value)) {
			Object[] values = ObjectUtils.toObjectArray(value);
			if (!ObjectUtils.isEmpty(values)) {
				if (values.length == 1) {
					target.put(name, values);
				}
				else {
					target.put(name, values[0]);
				}
			}
		}
		else if (value instanceof Collection<?>) {
			Collection<?> values = (Collection<?>) value;
			if (!CollectionUtils.isEmpty(values)) {
				if (values.size() == 1) {
					target.put(name, values.iterator().next());
				}
				else {
					target.put(name, values);
				}
			}
		}
		else if (value != null) {
			target.put(name, value);
		}
	}


	/**
	 * Factory method for creating a basic outbound mapper instance.
	 * This will map all standard HTTP request headers when sending an HTTP request, 
	 * and it will map all standard HTTP response headers when receiving an HTTP response.
	 */
	public static DefaultHttpHeaderMapper outboundMapper() {
		DefaultHttpHeaderMapper mapper = new DefaultHttpHeaderMapper();
		mapper.setOutboundHeaderNames(HTTP_REQUEST_HEADER_NAMES);
		mapper.setInboundHeaderNames(HTTP_RESPONSE_HEADER_NAMES);
		return mapper;
	}

	/**
	 * Factory method for creating a basic inbound mapper instance.
	 * This will map all standard HTTP request headers when receiving an HTTP request,
	 * and it will map all standard HTTP response headers when sending an HTTP response.
	 */
	public static DefaultHttpHeaderMapper inboundMapper() {
		DefaultHttpHeaderMapper mapper = new DefaultHttpHeaderMapper();
		mapper.setInboundHeaderNames(HTTP_REQUEST_HEADER_NAMES);
		mapper.setOutboundHeaderNames(HTTP_RESPONSE_HEADER_NAMES);
		return mapper;
	}

}
