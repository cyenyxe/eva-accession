/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.accession.release.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static uk.ac.ebi.eva.accession.release.configuration.BeanNames.ACCESSION_RELEASE_JOB;
import static uk.ac.ebi.eva.accession.release.configuration.BeanNames.RELEASE_MAPPED_ACTIVE_VARIANTS_STEP;

@Configuration
@EnableBatchProcessing
public class AccessionReleaseJobConfiguration {

    @Autowired
    @Qualifier(RELEASE_MAPPED_ACTIVE_VARIANTS_STEP)
    private Step createReleaseStep;

    @Bean(ACCESSION_RELEASE_JOB)
    public Job accessionReleaseJob(JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory.get(ACCESSION_RELEASE_JOB)
                                .incrementer(new RunIdIncrementer())
                                .start(createReleaseStep)
                                .build();
    }
}
