/*
 * Copyright © 2022 Linked Data Benchmark Council (info@ldbcouncil.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldbc.finbench.datagen.config;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

public class DatagenConfiguration implements Iterable<Map.Entry<String, String>>, Serializable {
    public final Map<String, String> map;

    public DatagenConfiguration(Map<String, String> map) {
        this.map = map;
    }

    public String get(String key) {
        return map.get(key);
    }

    public String get(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    public String getTrimmed(String name) {
        String value = this.get(name);
        return null == value ? null : value.trim();
    }

    private String getHexDigits(String value) {
        boolean negative = false;
        String str = value;
        String hexString;
        if (value.startsWith("-")) {
            negative = true;
            str = value.substring(1);
        }

        if (!str.startsWith("0x") && !str.startsWith("0X")) {
            return null;
        } else {
            hexString = str.substring(2);
            if (negative) {
                hexString = "-" + hexString;
            }

            return hexString;
        }
    }

    public int getInt(String name, int defaultValue) {
        String valueString = this.getTrimmed(name);
        if (valueString == null) {
            return defaultValue;
        } else {
            String hexString = this.getHexDigits(valueString);
            return hexString != null ? Integer.parseInt(hexString, 16) : Integer.parseInt(valueString);
        }
    }

    public double getDouble(String name, double defaultValue) {
        String valueString = this.getTrimmed(name);
        return valueString == null ? defaultValue : Double.parseDouble(valueString);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return this.map.entrySet().iterator();
    }

    public String getOutputDir() {
        return map.get("generator.outputDir");
    }

    public String getFormat() {
        return map.get("generator.format");
    }

    public String getPartition() {
        return map.get("spark.partition");
    }

    public void printConfig() {
        System.out.println("********* Configuration *********");
        map.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("*********************************");
    }
}
