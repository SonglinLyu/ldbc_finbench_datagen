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

public class Bucket {

    private double min;
    private double max;

    //    public static List<Bucket> bucketizeHistogram(List<Pair<Integer, Integer>> histogram, int numBuckets) {
    //
    //        List<Bucket> buckets = new ArrayList<>();
    //        int population = 0;
    //        int numEdges = 0;
    //        for (Pair<Integer, Integer> i : histogram) {
    //            population += i.getValue();
    //            numEdges += i.getValue() * i.getKey();
    //        }
    //        numEdges /= 2;
    //
    //
    //        int avgDegreeAt1B = 200;
    //        int avgDegree = numEdges / population;
    //        double acoeff = Math.log(avgDegreeAt1B) / Math.log(1000000000);
    //        double bcoeff = (acoeff - (Math.log(avgDegree) / Math.log(population))) / Math.log10(population);
    //
    //        int targetMean = (int) Math.round(Math.pow(DatagenParams.numAccounts, (acoeff - bcoeff * Math
    //                .log10(DatagenParams.numAccounts))));
    //        System.out.println("Distribution mean degree: " + avgDegree + " Distribution target mean " + targetMean);
    //        int bucketSize = (int) (Math.ceil(population / (double) (numBuckets)));
    //        int currentHistogramIndex = 0;
    //        int currentHistogramLeft = histogram.get(currentHistogramIndex).getValue();
    //        for (int i = 0; i < numBuckets && (currentHistogramIndex < histogram.size()); ++i) {
    //            int currentBucketCount = 0;
    //            int min = population;
    //            int max = 0;
    //            while (currentBucketCount < bucketSize && currentHistogramIndex < histogram.size()) {
    //                int degree = histogram.get(currentHistogramIndex).getKey();
    //                min = degree < min ? degree : min;
    //                max = degree > max ? degree : max;
    //                if ((bucketSize - currentBucketCount) > currentHistogramLeft) {
    //                    currentBucketCount += currentHistogramLeft;
    //                    currentHistogramIndex++;
    //                    if (currentHistogramIndex < histogram.size()) {
    //                        currentHistogramLeft = histogram.get(currentHistogramIndex).getValue();
    //                    }
    //                } else {
    //                    currentHistogramLeft -= (bucketSize - currentBucketCount);
    //                    currentBucketCount = bucketSize;
    //                }
    //            }
    //            min = (int) (min * targetMean / (double) avgDegree);
    //            max = (int) (max * targetMean / (double) avgDegree);
    //            buckets.add(new Bucket(min, max));
    //        }
    //        return buckets;
    //    }


    public Bucket(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double min() {
        return min;
    }

    public void min(double min) {
        this.min = min;
    }

    public double max() {
        return max;
    }

    public void max(double max) {
        this.max = max;
    }
}
