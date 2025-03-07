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

package ldbc.finbench.datagen.generation.dictionary;

import java.util.Random;

public class NumbersGenerator {

    // TODO: add more
    private String[] bankCode = {"001", "100", "102", "103", "104", "105", "301", "302", "303", "304", "305", "306",
        "307", "308", "309",};

    // TODO: add more
    private String[] districtCode =
        {"1100", "1200", "3700", "2100", "1400", "4100", "2200", "2300", "6100", "6200", "6300",
            "6400", "6500", "4600", "8100", "8200", "2900", "5000", "4400", "4500", "4300", "4200", "3200", "3300"};

    public NumbersGenerator() {
    }

    public String generatePhonenum(Random random) {
        return String.format("%03d", random.nextInt(1000))
            + "-" + String.format("%04d", random.nextInt(10000));
    }

    public String generateOrdernum(Random random) {
        return bankCode[random.nextInt(bankCode.length)]
            + districtCode[random.nextInt(districtCode.length)]
            + String.format("%04d", random.nextInt(1000))
            + String.format("%04d", random.nextInt(10000));
    }
}
