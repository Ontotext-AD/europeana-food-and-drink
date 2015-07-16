package com.ontotext.efd.services;

import com.ontotext.efd.EDMConstants;
import org.openrdf.model.*;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import java.util.*;

/**
 * Created by boyan on 15-7-13.
 */
public class EDMDatasetsService {

    private  String detailsTemplate = "prefix edm: <http://www.europeana.eu/schemas/edm/>\n" +
            "construct {\n" +
            "   ?s ?p ?o.\n" +
            "   ?s ?x ?y.\n" +
            "   ?a ?b ?s.\n" +
            "   ?a ?x1 ?y1.\n" +
            "\n" +
            "} where {\n" +
            "  bind(<$OBJ> as ?s)\n" +
            "  {?s ?p edm:ProvidedCHO; ?x ?y.} union \n" +
            "  {?a edm:aggregatedCHO ?s; ?x1 ?y1}\n" +
            "\n" +
            "} limit 100";

    private String providedCHO = "SELECT * {?s rdf:type <http://www.europeana.eu/schemas/edm/ProvidedCHO>}";


    private String byDataProvider = "prefix edm: <http://www.europeana.eu/schemas/edm/>\n" +
            "SELECT ?s {\n" +
            "    ?s rdf:type <http://www.europeana.eu/schemas/edm/ProvidedCHO>.\n" +
            "    ?aggregatedCHO  edm:aggregatedCHO ?s.\n" +
            "    ?aggregatedCHO  edm:dataProvider \"$PROVIDER\"@en.\n" +
            "    \n" +
            "} ";

    private Repository getRepositoryConnection(){
        Repository repository = new HTTPRepository("http://192.168.130.19:8087/openrdf-sesame/", "test-insert");
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repository;
    }

    public List<Map<String, List<Value>>> getAllEDMObjects () {
        RepositoryConnection connection = null;
        List<Map<String, List<Value>>> EDM = new ArrayList<>();

        try {
            connection = getRepositoryConnection().getConnection();
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, providedCHO);
            TupleQueryResult result = tupleQuery.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                String object = bindings.getBinding("s").getValue().stringValue();
                EDM.add(getEDMObjectDetails(object));
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

    public List<Map<String, List<Value>>> getHornimanByProvider(String provider) {
        RepositoryConnection connection = null;
        List<Map<String, List<Value>>> EDM = new ArrayList<>();

        try {
            connection = getRepositoryConnection().getConnection();
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, byDataProvider.replace("$PROVIDER", provider));
            TupleQueryResult result = tupleQuery.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                String object = bindings.getBinding("s").getValue().stringValue();
                EDM.add(getEDMObjectDetails(object));
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
            connection = getRepositoryConnection().getConnection();
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
