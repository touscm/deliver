package com.touscm.deliver.elastic;

import com.touscm.deliver.base.entry.ElasticEntry;
import com.touscm.deliver.base.entry.PagingEntry;
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

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.action.support.IndicesOptions.LENIENT_EXPAND_OPEN;

@Service
public class ElasticSearch {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearch.class);

    @Resource
    private RestHighLevelClient esClient;

    /* ...... */

    public String[] getIndices() {
        try {
            GetIndexResponse response = this.esClient.indices().get(new GetIndexRequest().indicesOptions(LENIENT_EXPAND_OPEN), RequestOptions.DEFAULT);
            response.getIndices();
        } catch (IOException e) {
            logger.error("get indices with exception", e);
        }
        return new String[]{};
    }

    public boolean existIndex(@NotBlank String index) {
        try {
            return this.esClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("check index exist with exception, index:{}", index, e);
        }
        return false;
    }

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

    public <T> ElasticEntry<T> get(@NotNull Class<T> entryType, @NotBlank String index, @NotBlank String id) {
        try {
            GetResponse response = this.esClient.get(new GetRequest(index, id), RequestOptions.DEFAULT);
            logger.debug("ElasticSearch Get API, index:{}, id:{}, response:{}", index, id, response);

            if (response != null && response.isExists()) {
                return new ElasticEntry<>(response.getId(), EntryUtils.parse(entryType, response.getSourceAsString()));
            }
        } catch (IOException e) {
            logger.error("ElasticSearch Get API with exception, index:{}", index, e);
        }
        return null;
    }

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

    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size) {
        return paging(entryType, index, page, size, null, false);
    }

    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, String sort) {
        return paging(entryType, index, page, size, sort, false);
    }

    public <T> PagingEntry<ElasticEntry<T>> paging(@NotNull Class<T> entryType, @NotBlank String index, int page, int size, String sort, boolean isAsc) {
        if (page <= 1) page = 1;
        int from = (page - 1) * size;
        SearchRequest request = new SearchRequest(index).source(getSearchSourceBuilder(sort, isAsc, from, size));

        long itemCount = count(index);
        if (itemCount == 0) return new PagingEntry<>();

        try {
            SearchResponse response = this.esClient.search(request, RequestOptions.DEFAULT);
            logger.debug("ElasticSearch Search API, index:{}, sort:{}, page:{}, size:{}, response:{}", index, sort, page, size, response);

            if (response != null && response.status() == RestStatus.OK) {
                SearchHits hits = response.getHits();
                return new PagingEntry<>(page, size, itemCount, Arrays.stream(hits.getHits()).map(a -> new ElasticEntry<T>(a.getId(), EntryUtils.parse(entryType, a.getSourceAsString()))).collect(toList()));
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

    private SearchSourceBuilder getSearchSourceBuilder(String sort, boolean isAsc, int from, int size) {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        if (StringUtils.isNotBlank(sort)) {
            builder.sort(sort, isAsc ? SortOrder.ASC : SortOrder.DESC);
        }

        builder.from(from).size(size);
        return builder;
    }
}
