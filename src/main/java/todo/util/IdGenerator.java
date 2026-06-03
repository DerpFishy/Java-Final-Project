package todo.util;

import java.util.UUID;

public class IdGenerator {

    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
}