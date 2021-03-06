package com.ontotext.efd.rdf;

public interface EFDTaxonomy {
    
    /**
     * Namespace URIs
     */
    String DC = "http://purl.org/dc/";
    String DB = "http://dbpedia.org/";
    String SKOS = "http://www.w3.org/2004/02/skos/core#";
    String EFD = "http://data.foodanddrinkeurope.eu/ontology#";
    String OWL = "http://www.w3.org/2002/07/owl#";

    /**
     * Entity URIs
     */
    String DCT = DC + "terms/";
    String DBO = DB + "ontology/";
    String DBR = DB + "resource/";
    String DBCAT = DBR + "Category:";
    String SKOS_CONCEPT = SKOS + "Concept";
    String EFD_ANNOTATOR_MANUAL = EFD + "ManualJudgement";
    String EFD_ANNOTATOR_AUTOMATED = EFD + "AutomatedJudgement";
    
    /**
     * Property URIs
     */
    String DCT_SUBJECT = DCT + "subject";
    String DB_PROP = DB + "property/";
    String SKOS_PREF_LABEL = SKOS + "prefLabel";
    String SKOS_BROADER = SKOS + "broader";
    String SKOS_REl = SKOS + "related";
    
    String EFD_IRRELEVANT = EFD + "notRelevant";
    String EFD_LEVEL = EFD + "treeLevel";
    String EFD_LOC_ART_CNT = EFD + "localArticleCount";
    String EFD_DESC_ART_CNT = EFD + "descArticleCount";
    String EFD_DESC_CNT = EFD + "descendantCount";
    String EFD_CHILD = EFD + "child";
    String EFD_LOC_EVD_CNT = EFD + "localEvidenceCount";
    String EFD_EVD_PROP_1 = EFD + "evidenceScore1";
    String EFD_EVD_PROP_2 = EFD + "evidenceScore2";
}
