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

package ldbc.finbench.datagen.entities.edges;

import java.io.Serializable;
import java.util.Random;
import ldbc.finbench.datagen.entities.DynamicActivity;
import ldbc.finbench.datagen.entities.nodes.Company;
import ldbc.finbench.datagen.entities.nodes.Person;
import ldbc.finbench.datagen.generation.dictionary.Dictionaries;
import ldbc.finbench.datagen.util.RandomGeneratorFarm;

public class PersonInvestCompany implements DynamicActivity, Serializable {
    private final long personId;
    private final long companyId;
    private double ratio;
    private final long creationDate;
    private final long deletionDate;
    private final boolean isExplicitlyDeleted;
    private final String comment;

    public PersonInvestCompany(Person person, Company company,
                               long creationDate, long deletionDate, double ratio, boolean isExplicitlyDeleted,
                               String comment) {
        this.personId = person.getPersonId();
        this.companyId = company.getCompanyId();
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;
        this.ratio = ratio;
        this.isExplicitlyDeleted = isExplicitlyDeleted;
        this.comment = comment;
    }

    public static void createPersonInvestCompany(RandomGeneratorFarm farm, Person investor,
                                                 Company target) {
        Random dateRandom = farm.get(RandomGeneratorFarm.Aspect.PERSON_INVEST_DATE);
        long creationDate = Dictionaries.dates.randomPersonToCompanyDate(dateRandom, investor, target);
        double ratio = farm.get(RandomGeneratorFarm.Aspect.INVEST_RATIO).nextDouble();
        String comment =
            Dictionaries.randomTexts.getUniformDistRandomTextForComments(
                farm.get(RandomGeneratorFarm.Aspect.COMMON_COMMENT));
        PersonInvestCompany personInvestCompany = new PersonInvestCompany(investor, target, creationDate, 0, ratio,
                                                                          false, comment);
        target.getPersonInvestCompanies().add(personInvestCompany);
    }

    public void scaleRatio(double sum) {
        this.ratio = this.ratio / sum;
    }

    public double getRatio() {
        return ratio;
    }

    public long getCompanyId() {
        return companyId;
    }

    public long getPersonId() {
        return personId;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public long getDeletionDate() {
        return deletionDate;
    }

    @Override
    public boolean isExplicitlyDeleted() {
        return isExplicitlyDeleted;
    }

    public String getComment() {
        return comment;
    }
}
