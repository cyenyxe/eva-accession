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
package uk.ac.ebi.eva.accession.release.io;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.accession.core.configuration.MongoConfiguration;
import uk.ac.ebi.eva.accession.release.test.configuration.MongoTestConfiguration;
import uk.ac.ebi.eva.accession.release.test.rule.FixSpringMongoDbRule;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.STUDY_ID_KEY;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.VARIANT_CLASS_KEY;


@RunWith(SpringRunner.class)
@TestPropertySource("classpath:accession-release-test.properties")
@UsingDataSet(locations = {
        "/test-data/dbsnpClusteredVariantEntity.json",
        "/test-data/dbsnpSubmittedVariantEntity.json"})
@ContextConfiguration(classes = {MongoConfiguration.class, MongoTestConfiguration.class})
public class AccessionedVariantMongoReaderTest {

    private static final String ASSEMBLY_ACCESSION_1 = "GCF_000409795.2";

    private static final String ASSEMBLY_ACCESSION_2 = "GCF_000001735.3";

    private static final String ASSEMBLY_ACCESSION_3 = "GCF_000372685.1";

    private static final String ASSEMBLY_ACCESSION_4 = "GCF_000309985.1";

    private static final String TEST_DB = "test-db";

    private static final String DBSNP_CLUSTERED_VARIANT_ENTITY = "dbsnpClusteredVariantEntity";

    private static final String RS_1 = "869808637";

    private static final String RS_2 = "869927931";

    private static final String RS_3 = "347048227";

    private AccessionedVariantMongoReader reader;

    private ExecutionContext executionContext;

    @Autowired
    private MongoClient mongoClient;

    //Required by nosql-unit
    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = new FixSpringMongoDbRule(
            MongoDbConfigurationBuilder.mongoDb().databaseName(TEST_DB).build());

    @Before
    public void setUp() throws Exception {
        executionContext = new ExecutionContext();
        reader = new AccessionedVariantMongoReader(ASSEMBLY_ACCESSION_1, mongoClient, TEST_DB);
    }

    @Test
    public void readTestDataMongo() {
        MongoDatabase db = mongoClient.getDatabase(TEST_DB);
        MongoCollection<Document> collection = db.getCollection(DBSNP_CLUSTERED_VARIANT_ENTITY);

        AggregateIterable<Document> result = collection.aggregate(reader.buildAggregation())
                                                       .allowDiskUse(true)
                                                       .useCursor(true);

        MongoCursor<Document> cursor = result.iterator();

        List<Variant> variants = new ArrayList<>();
        while (cursor.hasNext()) {
            Document clusteredVariant = cursor.next();
            Variant variant = reader.getVariant(clusteredVariant);
            variants.add(variant);
        }
        assertEquals(2, variants.size());
     }

    @Test
    public void reader() throws Exception {
        List<Variant> variants = readIntoList();
        assertEquals(2, variants.size());
    }

    private List<Variant> readIntoList() throws Exception {
        reader.open(executionContext);
        List<Variant> variants = new ArrayList<>();
        Variant variant;
        while ((variant = reader.read()) != null) {
            variants.add(variant);
        }
        reader.close();
        return variants;
    }

    @Test
    public void linkedSubmittedVariants() throws Exception {
        Map<String, Variant> variants = readIntoMap();
        assertEquals(2, variants.size());
        assertEquals(2, variants.get(RS_1).getSourceEntries().size());
        assertEquals(1, variants.get(RS_2).getSourceEntries().size());
    }

    private Map<String, Variant> readIntoMap() throws Exception {
        reader.open(executionContext);
        Map<String, Variant> variants = new HashMap<>();
        Variant variant;
        while ((variant = reader.read()) != null) {
            variants.put(variant.getMainId(), variant);
        }
        reader.close();
        return variants;
    }

    @Test
    public void queryOtherAssembly() throws Exception {
        reader = new AccessionedVariantMongoReader(ASSEMBLY_ACCESSION_2, mongoClient, TEST_DB);
        reader.open(executionContext);
        Map<String, Variant> variants = new HashMap<>();
        Variant variant;
        while ((variant = reader.read()) != null) {
            variants.put(variant.getMainId(), variant);
        }
        assertEquals(1, variants.size());
        assertEquals(2, variants.get(RS_3).getSourceEntries().size());
    }

    @Test
    public void snpVariantClassAttribute() throws Exception {
        Map<String, Variant> variants = readIntoMap();
        assertEquals(2, variants.size());
        String snpSequenceOntology = "SO:0001483";
        assertTrue(variants
                           .get(RS_1)
                           .getSourceEntries()
                           .stream()
                           .allMatch(se -> snpSequenceOntology.equals(se.getAttribute(VARIANT_CLASS_KEY))));
        assertTrue(variants
                           .get(RS_2)
                           .getSourceEntries()
                           .stream()
                           .allMatch(se -> snpSequenceOntology.equals(se.getAttribute(VARIANT_CLASS_KEY))));
    }

    @Test
    public void insertionVariantClassAttribute() throws Exception {
        reader = new AccessionedVariantMongoReader(ASSEMBLY_ACCESSION_4, mongoClient, TEST_DB);
        List<Variant> variants = readIntoList();
        assertEquals(1, variants.size());
        String snpSequenceOntology = "SO:0000667";
        assertTrue(variants.get(0)
                           .getSourceEntries()
                           .stream()
                           .allMatch(se -> snpSequenceOntology.equals(se.getAttribute(VARIANT_CLASS_KEY))));
    }

    @Test
    public void studyIdAttribute() throws Exception {
        Map<String, Variant> variants = readIntoMap();
        assertEquals(2, variants.size());

        String studyId;
        studyId = "PRJEB7923";
        assertEquals(studyId, variants.get(RS_1).getSourceEntry(studyId, studyId).getAttribute(STUDY_ID_KEY));
        studyId = "PRJEB9999";
        assertEquals(studyId, variants.get(RS_1).getSourceEntry(studyId, studyId).getAttribute(STUDY_ID_KEY));
        studyId = "PRJEB7923";
        assertEquals(studyId, variants.get(RS_2).getSourceEntry(studyId, studyId).getAttribute(STUDY_ID_KEY));
    }

    @Test
    public void clusteredVariantWithoutSubmittedVariants() throws Exception {
        reader = new AccessionedVariantMongoReader(ASSEMBLY_ACCESSION_3, mongoClient, TEST_DB);
        List<Variant> variants = readIntoList();
        assertEquals(1, variants.size());
        assertEquals(0, variants.get(0).getSourceEntries().size());
    }
}
