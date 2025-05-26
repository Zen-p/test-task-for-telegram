package com.test.telegram.controllers.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.telegram.DTOs.AuthRequest;
import com.test.telegram.entities.User;
import com.test.telegram.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/*
    Validating data received via the Mini App

    To validate data received via the Mini App,
    one should send the data from the Telegram.WebApp.initData field to
    the bot's backend. The data is a query string, which is composed of a
    series of field-value pairs.

    You can verify the integrity of the data received
    by comparing the received hash parameter with the hexadecimal
    representation of the HMAC-SHA-256 signature of the data-check-string with the
    secret key, which is the HMAC-SHA-256 signature of the bot's token with the
    constant string WebAppData used as a key.

    Data-check-string is a chain of all received fields,
    !!!

    sorted alphabetically

    !!!
    , in the format key=<value> with a line
    feed character ('\n', 0x0A) used as separator – e.g.,
    'auth_date=<auth_date>\nquery_id=<query_id>\nuser=<user>'.

    The full check might look like:

    data_check_string = ...
    secret_key = HMAC_SHA256(<bot_token>, "WebAppData")
    if (hex(HMAC_SHA256(data_check_string, secret_key)) == hash) {
     // data is from Telegram
    }

    To prevent the use of outdated data, you can additionally check
    the auth_date field, which contains a Unix timestamp of
    when it was received by the Mini App.

    Once validated, the data may be used on
    your server. Complex data types are represented as
    JSON-serialized objects.

*/

// тут, кажется, много времени уйдет
// счетчик потраченных часов: 8

// https://habr.com/ru/articles/850298/ - пометка для самого себя

@RestController
public class AuthController {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticate")
    @Transactional
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request, HttpSession session) {

        String dataFromRequest = request.getInitData();
        Map<String, String> params = parseInitData(dataFromRequest);

        String hashFromParams = params.remove("hash");

        boolean isValid = validateInitData(hashFromParams, params);

        if (isValid) {
            try {
                Map<String, Object> userMap = new ObjectMapper().readValue(params.get("user"), new TypeReference<Map<String, Object>>() {});
                Long userId = ((Number) userMap.get("id")).longValue();
                String firstName = (String) userMap.get("first_name");
                String lastName = (String) userMap.get("last_name");
                String username = (String) userMap.get("username");

                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setId(userId);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setUsername(username);
                } else {
                    if (!Objects.equals(user.getFirstName(), firstName) || !Objects.equals(user.getLastName(), lastName) || !Objects.equals(user.getUsername(), username)) {
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setUsername(username);
                    } else {
                        session.setAttribute("userId", userId);
                        return ResponseEntity.ok("Authenticated");
                    }
                }

                userRepository.save(user);
                session.setAttribute("userId", userId);
                return ResponseEntity.ok("Authenticated");
            } catch (ObjectOptimisticLockingFailureException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Concurrent modification detected");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error parsing user data");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid data");
        }

    }


    private Map<String, String> parseInitData(String dataFromRequest) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(dataFromRequest, StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (NameValuePair pair : pairs) {
            params.put(pair.getName(), pair.getValue());
        }
        return params;
    }

    private boolean validateInitData (String hash, Map<String, String> params) {

        ArrayList<String> sortedParams = new ArrayList<>(params.keySet());
        Collections.sort(sortedParams);

        String dataCheckString = sortedParams.stream()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.joining("\n"));

        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] secretKey = hmacSha256.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            Mac hmacSha256Data = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeyData = new SecretKeySpec(secretKey, "HmacSHA256");
            hmacSha256Data.init(secretKeyData);
            byte[] signature = hmacSha256Data.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(signature);

            return computedHash.equalsIgnoreCase(hash);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
