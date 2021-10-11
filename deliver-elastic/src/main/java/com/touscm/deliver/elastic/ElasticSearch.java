package com.touscm.deliver.elastic;

import com.touscm.deliver.base.entry.ElasticEntry;
import com.touscm.deliver.base.entry.PagingEntry;
import com.touscm.deliver.base.utils.CollectionUtils;
import com.touscm.deliver.base.utils.EntryUtils;
import com.touscm.deliver.base.utils.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.action.support.IndicesOptions.LENIENT_EXPAND_OPEN;

/**
 * ElasticSearch服务
 */
@Service
public class ElasticSearch {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);

    public static final String KEY_FROM = "from_";
    public static final String KEY_TO = "to_";

    @Resource
    private RestHighLevelClient esClient;

    /* ...... */

    /**
     * 取得索引数组
     *
     * @return 索引数组
     */
    public String[] getIndices() {
        try {
            GetIndexResponse response = this.esClient.indices().get(new GetIndexRequest().indicesOptions(LENIENT_EXPAND_OPEN), RequestOptions.DEFAULT);
            response.getIndices();
        } catch (IOException e) {
            logger.error("get indices with exception", e);
        }
        return new String[]{};
    }

    /**
     * 判断索引是否存在
     *
     * @param index 索引
     * @return 判断结果
     */
    public boolean existIndex(@NotBlank String index) {
        try {
            return this.esClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("check index exist with exception, index:{}", index, e);
        }
        return false;
    }

    /**
     * 创建索引
     *
     * @param index 索引
     * @return 创建结果
     */
    public boolean createIndex(@NotBlank String index) {
        CreateIndexRequest request = new CreateIndexRequest(index);

        try {
            CreateIndexResponse response = this.esClient.indices().create(request, RequestOptions.DEFAULT);

            logger.debug("ElasticSearch Create Index API, index:{}, response:{}", index, response);
            return StringUtils.isNotEmpty(response.index());
        } catch (IOException e) {
            logger.error("ElasticSearch Create Index API with exception, index:{}", index, e);
        }

        return false;
    }

    /**
     * 取得索引
     *
     * @param index 索引
     * @return 索引
     */
    public GetIndexResponse getIndex(String index) {
        try {
            GetIndexResponse response = this.esClient.indices().get(new GetIndexRequest(index), RequestOptions.DEFAULT);
            System.out.println(response.toString());
            return response;
        } catch (IOException e) {
            logger.error("get index exception, index:{}", index, e);
        }
        return null;
    }

    /* ...... */

    /**
     * 统计索引记录数
     *
     * @param index 索引
     * @return 记录数
     */
    public long count(@NotBlank String index) {
        try {
            CountResponse response = this.esClient.count(new CountRequest(index), RequestOptions.DEFAULT);

            logger.debug("ElasticSearch Count API, index:{}, response:{}", index, response.toString());
            return response.getCount();
        } catch (IOException e) {
            logger.error("ElasticSearch Count API with exception, index:{}", index, e);
        }
        return 0;
    }

    /**
     * 取得索引记录
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param id        记录ID
     * @param <T>       记录实体类型
     * @return 索引记录实体
     */
    public <T> ElasticEntry<T> get(@NotNull Class<T> entryType, @NotBlank String index, @NotBlank String id) {
        try {
            GetResponse response = this.esClient.get(new GetRequest(index, id), RequestOptions.DEFAULT);
            logger.debug("ElasticSearch Get API, index:{}, id:{}, response:{}", index, id, response);

            if (response != null) {
                return new ElasticEntry<>(response.getId(), response.isExists() ? EntryUtils.parse(entryType, response.getSourceAsString()) : null);
            }
        } catch (IOException e) {
            logger.error("ElasticSearch Get API with exception, index:{}", index, e);
        }
        return null;
    }

    /**
     * 添加索引记录
     *
     * @param index 索引
     * @param entry 记录实例
     * @param <T>   实体类型
     * @return 添加结果
     */
    public <T> boolean index(@NotBlank String index, @NotNull T entry) {
        IndexRequest request = Requests.indexRequest(index).source(EntryUtils.toPropertyMap(entry));

        try {
            IndexResponse response = this.esClient.index(request, RequestOptions.DEFAULT);
            logger.debug("ElasticSearch Index API, index:{}, response:{}", index, response);
            return response.status() == RestStatus.CREATED;
        } catch (IOException e) {
            logger.error("ElasticSearch Index API with exception, index:{}", index, e);
        }
        return false;
    }

    /* ...... */

    /**
     * 取得索引记录分页
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param page      页码
     * @param size      分页尺寸
     * @param <T>       实体类型
     * @return 记录分页
     */
    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size) {
        return paging(entryType, index, page, size, null, null, false);
    }

    /**
     * 取得索引记录分页
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param page      页码
     * @param size      分页尺寸
     * @param sort      排序字段
     * @param <T>       实体类型
     * @return 记录分页
     */
    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, String sort) {
        return paging(entryType, index, page, size, null, sort, false);
    }

    /**
     * 取得索引记录分页
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param page      页码
     * @param size      分页尺寸
     * @param sort      排序字段
     * @param isAsc     是否顺序
     * @param <T>       实体类型
     * @return 记录分页
     */
    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, String sort, boolean isAsc) {
        return paging(entryType, index, page, size, null, sort, isAsc);
    }

    /**
     * 取得索引记录分页
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param page      页码
     * @param size      分页尺寸
     * @param filter    检索参数
     * @param sort      排序字段
     * @param <T>       实体类型
     * @return 记录分页
     */
    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, Map<String, Object> filter, String sort) {
        return paging(entryType, index, page, size, null, sort, false);
    }

    /**
     * 取得索引记录分页
     *
     * @param entryType 实体类型
     * @param index     索引
     * @param page      页码
     * @param size      分页尺寸
     * @param filter    检索参数
     * @param sort      排序字段
     * @param isAsc     是否顺序
     * @param <T>       实体类型
     * @return 记录分页
     */
    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, Map<String, Object> filter, String sort, boolean isAsc) {
        if (page <= 1) page = 1;
        int from = (page - 1) * size;
        SearchRequest request = new SearchRequest(index).source(getSearchSourceBuilder(filter, sort, isAsc, from, size));

        long itemCount = count(index);
        if (itemCount == 0) return new PagingEntry<>();

        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            logger.debug("ElasticSearch Search API, index:{}, sort:{}, page:{}, size:{}, response:{}", index, sort, page, size, response);

            if (response != null && response.status() == RestStatus.OK) {
                SearchHits hits = response.getHits();
                return new PagingEntry<>(page, size, itemCount, Arrays.stream(hits.getHits()).map(a -> new ElasticEntry<>(a.getId(), EntryUtils.parse(entryType, a.getSourceAsString()))).collect(toList()));
            }
        } catch (IOException e) {
            logger.error("ElasticSearch Search API with exception, index:{}, page:{}, size:{}, sort:{}, type:{}", index, page, size, sort, entryType.getName(), e);
        }

        return new PagingEntry<>();
    }

    /* ...... */

    @PreDestroy
    public void close() {
        if (this.esClient != null) {
            try {
                esClient.close();
            } catch (IOException e) {
                logger.error("Elasticsearch Java High Level REST Client close with exception", e);
            }
        }
    }

    /* ...... */

    private SearchSourceBuilder getSearchSourceBuilder(Map<String, Object> filters, String sort, boolean isAsc, int from, int size) {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        if (CollectionUtils.isNotEmpty(filters)) {
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

            filters.entrySet().stream().filter(a -> StringUtils.isNotEmpty(a.getKey()) && a.getValue() != null).forEach(a -> {
                if (a.getKey().startsWith(KEY_FROM) && KEY_FROM.length() < a.getKey().length()) {
                    queryBuilder.must(QueryBuilders.rangeQuery(a.getKey().substring(KEY_FROM.length())).from(a.getValue()));
                } else if (a.getKey().startsWith(KEY_TO) && KEY_TO.length() < a.getKey().length()) {
                    queryBuilder.must(QueryBuilders.rangeQuery(a.getKey().substring(KEY_TO.length())).to(a.getValue()));
                } else {
                    queryBuilder.must(QueryBuilders.matchPhraseQuery(a.getKey(), a.getValue()));
                }
            });

            if (queryBuilder.hasClauses()) {
                builder.query(queryBuilder);
            }
        }

        if (StringUtils.isNotBlank(sort)) {
            builder.sort(sort, isAsc ? SortOrder.ASC : SortOrder.DESC);
        }

        builder.from(from).size(size);
        return builder;
    }
}
