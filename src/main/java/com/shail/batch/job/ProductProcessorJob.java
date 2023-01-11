package com.shail.batch.job;

import com.shail.batch.entity.Product;
import com.shail.batch.entity.ProductFinal;
import com.shail.batch.repository.ProductFinalRepository;
import com.shail.batch.repository.ProductRepository;
import com.shail.batch.service.ProductService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductProcessorJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ProductService service;

    @Autowired
    private ProductFinalRepository productFinalRepo;

    @Autowired
    private ProductRepository productRepository;

    @Bean("product.deleteFinalRowsStep")
    protected Step deleteFinalRowsStep(@Qualifier("product.deleteRowsTasklet") Tasklet  deleteRows) {
        return stepBuilderFactory.get("product.deleteFinalRows")
                .tasklet(deleteRows)
                .build();
    }

    @Bean("product.deleteRowsTasklet")
    public Tasklet deleteRowsTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                productFinalRepo.deleteAll();
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean("product.itemReader")
    public RepositoryItemReader<Product> itemReader() {
        RepositoryItemReader<Product> reader = new RepositoryItemReader<>();
        reader.setRepository(productRepository);
        reader.setMethodName("findByStatus");

        List<String> args = new ArrayList<>();
        args.add("Y");

        reader.setArguments(args);
        return reader;
    }

    @Bean("product.itemProcessor")
    public ItemProcessor<Product, ProductFinal> itemProcessor() {
        return new ItemProcessor<Product, ProductFinal>() {
            @Override
            public ProductFinal process(Product product) throws Exception {
                return service.processProducts(product);
            }
        };
    }

    @Bean("product.productFinalWriter")
    public RepositoryItemWriter<ProductFinal> prdFinalItemWriter() {
        RepositoryItemWriter<ProductFinal> writer = new RepositoryItemWriter<>();
        writer.setRepository(productFinalRepo);
//        writer.setMethodName("save");
        return writer;
    }

    @Bean("product.productWriter")
    public ItemWriter<ProductFinal> productWriter() {
        return new ItemWriter<ProductFinal>() {
            @Override
            public void write(List<? extends ProductFinal> prdFinal) throws Exception {
                List<Product> prds = prdFinal.parallelStream().map(item -> new Product(item.getId(), item.getName(), item.getPrice(), item.getDescription(), item.getWeight(), item.getBrand(), item.getOnForSale(), "Y"))
                        .collect(Collectors.toList());
                productRepository.saveAll(prds);
            }
        };
    }

    @Bean("product.compositeWriter")
    public CompositeItemWriter<ProductFinal> compositeItemWriter(
            @Qualifier("product.productFinalWriter") RepositoryItemWriter<ProductFinal> prdFinalWriter,
            @Qualifier("product.productWriter") ItemWriter<ProductFinal> productWriter
    ) {
        CompositeItemWriter<ProductFinal> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(prdFinalWriter, productWriter));
        return compositeItemWriter;
    }
}
