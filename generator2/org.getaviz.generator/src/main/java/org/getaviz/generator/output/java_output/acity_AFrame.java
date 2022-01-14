package org.getaviz.generator.output.java_output;

public class acity_AFrame implements Java_OutputFormat {

    public String head() {
        return "<!DOCTYPE html>" +
                "\n" +
                "<html>" +
                "\n" +
                "\t <head>" +
                "\n" +
                "\t\t <meta charset=\"utf-8\">" +
                "\n" +
                "\t    <title>ACity</title>" +
                "\n" +
                "\t    <meta name=\"description\" content=\"Getaviz\">" +
                "\n" +
                "\t    <script src=\"https://aframe.io/releases/1.0.2/aframe.min.js\"></script>" +
                "\n" +
                "\t </head>" +
                "\n" +
                "\t <body>" +
                "\n" +
                "\t\t <a-scene>" +
                "\n" +
                "\t\t\t <a-entity camera look-controls wasd-controls=\"acceleration: 5000\" position=\"-10 10 0\"></a-entity>" +
                "\n";

    }

    public String tail() {
        return "\t\t </a-scene>" +
                "\n" +
                " \t </body>" +
                "\n" +
                "</html>" +
                "\n";
    }

}
