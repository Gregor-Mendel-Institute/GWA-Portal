package com.gmi.nordborglab.browser.server.es;

import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by uemit.seren on 1/28/15.
 */
@Component
public class EsIndexerApp {
    private static final String CONFIG_PATH = "classpath:META-INF/applicationContext.xml";
    private static int BULK_SIZE = 1000;


    @Resource
    protected EsIndexer esIndexer;

    @Resource
    protected GWASResultRepository gwasResultRepository;

    public static void main(String[] args) {
        final ApplicationContext context =
                new ClassPathXmlApplicationContext(CONFIG_PATH);
        final EsIndexerApp indexer =
                context.getBean(EsIndexerApp.class);
        indexer.index(args);
    }

    private void index(String[] args) {
        if (args.length != 1)
            throw new RuntimeException("You have to specifiy the type to index");
        switch (args[0].toLowerCase()) {
            case "gwasviewer":
                indexGWASViewer();
                break;

            default:
                throw new RuntimeException(String.format("Method %s not supported", args[0]));
        }
    }


    private void indexGWASViewer() {
        int start = 0;
        try {
            Page<GWASResult> gwasResultPage = null;
            do {
                gwasResultPage = gwasResultRepository.findAll(new PageRequest(start, BULK_SIZE));
                BulkResponse response = esIndexer.bulkIndex(gwasResultPage.getContent());
                if (response.hasFailures()) {
                    System.out.print(response.buildFailureMessage());
                }

            } while (!gwasResultPage.isLast());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
