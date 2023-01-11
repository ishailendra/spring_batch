package com.shail.batch.job;

import com.shail.batch.entity.Product;
import com.shail.batch.repository.ProductRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
//import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
//import org.springframework.batch.item.file.transform.IncorrectTokenCountException;
//import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class ProductFileImportJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ProductRepository productRepository;

    @Bean(name = "fileImport.productFileImportJob")
    public Job importFileJob(
      @Qualifier("fileImport.productFileImportStep") Step step
    ) {
        return jobBuilderFactory.get("fileImport.productFileImportJob")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean(name = "fileImport.productFileImportStep")
    protected Step importRecords(
            @Qualifier("fileImport.itemReader") FlatFileItemReader<Product> itemReader,
            @Qualifier("fileImport.itemProcessor") ItemProcessor<Product, Product> itemProcessor,
            @Qualifier("fileImport.itemWriter") RepositoryItemWriter<Product> itemWriter
    ) {
        return stepBuilderFactory.get("fileImport.productImportStep")
                .<Product, Product>chunk(1000)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .faultTolerant()
//                .skip(IncorrectTokenCountException.class)
//                .skipLimit(1)
                .build();
    }

    @Bean(name = "fileImport.itemReader")
    @StepScope
    public FlatFileItemReader<Product> itemReader(@Value("#{jobParameters[inputFile]}") String inputFile) {
        FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<>() {
            {

                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setDelimiter("|");
                        setNames(new String[] {
                                "id", "name", "price", "description", "weight", "brand", "onForSale", "status"
                        });
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {
                    {
                        setTargetType(Product.class);
                    }
                });
            }
        });

        return reader;
    }

    /*
    @Bean(name = "fileImport.fieldSetMapper")
    public FieldSetMapper<Product> fieldSetMapper() {
        return new BeanWrapperFieldSetMapper<Product>() {
            {
                setTargetType(Product.class);
            }
        };
    }

    @Bean(name = "fileImport.lineTokenizer")
    public LineTokenizer lineTokenizer() {
        return new DelimitedLineTokenizer() {
            {
                setDelimiter("|");
                setNames(new String[] {
                        "id", "name", "price", "description", "weight", "brand", "onForSale", "status"
                });
            }
        };
    }

    @Bean("fileImport.lineMapper")
    public LineMapper<Product> lineMapper(
            @Qualifier("fileImport.fieldSetMapper") FieldSetMapper fieldMapper,
            @Qualifier("fileImport.lineTokenizer") LineTokenizer lineTokenizer
    ) {
       DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();
       lineMapper.setFieldSetMapper(fieldMapper);
       lineMapper.setLineTokenizer(lineTokenizer);

    }
    */

    @Bean(name = "fileImport.itemProcessor")
    public ItemProcessor<Product, Product> itemProcessor() {
        return new ItemProcessor<Product, Product>() {
            @Override
            public Product process(Product item) throws Exception {
                System.err.println(item.getId());
                return item;
            }
        };
    }

    @Bean(name = "fileImport.itemWriter")
    public RepositoryItemWriter<Product> itemWriter() {
        RepositoryItemWriter<Product> writer = new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        return writer;
    }
}
