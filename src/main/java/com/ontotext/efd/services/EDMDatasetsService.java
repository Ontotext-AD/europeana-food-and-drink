package com.ontotext.efd.services;

import com.ontotext.efd.EDMConstants;
import org.openrdf.model.*;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by boyan on 15-7-13.
 */
@Service
public class EDMDatasetsService {

    @org.springframework.beans.factory.annotation.Value("${edm.details.template}")
    private  String detailsTemplate;

    @org.springframework.beans.factory.annotation.Value("${edm.providedCHO}")
    private String providedCHO;

    @org.springframework.beans.factory.annotation.Value("${cho.by.dataprovider}")
    private String byDataProvider;

    @Autowired
    RepositoryConnectionService connectionService;

//    private Repository getRepositoryConnection(){
//        Repository repository = new HTTPRepository("http://192.168.130.19:8087/openrdf-sesame/", "DBpedia-efd");
//        try {
//            repository.initialize();
//        } catch (RepositoryException e) {
//            e.printStackTrace();
//        }
//        return repository;
//    }

    public Map<String, Map<String, List<Value>>> getAllEDMObjects () {
        RepositoryConnection connection = null;
        Map<String, Map<String, List<Value>>> EDM = new HashMap<>();

        try {
            connection = connectionService.getRepository().getConnection();
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, providedCHO);
            TupleQueryResult result = tupleQuery.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                String object = bindings.getBinding("s").getValue().stringValue();
                EDM.put(object, getEDMObjectDetails(object));
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }

        return EDM;
    }

    public Map<String, Map<String, List<Value>>> getHornimanByProvider(String provider) {
        RepositoryConnection connection = null;
        Map<String, Map<String, List<Value>>> EDM = new HashMap<>();

        try {
            connection = connectionService.getRepository().getConnection();
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, byDataProvider.replace("$PROVIDER", provider));
            TupleQueryResult result = tupleQuery.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                String object = bindings.getBinding("s").getValue().stringValue();
                EDM.put(object, getEDMObjectDetails(object));
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return EDM;
    }

    public Map<String, List<Value>> getEDMObjectDetails(String edm) {
        GraphQuery tupleQuery = null;
        Map<String, List<Value>> edmObj = new HashMap<>();
        RepositoryConnection connection = null;
        try {
            connection = connectionService.getRepository().getConnection();
            tupleQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, detailsTemplate.replace("$OBJ", edm));
            GraphQueryResult result = tupleQuery.evaluate();
            constructEDM(result, edmObj);

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }



        return edmObj;
    }

    private Map<String, List<Value>> constructEDM(GraphQueryResult result, Map<String, List<Value>> edmObj) throws QueryEvaluationException {
        while (result.hasNext()) {
            Statement statement = result.next();
            if (Arrays.asList(EDMConstants.FIELDS).contains(statement.getPredicate().stringValue())){
                if (edmObj.containsKey(statement.getPredicate().stringValue())) {
                    edmObj.get(statement.getPredicate().stringValue()).add(statement.getObject());
                }
                else {
                    List<Value> list = new ArrayList();
                    list.add(statement.getObject());
                    edmObj.put(statement.getPredicate().stringValue(), list);
                }
            }
        }

        return edmObj;
    }
}
