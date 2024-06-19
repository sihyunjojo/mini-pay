package org.c4marathon.assignment.extractor;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

import static org.c4marathon.assignment.util.JwtConst.HEADER_AUTH;

@Component
public class AuthorizationExtractor {

    public String extract(HttpServletRequest request, String type) {
        Enumeration<String> headers = request.getHeaders(HEADER_AUTH);
        System.out.println(headers);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.toLowerCase().startsWith(type.toLowerCase())) {
                return value.substring(type.length()).trim();
            }
        }
        return Strings.EMPTY;
    }
}