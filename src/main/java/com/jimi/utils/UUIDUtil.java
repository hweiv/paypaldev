package com.jimi.utils;

import java.util.Random;
import java.util.UUID;

public class UUIDUtil {

   public static String getUUID() {
      UUID uuid = UUID.randomUUID();
      return uuid.toString().replace("-", "");
   }

   public static String getRandomSix() {
      return "" + (new Random()).nextInt(999999);
   }
}
