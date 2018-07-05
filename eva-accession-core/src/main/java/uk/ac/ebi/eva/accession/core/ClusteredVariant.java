/*
 *
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
 *
 */
package uk.ac.ebi.eva.accession.core;

import java.time.LocalDateTime;
import java.util.Objects;

public class ClusteredVariant implements IClusteredVariant {

    private String assemblyAccession;

    private int taxonomyAccession;

    private String contig;

    private long start;

    private String referenceAllele;

    private String alternateAllele;

    private Boolean validated;

    private LocalDateTime createdDate;

    public ClusteredVariant(String assemblyAccession, int taxonomyAccession, String contig, long start,
                            String referenceAllele, String alternateAllele, Boolean validated) {
        if (Objects.isNull(assemblyAccession)) {
            throw new IllegalArgumentException("Assembly accession is required");
        }
        if (Objects.isNull(contig)) {
            throw new IllegalArgumentException("Contig is required");
        }
        if (Objects.isNull(referenceAllele)) {
            throw new IllegalArgumentException("Reference allele is required");
        }
        if (Objects.isNull(alternateAllele)) {
            throw new IllegalArgumentException("Alternate allele is required");
        }

        this.assemblyAccession = assemblyAccession;
        this.taxonomyAccession = taxonomyAccession;
        this.contig = contig;
        this.start = start;
        this.referenceAllele = referenceAllele;
        this.alternateAllele = alternateAllele;
        this.validated = validated;
        this.createdDate = null;
    }

    @Override
    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    public void setAssemblyAccession(String assemblyAccession) {
        this.assemblyAccession = assemblyAccession;
    }

    @Override
    public int getTaxonomyAccession() {
        return taxonomyAccession;
    }

    public void setTaxonomyAccession(int taxonomyAccession) {
        this.taxonomyAccession = taxonomyAccession;
    }

    @Override
    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    @Override
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    @Override
    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    @Override
    public String getAlternateAllele() {
        return alternateAllele;
    }

    public void setAlternateAllele(String alternateAllele) {
        this.alternateAllele = alternateAllele;
    }

    @Override
    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ISubmittedVariant)) return false;

        ISubmittedVariant that = (ISubmittedVariant) o;

        if (taxonomyAccession != that.getTaxonomyAccession()) return false;
        if (start != that.getStart()) return false;
        if (!assemblyAccession.equals(that.getAssemblyAccession())) return false;
        if (!contig.equals(that.getContig())) return false;
        if (!referenceAllele.equals(that.getReferenceAllele())) return false;
        return alternateAllele.equals(that.getAlternateAllele());
    }

    @Override
    public int hashCode() {
        return Objects.hash(assemblyAccession, taxonomyAccession, contig, start, referenceAllele, alternateAllele);
    }

    @Override
    public String toString() {
        return "ClusteredVariant{" +
                "assemblyAccession='" + assemblyAccession + '\'' +
                ", taxonomyAccession=" + taxonomyAccession +
                ", contig='" + contig + '\'' +
                ", start=" + start +
                ", referenceAllele='" + referenceAllele + '\'' +
                ", alternateAllele='" + alternateAllele + '\'' +
                ", validated=" + validated +
                ", createdDate=" + createdDate +
                '}';
    }
}
