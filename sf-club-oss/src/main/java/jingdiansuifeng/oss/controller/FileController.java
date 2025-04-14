package jingdiansuifeng.oss.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import jingdiansuifeng.oss.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class FileController {

    @Resource
    private FileService fileService;

    @RequestMapping("/testGetAllBuckets")
    private String testGetAllBucket() throws Exception {
        List<String> allBucket = fileService.getAllBucket();
        return allBucket.get(0);
    }
    @RequestMapping("/testNacos")
    private String testNacos() throws Exception {
        return "tsetnacos";
    }
}
