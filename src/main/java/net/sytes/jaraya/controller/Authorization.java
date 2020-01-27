package net.sytes.jaraya.controller;

import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.CoreException;
import net.sytes.jaraya.properties.Properties;
import net.sytes.jaraya.security.Base64;
import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class Authorization implements Filter {
    @Override
    public void handle(Request request, Response response) throws CoreException {
        String auth = request.headers("Authorization");
        if (auth == null || auth.isEmpty()) {
            halt(401, "Unauthorize");
        }

        String decAuth = Base64.decode(auth.replaceAll("Basic ", ""));
        String[] credentials = decAuth.split(":");
        if (credentials.length != 2) {
            halt(401, "Unauthorize");
        }

        if (!credentials[0].contentEquals(Properties.get(Property.USER.name())) ||
                !credentials[1].contentEquals(Properties.get(Property.PASSWORD.name()))) {
            halt(401, "Bad Credentials");
        }

    }
}
