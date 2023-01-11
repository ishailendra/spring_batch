package com.shail.batch.job;

import com.shail.batch.entity.Product;
import com.shail.batch.repository.ProductFinalRepository;
import com.shail.batch.repository.ProductRepository;
import com.shail.batch.service.ProductService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ProductFileExportJob {

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

    @Bean(name = "product.productJob")
    public Job productJob(
            @Qualifier("product.processProductStep") Step step
    ) {
        return jobBuilderFactory.get("product.productJob")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean(name = "product.processProductStep")
    protected Step processRecords(
            @Qualifier("product.itemReader") RepositoryItemReader<Product> reader,
            @Qualifier("product.productProcessor") ItemProcessor<Product, Product> processor,
            @Qualifier("product.productFlatFileWriter") FlatFileItemWriter<Product> writer
    ) {
        return stepBuilderFactory.get("product.productFileStep")
                .<Product, Product>chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                //.skip(Exception.class)
                .build();
    }

    @Bean("product.itemReader")
    public RepositoryItemReader<Product> itemReader() {
        Map<String, Sort.Direction> sortMap = new HashMap<>();
        sortMap.put("id", Sort.Direction.ASC);

        RepositoryItemReader<Product> reader = new RepositoryItemReader<>();
        reader.setRepository(productRepository);
        reader.setMethodName("findByStatus");

        List<Object> args = new ArrayList<>();
        args.add("-");

        reader.setArguments(args);
        reader.setSort(sortMap);
        reader.setPageSize(1000);
        return reader;
    }

    @Bean("product.productProcessor")
    public ItemProcessor<Product, Product> productProcessor() {
        return new ItemProcessor<Product, Product>() {
            @Override
            public Product process(Product product) throws Exception {
                System.err.println(product.getId());
                return product;
            }
        };
    }

    @Bean("product.productFlatFileWriter")
    @StepScope
    public FlatFileItemWriter<Product> flatFileItemWriter(@Value("#{jobParameters['outputFile']}") String outputFile) {
        FlatFileItemWriter<Product> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFile));

        writer.setLineAggregator(new DelimitedLineAggregator<>(){
            {
                setDelimiter("|");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{
                                "id", "name", "price", "description", "weight", "brand", "onForSale", "status"
                        });
                    }
                });
            }
        });
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("id|name|price|description|weight|brand|onForSale|status");
            }
        });

//        writer.setFooterCallback(new FlatFileFooterCallback() {
//            @Override
//            public void writeFooter(Writer writer) throws IOException {
//                writer.write("****************File Created At:  " + LocalDateTime.now() +" ****************");
//            }
//        });

        writer.setLineSeparator("\n");
//        writer.setAppendAllowed(true);
//        writer.setShouldDeleteIfEmpty(true);
        writer.setShouldDeleteIfExists(true);
        return writer;
    }
}
