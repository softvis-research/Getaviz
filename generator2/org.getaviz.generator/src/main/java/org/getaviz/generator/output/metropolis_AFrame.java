package org.getaviz.generator.output;

import org.getaviz.generator.SettingsConfiguration;

public class metropolis_AFrame implements OutputFormat {

    private SettingsConfiguration config;

    public metropolis_AFrame(SettingsConfiguration config) {
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
                "\t\t <a-scene id=\"aframe-canvas\" embedded=\"true\">" +
                "\n" +
                "\t\t\t <a-entity id=\"rig\" position=\"25 10 0\">" +
                "\n" +
                "\t\t\t\t <a-entity id=\"cam\" camera look-controls wasd-controls=\"acceleration: 5000\" position=\"300 120 300\" rotation= \"0 -90 0\" ></a-entity>" +
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
                "\t\t\t <a-plane src=\"#ground\" height=\"1000\" width=\"1000\" rotation=\"-90 0 0\" position=\"0 0 0\" repeat=\"30 30\"></a-plane>" +
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
