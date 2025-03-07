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

package ldbc.finbench.datagen.generation.distribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowerLawActivityDeleteDistribution {

    private double[] minutes;
    private double[] distribution;
    private String distributionFile;

    public PowerLawActivityDeleteDistribution(String distributionFile) {
        this.distributionFile = distributionFile;

        this.minutes =
            new double[] {0, 0.5, 1, 5, 10, 20, 30, 40, 60, 120, 300, 1440, 2880, 4320, 5760, 7200, 8460, 10080};

    }

    public void initialize() {
        try {
            BufferedReader distributionBuffer = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(distributionFile), StandardCharsets.UTF_8));
            List<Double> temp = new ArrayList<>();
            String line;
            while ((line = distributionBuffer.readLine()) != null) {
                Double prob = Double.valueOf(line);
                temp.add(prob);
            }
            distribution = new double[temp.size()];
            int index = 0;
            for (Double item : temp) {
                distribution[index] = item;
                ++index;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double nextDouble(double prob, Random random) {

        double draw = 0;
        for (int i = 0; i < distribution.length; i++) {
            if (prob < distribution[i]) {
                double lower = minutes[i - 1];
                double upper = minutes[i];
                draw = lower + (upper - lower) * random.nextDouble();
                break;
            }
        }
        return draw;

    }


}
