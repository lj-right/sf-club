package com.jingdiansuifeng.subject.domain.service.impl;

import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.domain.entity.EsSubjectFields;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoEs;
import com.jingdiansuifeng.subject.domain.service.SubjectEsService;
import com.jingdiansuifeng.subject.infra.basic.es.EsConfigProperties;
import com.jingdiansuifeng.subject.infra.basic.es.EsIndexInfo;
import com.jingdiansuifeng.subject.infra.basic.es.EsRestClient;
import com.jingdiansuifeng.subject.infra.basic.es.EsSearchRequest;
import com.jingdiansuifeng.subject.infra.basic.es.EsSourceData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Service
@Slf4j
public class SubjectEsServiceImpl implements SubjectEsService {

    @Resource
    private EsConfigProperties esConfigProperties;

    @Override
    public Boolean createIndex() {
        String mappingJson = "{\n" +
                "  \"properties\": {\n" +
                "    \"doc_id\": { \"type\": \"long\" },\n" +
                "    \"subject_id\": { \"type\": \"long\" },\n" +
                "    \"subject_name\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"subject_answer\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"subject_type\": { \"type\": \"integer\" },\n" +
                "    \"create_user\": { \"type\": \"keyword\" },\n" +
                "    \"create_time\": { \"type\": \"long\" },\n" +
                "    \"category_names\": { \"type\": \"keyword\" },\n" +
                "    \"label_names\": { \"type\": \"keyword\" }\n" +
                "  }\n" +
                "}";
        return EsRestClient.createIndex(getEsIndexInfo(), mappingJson);
    }

    @Override
    public Boolean insert(SubjectInfoEs subjectInfoEs) {
        if (!EsRestClient.isExistIndex(getEsIndexInfo())) {
            createIndex();
        }
        EsSourceData esSourceData = new EsSourceData();
        Map<String, Object> data = convert2EsSourceData(subjectInfoEs);
        // 直接使用 subjectId 作为 ES 文档主键 _id，天然保障 MQ 重试消费的幂等性与有序性
        esSourceData.setDocId(subjectInfoEs.getSubjectId().toString());
        esSourceData.setData(data);
        return EsRestClient.insertDoc(getEsIndexInfo(), esSourceData);
    }

    @Override
    public Boolean delete(Long subjectId) {
        if (Objects.isNull(subjectId)) {
            return false;
        }
        return EsRestClient.deleteDoc(getEsIndexInfo(), subjectId.toString());
    }

    @Override
    public Boolean batchInsert(List<SubjectInfoEs> subjectInfoEsList) {
        if (CollectionUtils.isEmpty(subjectInfoEsList)) {
            return true;
        }
        if (!EsRestClient.isExistIndex(getEsIndexInfo())) {
            createIndex();
        }
        List<EsSourceData> esSourceDataList = new LinkedList<>();
        for (SubjectInfoEs subjectInfoEs : subjectInfoEsList) {
            EsSourceData esSourceData = new EsSourceData();
            esSourceData.setDocId(subjectInfoEs.getSubjectId().toString());
            esSourceData.setData(convert2EsSourceData(subjectInfoEs));
            esSourceDataList.add(esSourceData);
        }
        return EsRestClient.batchInsertDoc(getEsIndexInfo(), esSourceDataList);
    }

    private Map<String, Object> convert2EsSourceData(SubjectInfoEs subjectInfoEs) {
        Map<String, Object> data = new HashMap<>();
        data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId() != null ? subjectInfoEs.getDocId() : subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
        data.put(EsSubjectFields.SUBJECT_ANSWER, subjectInfoEs.getSubjectAnswer());
        data.put(EsSubjectFields.SUBJECT_TYPE, subjectInfoEs.getSubjectType());
        data.put(EsSubjectFields.CREATE_USER, subjectInfoEs.getCreateUser());
        data.put(EsSubjectFields.CREATE_TIME, subjectInfoEs.getCreateTime());
        if (!CollectionUtils.isEmpty(subjectInfoEs.getCategoryNames())) {
            data.put(EsSubjectFields.CATEGORY_NAMES, subjectInfoEs.getCategoryNames());
        }
        if (!CollectionUtils.isEmpty(subjectInfoEs.getLabelNames())) {
            data.put(EsSubjectFields.LABEL_NAMES, subjectInfoEs.getLabelNames());
        }
        return data;
    }

