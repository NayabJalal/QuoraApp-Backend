package com.quoraBackend.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

//Convert String into local Time and Date (helperFunc. can go)
@UtilityClass
public class CursorUtils {
    public static boolean isValidCursor(String cursor){
        if (cursor == null || cursor.isBlank()){
            return false;
        }
        try {
            LocalDateTime.parse(cursor);
            return true;
        }catch (DateTimeParseException e ){
            return false;
        }
    }
    public static LocalDateTime parseCursor(String cursor){
        if (!isValidCursor(cursor)){
            throw new IllegalArgumentException("Invalid Cursor timestamp: " +cursor);
        }
        return LocalDateTime.parse(cursor);
    }
}
