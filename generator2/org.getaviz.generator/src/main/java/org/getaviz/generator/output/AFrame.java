package org.getaviz.generator.output;

public class AFrame implements OutputFormat {

    public String head() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("\n");
        builder.append("<html>");
        builder.append("\n");
        builder.append("\t <head>");
        builder.append("\n");
        builder.append("\t\t <meta charset=\"utf-8\">");
        builder.append("\n");
        builder.append("\t    <title>Ring</title>");
        builder.append("\n");
        builder.append("\t    <meta name=\"description\" content=\"Getaviz\">");
        builder.append("\n");
        builder.append("\t </head>");
        builder.append("\n");
        builder.append("\t <body>");
        builder.append("\n");
        builder.append("\t\t <a-scene id=\"aframe-canvas\"");
        builder.append("\n");
        builder.append("\t    \t cursor=\"rayOrigin: mouse\"");
        builder.append("\n");
        builder.append("\t    \t embedded=\"true\"");
        builder.append("\n");
        builder.append("\t    >");
        builder.append("\n");
        builder.append("\t\t    <a-entity");
        builder.append("\n");
        builder.append("\t\t    \t id=\"camera\"");
        builder.append("\n");
        builder.append("\t\t    \t camera=\"fov: 80; zoom: 1;\"");
        builder.append("\n");
        builder.append("\t\t    \t position=\"44.0 20.0 44.0\"");
        builder.append("\n");
        builder.append("\t\t    \t");
        builder.append("rotation=\"0 -90 0\"");
        builder.append("\n");
        builder.append("\t\t    \t orbit-camera=\"");
        builder.append("\n");
        builder.append("\t\t    \t   \t target: 15.0 1.5 15.0;");
        builder.append("\n");
        builder.append("\t\t    \t   \t enableDamping: true;");
        builder.append("\n");
        builder.append("\t\t    \t   \t dampingFactor: 0.25;");
        builder.append("\n");
        builder.append("\t\t    \t   \t rotateSpeed: 0.25;");
        builder.append("\n");
        builder.append("\t\t    \t   \t panSpeed: 0.25;");
        builder.append("\n");
        builder.append("\t\t    \t   \t invertZoom: true;");
        builder.append("\n");
        builder.append("\t\t    \t   \t logPosition: false;");
        builder.append("\n");
        builder.append("\t\t    \t   \t minDistance:0;");
        builder.append("\n");
        builder.append("\t\t    \t   \t maxDistance:1000;");
        builder.append("\n");
        builder.append("\t\t    \t   \t \"");
        builder.append("\n");
        builder.append("\t\t    \t mouse-cursor=\"\"");
        builder.append("\n");
        builder.append("\t\t   \t\t >");
        builder.append("\n");
        builder.append("\t\t     </a-entity>");
        builder.append("\n");
        return builder.toString();
    }

   public String tail() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t\t </a-scene>");
        builder.append("\n");
        builder.append(" \t </body>");
        builder.append("\n");
        builder.append("</html>");
        builder.append("\n");
        return builder.toString();
    }
}
