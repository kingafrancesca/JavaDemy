package com.example.javademy.model;

/**
 * Reprezentuje temat nauki w aplikacji JavaDemy.
 * Każdy temat zawiera lekcje i zadania pogrupowane tematycznie.
 */
public class Topic {

    private String id;
    private String name;

    /** Tworzy instancję Topic. */
    public Topic() {}

    /**
     * Zwraca identyfikator tematu.
     * @return identyfikator tematu
     */
    public String getId()  { return id; }

    /**
     * Zwraca nazwę tematu.
     * @return nazwa tematu
     */
    public String getName() { return name; }
}