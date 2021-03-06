package fr.lelouet.tools.webfetchers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import fr.lelouet.tools.holders.cache.URIBasedCache;
import fr.lelouet.tools.holders.interfaces.ObjHolder;
import fr.lelouet.tools.holders.interfaces.RWObjHolder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * provide static utility methods to represent a Web resource cache. Each cached
 * resource must be of the same type.
 * <p>
 * The schedule for resource update is based on the expires header, if present, or a
 * </p>
 *
 * @author glelouet
 *
 */
public class WebFetcher {

	private static final Logger log = LoggerFactory.getLogger(WebFetcher.class);

	private WebFetcher() {
	};

	//
	// basic helper function
	//

	/**
	 * create a mono for a web response, based on the uri and the previously
	 * received entity
	 *
	 * @param <T>
	 *          representation class of the resource fetched
	 * @param wc
	 *          web client, that is used to send the actual request and receive
	 *          it.
	 * @param uri
	 *          uri of the resource to fetch
	 * @param old
	 *          previously received entity
	 * @param retClass
	 *          the class that should be constructed from the response
	 * @return
	 */
	protected static <T> Mono<ResponseEntity<T>> makeMono(WebClient wc, String uri, ResponseEntity<T> old,
			Class<T> retClass) {
		RequestHeadersSpec<?> construct = wc.get().uri(uri);
		if (old != null) {
			if (old.getHeaders().getETag() != null) {
				construct.ifNoneMatch(old.getHeaders().getETag());
			}
			if (old.getHeaders().getLastModified() > -1) {
				construct.header(HttpHeaders.IF_MODIFIED_SINCE, "" + old.getHeaders().getLastModified());
			}
		}
		Mono<ResponseEntity<T>> ret = construct.retrieve().toEntity(retClass);
		ret = ret.retryWhen(Retry.backoff(4, Duration.ofSeconds(1)));
		ret = ret.onErrorResume(t -> {
			log.error("for uri " + uri, t);
			return Mono.justOrEmpty(null);
		});
		// ret = ret.doOnEach(s -> System.out.println("received " + s));
		return ret;
	}

	//
	// Synchronous calls
	//

	/**
	 * make a cache on a given type, for which each URI's resource is fetches
	 * synchronously in the executors calls.
	 *
	 * @param <Resource>
	 * @param <Hold>
	 * @param <RWHold>
	 * @param executor
	 * @param wc
	 * @param retClass
	 * @param init
	 * @param convert
	 * @param defaultDelay
	 * @return
	 */
	public static <
	Resource,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>
	> URIBasedCache<Resource, Hold> cacheSync(
			BiConsumer<Callable<?>, Long> executor,
			WebClient wc,
			Class<Resource> retClass,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert, long defaultDelay
			) {
		BiFunction<String, ResponseEntity<Resource>, ResponseEntity<Resource>> fetcher = (uri, old) -> webFetchSync(wc, uri,
				old, retClass);
		return new <ResponseEntity<Resource>, RWHold>URIBasedCache<Resource, Hold>(executor, init, convert, fetcher,
				WebFetcher::isReplace, WebFetcher::extract, WebFetcher.isRescheduler(defaultDelay), WebFetcher.rescheduler(defaultDelay));
	}

	public static <
	Resource,
	Hold extends ObjHolder<Resource>,
	RWHold extends RWObjHolder<Resource>
	> URIBasedCache<Resource, Hold> cacheSync(
			ScheduledExecutorService exec,
			WebClient wc,
			Class<Resource> retClass,
			Supplier<RWHold> init,
			Function<RWHold, Hold> convert, long defaultDelay
			) {
		return cacheSync((r, l) -> exec.schedule(r, l, TimeUnit.MILLISECONDS), wc, retClass, init, convert, defaultDelay);
	}

	protected static boolean isReplace(ResponseEntity<?> response) {
		return response == null || response.getStatusCode() == HttpStatus.OK;
	}

	protected static <Resource> Resource extract(ResponseEntity<Resource> response) {
		return response == null ? null : response.getBody();
	}

	protected static <Resource> Predicate<ResponseEntity<Resource>> isRescheduler(long default_delay_ms) {
		return response -> (response.getHeaders().getExpires() > -1 || default_delay_ms > -1);
	}

	protected static <Resource> ToLongFunction<ResponseEntity<Resource>> rescheduler(long defaultdelay) {
		return response -> {
			long expires = response.getHeaders().getExpires();
			if (expires > -1) {
				return expires - Instant.now().toEpochMilli();
			}
			return defaultdelay;
		};
	}

	protected static <T> ResponseEntity<T> webFetchSync(WebClient wc, String uri, ResponseEntity<T> old,
			Class<T> retClass) {
		ResponseEntity<T> ret = makeMono(wc, uri, old, retClass).block();
		return ret;
	}

	//
	// async
	//

	// TODO
	protected static <T> Mono<ResponseEntity<T>> webFetchAsync(WebClient wc, String uri, Mono<ResponseEntity<T>> old,
			Class<T> retClass) {
		Mono<ResponseEntity<T>> ret = makeMono(wc, uri, old == null ? null : old.block(), retClass);
		return ret;
	}

}