    @Override
    public PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs req) {
        PageResult<SubjectInfoEs> pageResult = new PageResult<>();
        EsSearchRequest esSearchRequest = createSearchListQuery(req);
        SearchResponse searchResponse = EsRestClient.searchWithTermQuery(getEsIndexInfo(), esSearchRequest);

        List<SubjectInfoEs> subjectInfoEsList = new LinkedList<>();
        SearchHits searchHits = searchResponse.getHits();
        if (searchHits == null || searchHits.getHits() == null) {
            pageResult.setPageNo(req.getPageNo());
            pageResult.setPageSize(req.getPageSize());
            pageResult.setRecords(subjectInfoEsList);
            pageResult.setTotal(0);
            return pageResult;
        }

        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            SubjectInfoEs subjectInfoEs = convertResult(hit);
            if (Objects.nonNull(subjectInfoEs)) {
                subjectInfoEsList.add(subjectInfoEs);
            }
        }
        pageResult.setPageNo(req.getPageNo());
        pageResult.setPageSize(req.getPageSize());
        pageResult.setRecords(subjectInfoEsList);
        pageResult.setTotal(Long.valueOf(searchHits.getTotalHits().value).intValue());

        return pageResult;
    }

    private SubjectInfoEs convertResult(SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        if (CollectionUtils.isEmpty(sourceAsMap)) {
            return null;
        }
        SubjectInfoEs result = new SubjectInfoEs();
        result.setSubjectId(MapUtils.getLong(sourceAsMap, EsSubjectFields.SUBJECT_ID));
        result.setSubjectName(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_NAME));
        result.setSubjectAnswer(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_ANSWER));
        result.setDocId(MapUtils.getLong(sourceAsMap, EsSubjectFields.DOC_ID));
        result.setSubjectType(MapUtils.getInteger(sourceAsMap, EsSubjectFields.SUBJECT_TYPE));
        result.setScore(new BigDecimal(String.valueOf(hit.getScore())).multiply(new BigDecimal("100.00")
                .setScale(2, RoundingMode.HALF_UP)));

        //处理name的高亮
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField subjectnameField = highlightFields.get(EsSubjectFields.SUBJECT_NAME);
        if (Objects.nonNull(subjectnameField)) {
            Text[] fragments = subjectnameField.getFragments();
            StringBuilder subjectNameBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectNameBuilder.append(fragment);
            }
            result.setSubjectName(subjectNameBuilder.toString());
        }
        //处理答案的高亮
        HighlightField subjectAnswerField = highlightFields.get(EsSubjectFields.SUBJECT_ANSWER);
        if (Objects.nonNull(subjectAnswerField)) {
            Text[] fragments = subjectAnswerField.getFragments();
            StringBuilder subjectAnswerBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectAnswerBuilder.append(fragment);
            }
            result.setSubjectAnswer(subjectAnswerBuilder.toString());
        }

        Object categoryNamesObj = sourceAsMap.get(EsSubjectFields.CATEGORY_NAMES);
        if (categoryNamesObj instanceof List) {
            result.setCategoryNames((List<String>) categoryNamesObj);
        }
        Object labelNamesObj = sourceAsMap.get(EsSubjectFields.LABEL_NAMES);
        if (labelNamesObj instanceof List) {
            result.setLabelNames((List<String>) labelNamesObj);
        }

        return result;
    }

    private EsSearchRequest createSearchListQuery(SubjectInfoEs req) {
        EsSearchRequest esSearchRequest = new EsSearchRequest();
        BoolQueryBuilder bq = new BoolQueryBuilder();
        MatchQueryBuilder subjectNameQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_NAME, req.getKeyWord()).analyzer("ik_smart");
        bq.should(subjectNameQueryBuilder);
        subjectNameQueryBuilder.boost(2);

        MatchQueryBuilder subjectAnswerQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_ANSWER, req.getKeyWord()).analyzer("ik_smart");
        bq.should(subjectAnswerQueryBuilder);

        // 默认检索全部题型；仅当入参明确传入 subjectType 时才进行题型过滤
        if (Objects.nonNull(req.getSubjectType())) {
            MatchQueryBuilder subjectTypeQueryBuilder =
                    QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_TYPE, req.getSubjectType());
            bq.must(subjectTypeQueryBuilder);
        }

        bq.minimumShouldMatch(1);

        //增加高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<b style = \"color:red\">");
        highlightBuilder.postTags("</b>");

        esSearchRequest.setBq(bq);
        esSearchRequest.setHighlightBuilder(highlightBuilder);
        esSearchRequest.setFields(EsSubjectFields.FIELD_QUERY);
        esSearchRequest.setFrom((req.getPageNo() - 1) * req.getPageSize());
        esSearchRequest.setSize(req.getPageSize());
        esSearchRequest.setNeedScroll(false);

        return esSearchRequest;
    }

    private EsIndexInfo getEsIndexInfo() {
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        String clusterName = "5ad1a12387b1";
        if (esConfigProperties != null && !CollectionUtils.isEmpty(esConfigProperties.getEsConfigs())) {
            clusterName = esConfigProperties.getEsConfigs().get(0).getName();
        }
        esIndexInfo.setClusterName(clusterName);
        esIndexInfo.setIndexName("subject_info");
        return esIndexInfo;
    }
}
