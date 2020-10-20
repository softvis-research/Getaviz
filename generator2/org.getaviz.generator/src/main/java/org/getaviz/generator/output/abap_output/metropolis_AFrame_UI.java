package org.getaviz.generator.output.abap_output;

import org.getaviz.generator.SettingsConfiguration;

public class metropolis_AFrame_UI implements ABAP_OutputFormat {

    private SettingsConfiguration config;

    public metropolis_AFrame_UI(SettingsConfiguration config) {
        this.config = config;
    }

    public String head() {
        return  "<!DOCTYPE html>" +
                "\n" +
                "<html>" +
                "\n" +
                "\t <head>" +
                "\n" +
                "\t\t <meta charset=\"utf-8\">" +
                "\n" +
                "\t    <title>Metropolis</title>" +
                "\n" +
                "\t    <meta name=\"description\" content=\"Getaviz\">" +
                "\n" +
                "\t </head>" +
                "\n" +
                "\t <body>" +
                "\n" +
                "\t\t <a-scene id=\"aframe-canvas\" cursor=\"rayOrigin: mouse\" embedded=\"true\" renderer=\"logarithmicDepthBuffer: true;\">" +
                "\n" +
                "\t\t\t <a-entity id=\"camera\" camera=\"fov: 80; zoom: 1;\"\n" +
                "\t\t    \t position=\"44.0 20.0 44.0\"\n" +
                "\t\t    \trotation=\"0 -90 0\"\n" +
                "\t\t    \t orbit-camera=\"\n" +
                "\t\t    \t   \t target: 15.0 1.5 15.0;\n" +
                "\t\t    \t   \t enableDamping: true;\n" +
                "\t\t    \t   \t dampingFactor: 0.25;\n" +
                "\t\t    \t   \t rotateSpeed: 0.25;\n" +
                "\t\t    \t   \t panSpeed: 0.25;\n" +
                "\t\t    \t   \t invertZoom: true;\n" +
                "\t\t    \t   \t logPosition: false;\n" +
                "\t\t    \t   \t minDistance:0;\n" +
                "\t\t    \t   \t maxDistance:1000;\n" +
                "\t\t    \t   \t \"\n" +
                "\t\t    \t mouse-cursor=\"\"\n" +
                "\t\t   \t\t >" +
                "\n" +
                "\t\t\t </a-entity>" +
                "\n" +
                "\t\t\t <a-assets>" +
                "\n" +
                "\t\t\t\t <img id=\"sky\" crossorigin=\"anonymous\" src=\"" + config.getMetropolisAssetsSourcePath("sky")  +  "\">" +
                "\n" +
                "\t\t\t\t <img id=\"sea\" crossorigin=\"anonymous\" src=\"" + config.getMetropolisAssetsSourcePath("sea")  +  "\">" +
                "\n" +
                "\t\t\t\t <img id=\"ground\" crossorigin=\"anonymous\" src=\"" + config.getMetropolisAssetsSourcePath("ground")  + "\">" +
                "\n" +
                "\t\t\t\t <a-asset-item id=\"mountain\" src=\"" + config.getMetropolisAssetsSourcePath("mountain")  +  "\"></a-asset-item>" +
                "\n" +
                "\t\t\t\t <a-asset-item id=\"cloud_black\" src=\"" + config.getMetropolisAssetsSourcePath("cloud")  +  "\"></a-asset-item>" +
                "\n" +
                "\t\t\t </a-assets>" +
                "\n" +
                "\t\t\t <a-sky src=\"#sky\" radius=\"7000\"></a-sky>" +
                "\n" +
                "\t\t\t <a-plane src=\"#ground\" height=\"5000\" width=\"5000\" rotation=\"-90 0 0\" position=\"0 0 0\" repeat=\"30 30\"></a-plane>" +
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
