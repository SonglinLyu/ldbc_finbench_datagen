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

package ldbc.finbench.datagen.generation.events;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import ldbc.finbench.datagen.entities.edges.SignIn;
import ldbc.finbench.datagen.entities.nodes.Account;
import ldbc.finbench.datagen.entities.nodes.Medium;
import ldbc.finbench.datagen.generation.DatagenParams;
import ldbc.finbench.datagen.util.RandomGeneratorFarm;

public class SignInEvent implements Serializable {
    private final RandomGeneratorFarm randomFarm;
    private final Random randIndex;

    public SignInEvent() {
        randomFarm = new RandomGeneratorFarm();
        randIndex = new Random(DatagenParams.defaultSeed);
    }

    private void resetState(int seed) {
        randomFarm.resetRandomGenerators(seed);
        randIndex.setSeed(seed);
    }

    public List<Medium> signIn(List<Medium> mediums, List<Account> accounts, int blockId) {
        resetState(blockId);

        Random accountsToSignRand = randomFarm.get(RandomGeneratorFarm.Aspect.NUM_ACCOUNTS_SIGNIN_PER_MEDIUM);
        Random multiplicityRandom = randomFarm.get(RandomGeneratorFarm.Aspect.MULTIPLICITY_SIGNIN);
        int numAccountsToSign = accountsToSignRand.nextInt(DatagenParams.maxAccountToSignIn);

        for (Medium medium : mediums) {
            for (int i = 0; i < Math.max(1, numAccountsToSign); i++) {
                Account accountToSign = accounts.get(randIndex.nextInt(accounts.size()));
                if (cannotSignIn(medium, accountToSign)) {
                    continue;
                }
                int numSignIn = multiplicityRandom.nextInt(DatagenParams.maxSignInPerPair);
                for (int mid = 0; mid < Math.max(1, numSignIn); mid++) {
                    SignIn.createSignIn(randomFarm, mid, medium, accountToSign);
                }
            }
        }
        return mediums;
    }

    public boolean cannotSignIn(Medium from, Account to) {
        return from.getCreationDate() + DatagenParams.activityDelta > to.getDeletionDate();
    }
}
