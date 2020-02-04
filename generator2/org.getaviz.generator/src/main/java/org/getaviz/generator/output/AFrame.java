package org.getaviz.generator.output;

public class AFrame implements OutputFormat {

    public String head() {
        return "<!DOCTYPE html>" +
                "\n" +
                "<html>" +
                "\n" +
                "\t <head>" +
                "\n" +
                "\t\t <meta charset=\"utf-8\">" +
                "\n" +
                "\t    <title>Ring</title>" +
                "\n" +
                "\t    <meta name=\"description\" content=\"Getaviz\">" +
                "\n" +
                "\t </head>" +
                "\n" +
                "\t <body>" +
                "\n" +
                "\t\t <a-scene id=\"aframe-canvas\"" +
                "\n" +
                "\t    \t cursor=\"rayOrigin: mouse\"" +
                "\n" +
                "\t    \t embedded=\"true\"" +
                "\n" +
                "\t    >" +
                "\n" +
                "\t\t    <a-entity" +
                "\n" +
                "\t\t    \t id=\"camera\"" +
                "\n" +
                "\t\t    \t camera=\"fov: 80; zoom: 1;\"" +
                "\n" +
                "\t\t    \t position=\"44.0 20.0 44.0\"" +
                "\n" +
                "\t\t    \t" +
                "rotation=\"0 -90 0\"" +
                "\n" +
                "\t\t    \t orbit-camera=\"" +
                "\n" +
                "\t\t    \t   \t target: 15.0 1.5 15.0;" +
                "\n" +
                "\t\t    \t   \t enableDamping: true;" +
                "\n" +
                "\t\t    \t   \t dampingFactor: 0.25;" +
                "\n" +
                "\t\t    \t   \t rotateSpeed: 0.25;" +
                "\n" +
                "\t\t    \t   \t panSpeed: 0.25;" +
                "\n" +
                "\t\t    \t   \t invertZoom: true;" +
                "\n" +
                "\t\t    \t   \t logPosition: false;" +
                "\n" +
                "\t\t    \t   \t minDistance:0;" +
                "\n" +
                "\t\t    \t   \t maxDistance:1000;" +
                "\n" +
                "\t\t    \t   \t \"" +
                "\n" +
                "\t\t    \t mouse-cursor=\"\"" +
                "\n" +
                "\t\t   \t\t >" +
                "\n" +
                "\t\t     </a-entity>" +
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
