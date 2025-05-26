package com.test.telegram.controllers.auth;

import com.test.telegram.DTOs.AuthRequest;
import com.test.telegram.repositories.UserRepository;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.nio.charset.StandardCharsets;
import java.util.*;


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
// счетчик потраченных часов: 4

@RestController
public class AuthController {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request) {

        String dataFromRequest = request.getInitData();
        Map<String, String> params = parseInitData(dataFromRequest);






        return ResponseEntity.ok("Smthng");
    }


    private Map<String, String> parseInitData(String dataFromRequest) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(dataFromRequest, StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(pairs.toArray()));
        Map<String, String> params = new HashMap<>();
        for (NameValuePair pair : pairs) {
            params.put(pair.getName(), pair.getValue());
        }
        return params;
    }
}
