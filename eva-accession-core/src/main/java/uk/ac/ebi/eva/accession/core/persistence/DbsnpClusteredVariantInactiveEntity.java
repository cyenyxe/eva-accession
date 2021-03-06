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
package uk.ac.ebi.eva.accession.core.persistence;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.ac.ebi.ampt2d.commons.accession.persistence.mongodb.document.InactiveSubDocument;

import uk.ac.ebi.eva.accession.core.ClusteredVariant;
import uk.ac.ebi.eva.accession.core.IClusteredVariant;
import uk.ac.ebi.eva.commons.core.models.VariantType;

public class DbsnpClusteredVariantInactiveEntity extends InactiveSubDocument<IClusteredVariant, Long>
        implements IClusteredVariant {

    @Indexed(background = true)
    @Field("asm")
    private String assemblyAccession;

    @Field("tax")
    private int taxonomyAccession;

    private String contig;

    private long start;

    private VariantType type;

    private Boolean validated;

    public DbsnpClusteredVariantInactiveEntity() {
        super();
    }

    public DbsnpClusteredVariantInactiveEntity(DbsnpClusteredVariantEntity dbsnpClusteredVariantEntity) {
        super(dbsnpClusteredVariantEntity);
        this.assemblyAccession = dbsnpClusteredVariantEntity.getAssemblyAccession();
        this.taxonomyAccession = dbsnpClusteredVariantEntity.getTaxonomyAccession();
        this.contig = dbsnpClusteredVariantEntity.getContig();
        this.start = dbsnpClusteredVariantEntity.getStart();
        this.type = dbsnpClusteredVariantEntity.getType();
        this.validated = dbsnpClusteredVariantEntity.isValidated();
    }

    @Override
    public String getAssemblyAccession() {
        return assemblyAccession;
    }

    @Override
    public int getTaxonomyAccession() {
        return taxonomyAccession;
    }

    @Override
    public String getContig() {
        return contig;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public VariantType getType() {
        return type;
    }

    @Override
    public Boolean isValidated() {
        return validated;
    }

    @Override
    public IClusteredVariant getModel() {
        return new ClusteredVariant(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbsnpClusteredVariantInactiveEntity that = (DbsnpClusteredVariantInactiveEntity) o;

        if (taxonomyAccession != that.taxonomyAccession) return false;
        if (start != that.start) return false;
        if (!assemblyAccession.equals(that.assemblyAccession)) return false;
        if (!contig.equals(that.contig)) return false;
        if (type != that.type) return false;
        return validated != null ? validated.equals(that.validated) : that.validated == null;
    }

    @Override
    public int hashCode() {
        int result = assemblyAccession.hashCode();
        result = 31 * result + taxonomyAccession;
        result = 31 * result + contig.hashCode();
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + type.hashCode();
        result = 31 * result + (validated != null ? validated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DbsnpClusteredVariantInactiveEntity{"
                + "hashedMessage='" + getHashedMessage() + '\''
                + ", assemblyAccession='" + assemblyAccession + '\''
                + ", taxonomyAccession=" + taxonomyAccession
                + ", contig='" + contig + '\''
                + ", start=" + start
                + ", type=" + type
                + ", validated=" + validated
                + '}';
    }
}
