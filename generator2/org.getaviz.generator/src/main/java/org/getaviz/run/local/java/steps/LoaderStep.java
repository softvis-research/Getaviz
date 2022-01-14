package org.getaviz.run.local.java.steps;

import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.run.local.java.MetropolisStep;
import org.neo4j.driver.v1.types.Node;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class LoaderStep extends MetropolisStep {
    private Runtime runtime = Runtime.getRuntime();
//    private String inputFiles = "https://github.com/softvis-research/Bank/releases/download/test/bank-1.0.0-SNAPSHOT.jar";
//    private String inputFiles = "C:\\Users\\mykha\\Desktop\\Bank\\out\\artifacts\\Bank_jar\\Bank.jar";
//    private String inputFiles = "C:\\Users\\mykha\\Downloads\\Java-master\\out\\artifacts\\Java_jar\\Java.jar";
    private String inputFiles = "C:\\Users\\mykha\\Desktop\\TestBank\\out\\artifacts\\TestBank_jar\\TestBank.jar";
//    private String pathJQAssistant = "C:/Users/mykha/jqassistant/bin/jqassistant.cmd";
    private String pathJQAssistant = "C:/Users/mykha/jqassistant-1.11.1/bin/jqassistant.cmd";

    public static void main(String[] args) {
        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();

        connector.close();

        System.out.println("\nLoader step was completed");
    }

    public void init() {
        // Make sure the graph is empty
//        connector.executeWrite("MATCH (n) DETACH DELETE n"); // TODO -reset by jQAssistant makes it for me

        log.info("jQAssistant scan started.");
        log.info("Scanning from URI(s) " + inputFiles);

        try {
//            String options = "scan -reset -u " + inputFiles + " -storeUri " + config.getDefaultBoldAddress();
            String options = "scan -reset -f " + inputFiles + " -storeUri " + config.getDefaultBoldAddress();
            Process pScan = runtime.exec(pathJQAssistant + " " + options);
//            pScan.destroy();
        } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }

        log.info("jQAssistant scan finished.");

        addAdditionalAttributes();
    }

    private void addAdditionalAttributes() {
        AtomicInteger elementId = new AtomicInteger(1);
        for(JavaNodeTypes type: JavaNodeTypes.values()) {
            connector.executeWrite("MATCH (n:" + type + ")" +
                    " WITH COLLECT(n) as nodesNumber" +
                    " FOREACH(i IN RANGE(0, SIZE(nodesNumber) - 1) |" +
                    " SET (nodesNumber[i]).element_id = " + elementId + " + i" +
                    " SET (nodesNumber[i]).iteration = 1" +
                    " SET (nodesNumber[i]).type_name = '" + type.name() + "')"
            );

//            if (type.equals(JavaNodeTypes.Class) || type.equals(JavaNodeTypes.Interface)) {
//                connector.executeWrite("MATCH(n:" + type + ") " +
//                        " SET n.subClassOf = ''" +
//                        " SET n.superClassOf = ''"
//                );
//            }

            connector.executeRead("MATCH (n:" + type + ") RETURN n")
                .forEachRemaining((result) -> {
                    elementId.addAndGet(1);
                });
        }

        setRootPackageIteration();
        setDeclarationId(JavaNodeTypes.Class);
        setDeclarationId(JavaNodeTypes.Interface);
        deleteUnnecessaryNodes();
    }

    private void deleteUnnecessaryNodes() {
        // DELETE Method nodes with name = null or empty value
        connector.executeWrite("MATCH (n:Method)" +
                " WHERE n.name CONTAINS '<init>' OR n.name IS NULL" +
                " DETACH DELETE n");

        // DELETE Field nodes where no declares id
        connector.executeWrite("MATCH (n:Field)" +
                " WHERE n.declares_id IS NULL" +
                " DETACH DELETE n");
    }

    private void setRootPackageIteration() {
        connector.executeRead("MATCH(p:Package) RETURN p")
            .forEachRemaining((result) -> {
                Node sourceNode = result.get("p").asNode();
                String fqn = sourceNode.get("fqn").asString();
                if (!fqn.contains(".")) {
                    int packageRootElementId = sourceNode.get("element_id").asInt();
                    connector.executeWrite("" +
                            " MATCH(p:Package)" +
                            " WHERE p.element_id = " + packageRootElementId +
                            " SET p.iteration = 0"
                    );
                }
            });
    }

    private void setDeclarationId(JavaNodeTypes type) {
        connector.executeWrite("MATCH (n:" + type + ")-[:DECLARES]->(m)" +
                " WITH n as classNode, COLLECT(m) as declaresNodes" +
                " FOREACH(i IN RANGE(0, SIZE(declaresNodes) - 1) |" +
                " SET (declaresNodes[i]).declares_id = classNode.element_id)"
        );
    }
}

