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

import htsjdk.variant.variantcontext.VariantContext;
import org.assertj.core.util.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.ebi.eva.accession.release.steps.processors.VariantToVariantContextProcessor;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.ALLELES_MATCH_KEY;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.ASSEMBLY_MATCH_KEY;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.SUBMITTED_VARIANT_VALIDATED_KEY;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.SUPPORTED_BY_EVIDENCE_KEY;
import static uk.ac.ebi.eva.accession.release.io.AccessionedVariantMongoReader.CLUSTERED_VARIANT_VALIDATED_KEY;

public class VariantContextWriterTest {

    private static final String ID = "rs123";

    private static final String CHR_1 = "1";

    private static final String FILE_ID = "fileId";

    private static final String SNP_SEQUENCE_ONTOLOGY = "SO:0001483";

    private static final String DELETION_SEQUENCE_ONTOLOGY = "SO:0000159";

    private static final String INSERTION_SEQUENCE_ONTOLOGY = "SO:0000667";

    private static final String INDEL_SEQUENCE_ONTOLOGY = "SO:1000032";

    private static final String TANDEM_REPEAT_SEQUENCE_ONTOLOGY = "SO:0000705";

    private static final String SEQUENCE_ALTERATION_SEQUENCE_ONTOLOGY = "SO:0001059";

    private static final String MNV_SEQUENCE_ONTOLOGY = "SO:0002007";

    private static final String VARIANT_CLASS_KEY = "VC";

    private static final String STUDY_ID_KEY = "SID";

    private static final String STUDY_1 = "study_1";

    private static final String STUDY_2 = "study_2";

