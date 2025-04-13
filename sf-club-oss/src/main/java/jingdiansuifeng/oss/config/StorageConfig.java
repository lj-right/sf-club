package jingdiansuifeng.oss.config;

import jingdiansuifeng.oss.adapter.StorageAdapter;
import jingdiansuifeng.oss.adapter.AliStorageAdapter;
import jingdiansuifeng.oss.adapter.MinioStorageAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class StorageConfig {

    @Value("${storage.service.type}")
    private String storageType;

    @Bean
    public StorageAdapter storageService(){
        switch (storageType){
            case "aliyun":
                return new AliStorageAdapter();
            case "minio":
            default:
                return new MinioStorageAdapter();
        }
    }

}
