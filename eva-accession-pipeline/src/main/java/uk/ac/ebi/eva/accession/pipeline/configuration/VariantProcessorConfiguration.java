package uk.ac.ebi.eva.accession.pipeline.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.accession.core.ISubmittedVariant;
import uk.ac.ebi.eva.accession.pipeline.parameters.InputParameters;
import uk.ac.ebi.eva.accession.pipeline.steps.processors.ExcludeStructuralVariantsProcessor;
import uk.ac.ebi.eva.accession.pipeline.steps.processors.VariantProcessor;
import uk.ac.ebi.eva.commons.core.models.IVariant;

import java.util.Arrays;

import static uk.ac.ebi.eva.accession.pipeline.configuration.BeanNames.COMPOSITE_VARIANT_PROCESSOR;

/**
 * Configuration to inject a VariantProcessor as a bean.
 */
@Configuration
public class VariantProcessorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VariantProcessorConfiguration.class);

    @Bean(COMPOSITE_VARIANT_PROCESSOR)
    @StepScope
    public ItemProcessor<IVariant, ISubmittedVariant> compositeVariantProcessor(
            InputParameters inputParameters, VariantProcessor variantProcessor,
            ExcludeStructuralVariantsProcessor excludeStructuralVariantsProcessor) {
        logger.info("Injecting dbsnpVariantProcessor with parameters: {}", inputParameters);
        CompositeItemProcessor<IVariant, ISubmittedVariant> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(excludeStructuralVariantsProcessor, variantProcessor));
        return compositeProcessor;
    }

    @Bean
    VariantProcessor variantProcessor(InputParameters inputParameters) {
        String assemblyAccession = inputParameters.getAssemblyAccession();
        int taxonomyAccession = inputParameters.getTaxonomyAccession();
        String projectAccession = inputParameters.getProjectAccession();

        return new VariantProcessor(assemblyAccession, taxonomyAccession, projectAccession);
    }

    @Bean
    ExcludeStructuralVariantsProcessor excludeStructuralVariantsProcessor() {
        return new ExcludeStructuralVariantsProcessor();
    }
}