    private static final String REFERENCE_ASSEMBLY = "GCA_00000XXX.X";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void basicWrite() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1));
    }

    private Variant buildVariant(String chr, int start, String reference, String alternate,
                                 String sequenceOntologyTerm, String... studies) {
        return buildVariant(chr, start, reference, alternate, sequenceOntologyTerm, false, false, true, true, true,
                            studies);
    }

    private Variant buildVariant(String chr, int start, String reference, String alternate,
                                 String sequenceOntologyTerm, boolean validated, boolean submittedVariantValidated,
                                 boolean allelesMatch, boolean assemblyMatch, boolean evidence, String... studies) {
        Variant variant = new Variant(chr, start, start + alternate.length(), reference, alternate);
        variant.setMainId(ID);
        for (String study : studies) {
            VariantSourceEntry sourceEntry = new VariantSourceEntry(study, FILE_ID);
            sourceEntry.addAttribute(VARIANT_CLASS_KEY, sequenceOntologyTerm);
            sourceEntry.addAttribute(STUDY_ID_KEY, study);
            sourceEntry.addAttribute(CLUSTERED_VARIANT_VALIDATED_KEY, Boolean.toString(validated));
            sourceEntry.addAttribute(SUBMITTED_VARIANT_VALIDATED_KEY, Boolean.toString(submittedVariantValidated));
            sourceEntry.addAttribute(ALLELES_MATCH_KEY, Boolean.toString(allelesMatch));
            sourceEntry.addAttribute(ASSEMBLY_MATCH_KEY, Boolean.toString(assemblyMatch));
            sourceEntry.addAttribute(SUPPORTED_BY_EVIDENCE_KEY, Boolean.toString(evidence));
            variant.addSourceEntry(sourceEntry);
        }
        return variant;
    }

    public void assertWriteVcf(File output, Variant... variants) throws Exception {
        VariantContextWriter writer = new VariantContextWriter(output, REFERENCE_ASSEMBLY);
        writer.open(null);

        VariantToVariantContextProcessor variantToVariantContextProcessor = new VariantToVariantContextProcessor();
        List<VariantContext> variantContexts =
                Stream.of(variants).map(variantToVariantContextProcessor::process).collect(Collectors.toList());
        writer.write(variantContexts);

        writer.close();

        assertTrue(output.exists());
    }

    @Test
    public void checkReference() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> referenceLines = grepFile(output, "^##reference.*");
        assertEquals(1, referenceLines.size());
        assertEquals("##reference=" + REFERENCE_ASSEMBLY, referenceLines.get(0));
    }

    private List<String> grepFile(File file, String regex) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches(regex)) {
                lines.add(line);
            }
        }
        reader.close();
        return lines;
    }

    @Test
    public void checkMetadataSection() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> metadataLines = grepFile(output, "^##.*");
        assertEquals(9, metadataLines.size());
        List<String> headerLines = grepFile(output, "^#CHROM.*");
        assertEquals(1, headerLines.size());
    }

    @Test
    public void checkAccession() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> dataLines = grepFileContains(output, ID);
        assertEquals(1, dataLines.size());
    }

    @Test
    public void checkColumns() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> dataLines = grepFileContains(output, ID);
        assertEquals(1, dataLines.size());
        assertEquals(8, dataLines.get(0).split("\t", -1).length);
    }

    @Test
    public void checkStudies() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "A", SNP_SEQUENCE_ONTOLOGY, STUDY_1, STUDY_2));

        List<String> metadataLines = grepFileContains(output, STUDY_1);
        assertEquals(1, metadataLines.size());
        String[] infoPairs = metadataLines.get(0).split("\t")[7].split(";");
        boolean isSidPresent = false;
        for (String infoPair : infoPairs) {
            String[] keyValue = infoPair.split("=");
            if (keyValue[0].equals("SID")) {
                isSidPresent = true;
                String[] studies = keyValue[1].split(",");
                assertEquals(2, studies.length);
                assertEquals(Sets.newLinkedHashSet(STUDY_1, STUDY_2), Sets.newLinkedHashSet(studies));
            }
        }
        assertTrue(isSidPresent);
    }

    private List<String> grepFileContains(File output, String contains) throws IOException {
        return grepFile(output, ".*" + contains + ".*");
    }

    @Test
    public void checkSnpSequenceOntology() throws Exception {
        checkSequenceOntology(SNP_SEQUENCE_ONTOLOGY, "C", "A");
    }

    private void checkSequenceOntology(String sequenceOntology, String reference, String alternate) throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, reference, alternate, sequenceOntology, STUDY_1, STUDY_2));

        List<String> dataLines = grepFile(output, somewhereSurroundedByTabOrSemicolon(VARIANT_CLASS_KEY + "=SO:[0-9]+"));
        assertEquals(1, dataLines.size());
        String[] infoPairs = dataLines.get(0).split("\t")[7].split(";");
        boolean isVariantClassPresent = false;
        for (String infoPair : infoPairs) {
            String[] keyValue = infoPair.split("=");
            if (keyValue[0].equals(VARIANT_CLASS_KEY)) {
                isVariantClassPresent = true;
                String[] variantClass = keyValue[1].split(",");
                assertEquals(1, variantClass.length);
                assertEquals(sequenceOntology, variantClass[0]);
            }
        }
        assertTrue(isVariantClassPresent);
    }

    @Test
    public void checkInsertionSequenceOntology() throws Exception {
        checkSequenceOntology(INSERTION_SEQUENCE_ONTOLOGY, "C", "CA");
    }

    @Test
    public void checkDeletionSequenceOntology() throws Exception {
        checkSequenceOntology(DELETION_SEQUENCE_ONTOLOGY, "CA", "A");
    }

    @Test
    public void checkIndelSequenceOntology() throws Exception {
        checkSequenceOntology(INDEL_SEQUENCE_ONTOLOGY, "CAT", "CG");
    }

    @Test
    public void checkTandemRepeatSequenceOntology() throws Exception {
        checkSequenceOntology(TANDEM_REPEAT_SEQUENCE_ONTOLOGY, "C", "CAGAG");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkSequenceAlterationSequenceOntology() throws Exception {
        checkSequenceOntology(SEQUENCE_ALTERATION_SEQUENCE_ONTOLOGY, "(ADL260)", "(LEI0062)");
    }

    @Test
    public void checkMnvSequenceOntology() throws Exception {
        checkSequenceOntology(MNV_SEQUENCE_ONTOLOGY, "CA", "GG");
    }

    @Test
    public void checkSeveralVariants() throws Exception {
        File output = temporaryFolder.newFile();
        int position1 = 1003;
        int position2 = 1003;
        int position3 = 1002;
        String alternate1 = "A";
        String alternate2 = "T";
        String alternate3 = "G";

        assertWriteVcf(output,
                       buildVariant(CHR_1, position1, "C", alternate1, SNP_SEQUENCE_ONTOLOGY, STUDY_1),
                       buildVariant(CHR_1, position2, "C", alternate2, SNP_SEQUENCE_ONTOLOGY, STUDY_1),
                       buildVariant(CHR_1, position3, "C", alternate3, SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> dataLines = grepFileContains(output, ID);
        assertEquals(3, dataLines.size());
        assertEquals(position1, Integer.parseInt(dataLines.get(0).split("\t")[1]));
        assertEquals(alternate1, dataLines.get(0).split("\t")[4]);

        assertEquals(position2, Integer.parseInt(dataLines.get(1).split("\t")[1]));
        assertEquals(alternate2, dataLines.get(1).split("\t")[4]);

        assertEquals(position3, Integer.parseInt(dataLines.get(2).split("\t")[1]));
        assertEquals(alternate3, dataLines.get(2).split("\t")[4]);
    }

    @Test
    public void checkStandardNucleotides() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "NACTG", SNP_SEQUENCE_ONTOLOGY, STUDY_1));

        List<String> dataLines = grepFileContains(output, ID);
        assertEquals(1, dataLines.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIfNonStandardNucleotides() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output, buildVariant(CHR_1, 1000, "C", "U", SNP_SEQUENCE_ONTOLOGY, STUDY_1));
    }

    @Test
    public void checkFlagsAreNotPresentWhenDefaultValues() throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output,
                       buildVariant(CHR_1, 1000, "C", "G", SNP_SEQUENCE_ONTOLOGY, false, false, true, true, true,
                                    STUDY_1));

        String dataLinesRegex = "^[^#]";
        String dataLinesWithFlagsRegex = dataLinesRegex + ".*(" + CLUSTERED_VARIANT_VALIDATED_KEY
                                         + "|" + SUBMITTED_VARIANT_VALIDATED_KEY
                                         + "|" + ALLELES_MATCH_KEY
                                         + "|" + ASSEMBLY_MATCH_KEY
                                         + "|" + SUPPORTED_BY_EVIDENCE_KEY + ")";
        List<String> dataLines = grepFile(output, dataLinesWithFlagsRegex);

        assertEquals(0, dataLines.size());
    }

    @Test
    public void checkNonDefaultValidatedFlag() throws Exception {
        assertFlagIsPresent(CLUSTERED_VARIANT_VALIDATED_KEY);
    }

    private void assertFlagIsPresent(String flagRegex) throws Exception {
        File output = temporaryFolder.newFile();
        assertWriteVcf(output,
                       buildVariant(CHR_1, 1000, "C", "G", SNP_SEQUENCE_ONTOLOGY, true, true, false, false, false,
                                    STUDY_1));

        String dataLinesRegex = "^[^#]";
        String dataLinesWithValidatedRegex = dataLinesRegex + somewhereSurroundedByTabOrSemicolon(flagRegex);
        List<String> dataLines;
        dataLines = grepFile(output, dataLinesWithValidatedRegex);
        assertEquals(1, dataLines.size());
    }

    private String somewhereSurroundedByTabOrSemicolon(String regex) {
        return ".*[\t;]" + regex + "([\t;].*|$)";
    }

    @Test
    public void checkHowShouldTabsBeWrittenInJavaRegex() {
        assertTrue("\t".matches("\t")); // java string containing literal tab
        assertTrue("\t".matches("\\t"));    // java string containing a backwards slash and a 't', interpreted as
                                            // a tab by the regex classes, @see Pattern
    }

    @Test
    public void checkNonDefaultSubmittedVariantValidatedFlag() throws Exception {
        assertFlagIsPresent(SUBMITTED_VARIANT_VALIDATED_KEY + "=[0-9]");
    }
    @Test
    public void checkNonDefaultAllelesMatchFlag() throws Exception {
        assertFlagIsPresent(ALLELES_MATCH_KEY);
    }

    @Test
    public void checkNonDefaultAssemblyMatchFlag() throws Exception {
        assertFlagIsPresent(ASSEMBLY_MATCH_KEY);
    }

    @Test
    public void checkNonDefaultSupportedByEvidenceFlag() throws Exception {
        assertFlagIsPresent(SUPPORTED_BY_EVIDENCE_KEY);
    }

    @Test
    public void checkSeveralSupportedByEvidenceFlags() throws Exception {
        assertSeveralFlagValues(SUPPORTED_BY_EVIDENCE_KEY, true, false, 0);
    }

    private void assertSeveralFlagValues(String flagKey, boolean firstValue, boolean secondValue,
                                         int expectedLinesWithTheFlag) throws Exception {
        Variant variant = new Variant(CHR_1, 1000, 1000, "C", "G");
        variant.setMainId(ID);

        VariantSourceEntry sourceEntry1 = new VariantSourceEntry(STUDY_1, FILE_ID);
        sourceEntry1.addAttribute(flagKey, Boolean.toString(firstValue));
        variant.addSourceEntry(sourceEntry1);

        VariantSourceEntry sourceEntry2 = new VariantSourceEntry(STUDY_2, FILE_ID);
        sourceEntry2.addAttribute(flagKey, Boolean.toString(secondValue));
        variant.addSourceEntry(sourceEntry2);
        File output = temporaryFolder.newFile();

        assertWriteVcf(output, variant);

        String dataLinesRegex = "^[^#]";
        String dataLinesWithValidatedRegex = dataLinesRegex + somewhereSurroundedByTabOrSemicolon(flagKey);
        List<String> dataLines;
        dataLines = grepFile(output, dataLinesWithValidatedRegex);
        assertEquals(expectedLinesWithTheFlag, dataLines.size());
    }
    @Test
    public void checkSeveralValidatedFlags() throws Exception {
        assertSeveralFlagValues(CLUSTERED_VARIANT_VALIDATED_KEY, true, false, 1);
    }
    @Test
    public void checkSeveralAllelesMatchFlags() throws Exception {
        assertSeveralFlagValues(ALLELES_MATCH_KEY, true, false, 1);
    }
    @Test
    public void checkSeveralAssemblyMatchFlags() throws Exception {
        assertSeveralFlagValues(ASSEMBLY_MATCH_KEY, true, false, 1);
    }
}
