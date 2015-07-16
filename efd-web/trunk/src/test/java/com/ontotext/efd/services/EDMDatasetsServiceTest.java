package com.ontotext.efd.services;

import org.openrdf.model.Value;

import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-7-13.
 */
public class EDMDatasetsServiceTest {
    public static void main(String args[]) {
        EDMDatasetsService service = new EDMDatasetsService();
//        service.getAllEDMObjects();
        List<Map<String, List<Value>>> edm = service.getHornimanByProvider("Wolverhampton Arts and Museums");
        System.out.println("stop");
    }
}
