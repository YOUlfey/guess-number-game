package com.github.youlfey.guess.the.number.game.util;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.list;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;


public class HttpUtils {

    public static HttpHeaders getHttpHeadersFromHttpServletRequest(HttpServletRequest request) {
        return list(request.getHeaderNames())
                .stream()
                .collect(toMap(identity(),
                        h -> list(request.getHeaders(h)),
                        (oldValue, newValue) -> concat(oldValue.stream(), newValue.stream()).collect(toList()),
                        HttpHeaders::new
                ));
    }
}
