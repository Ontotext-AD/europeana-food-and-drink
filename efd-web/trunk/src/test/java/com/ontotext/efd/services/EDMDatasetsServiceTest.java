package com.ontotext.efd.services;

import org.openrdf.model.Value;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-7-13.
 */
public class EDMDatasetsServiceTest {
    public static void main(String args[]) {
        EDMDatasetsService service = new EDMDatasetsService();
//        service.getAllEDMObjects();
//        Map<String, Map<String, List<Value>>> edm = service.getHornimanByProvider("Wolverhampton Arts and Museums");
        Date start = new Date();
        Map<String, List<Value>> edm = service.getEDMObjectDetails("http://collections.horniman.ac.uk/objects/204703");
        Date stop = new Date();

        System.out.println(stop.getTime() - start.getTime());

        start = new Date();
        Map<String, List<Value>> edm1 = service.getEDMObjectDetails("http://collections.horniman.ac.uk/objects/66893");
        stop = new Date();

        System.out.println(stop.getTime() - start.getTime());
        System.out.println("stop");

    }
}
